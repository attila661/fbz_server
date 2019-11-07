package com.mdb;

import static com.mdb.ServerMain.workdir;
import static com.mdb.Utilities.readFileToStringHun;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class DatabaseTable {

    String name = null;
    String descrption = null;
    String tableFile = "";
    int indexCount = 0;
    int recordSize = 0;
    int recordCount = 0;
    int blockSize = 0;

    List<int[]> indexes = new ArrayList<>();
    List<List<Integer>> keyIndexes = new ArrayList<>();
    List<String> indexDescriptions = new ArrayList<>();

    Map<String, FieldDefinition> fieldMap = new TreeMap<>();
    List<FieldDefinition> fieldList = new ArrayList<>();

    JSONArray indexDef = new JSONArray();

    byte[] dataByteArray = null;
    ByteBuffer data = null;

    public DatabaseTable(String name) throws Exception {
        this.name = name;
        load(0);
    }

    public DatabaseTable(String name, int dump) throws Exception {
        this.name = name;
        load(dump);
    }

    public int sumFieldSizes() {
        return fieldMap.values().stream().mapToInt(f -> f.size * f.count).sum();
    }

    private void addField(String name, String desc, DataType dt, int size, int count, TextFormat df, int length) {
        FieldDefinition fd = new FieldDefinition(name, desc, sumFieldSizes(),
                                                 dt, size, count, df, length, fieldList.size());
        fieldMap.put(name, fd);
        fieldList.add(fd);
    }

    private void parseFieldDefinition(JSONArray fieldDef) throws Exception {
        for (int i = 0; i < fieldDef.length(); i++) {
            JSONObject field = fieldDef.getJSONObject(i);
            addField(field.getString("name"),
                     field.getString("description"),
                     FieldDefinition.getDataType(field.getString("datatype")),
                     field.getInt("datasize"),
                     field.getInt("count"),
                     FieldDefinition.getTextFormat(field.getString("textformat")),
                     field.getInt("textlength"));
        }
    }

    private void parseIndexDefinition() throws Exception {
        if ((indexCount = indexDef.length()) == 0) {
            return;
        }
        keyIndexes.add(null);
        indexDescriptions.add(null);
        for (int i = 0; i < indexCount; i++) {
            JSONObject index = indexDef.getJSONObject(i);
            indexDescriptions.add(index.getString("description"));
            List<Integer> indexes = new ArrayList<>();
            JSONArray keys = index.getJSONArray("keys");
            for (int j = 0; j < keys.length(); j++) {
                indexes.add(fieldMap.get(keys.getString(j)).schemaIndex);
            }
            keyIndexes.add(indexes);
        }
    }

    public void load(int dump) throws Exception {

        try {
            String filename = (name.startsWith("_") ? "./config/" : workdir + "/schema/") + name.toLowerCase() + ".inf";
            log.info("------------------------------ Loading defs: " + name + " -> " + filename);
            JSONObject def = new JSONObject(readFileToStringHun(filename));

            descrption = def.getString("description");
            JSONArray fieldDef = def.getJSONArray("fields");
            parseFieldDefinition(fieldDef);

            indexDef = def.getJSONArray("indexes");
            parseIndexDefinition();

            blockSize = def.getInt("blocksize");
            indexCount = indexDef.length();
            recordSize = sumFieldSizes();
            log.info(name + " fieldCount: " + fieldList.size());
            log.info(name + " recordSize: " + recordSize);

            tableFile = def.getString("filename");

            if (tableFile.length() > 0) {
                loadFromFile();
            } else {
                allocateOnly();
            }
            data = ByteBuffer.wrap(dataByteArray);
            data.order(ByteOrder.LITTLE_ENDIAN);

            if (dump > 0) {
                dumpRecords(dump, 1);
            }
            ServerMain.tables.put(name, this);

        } catch (Exception e) {
            log.error("Error mounting table", e);
            name = null;
            indexes = null;
            dataByteArray = null;
            throw new Exception("Error mounting table: " + name);
        }
    }

    private void allocateOnly() {
        log.info("Allocating storage for " + blockSize + " item(s)");
        recordCount = 0;
        indexes.add(null);
        for (int i = 1; i <= indexCount; i++) {
            indexes.add(new int[recordCount + 1 + blockSize]);
        }
        dataByteArray = new byte[(recordCount + 1 + blockSize) * recordSize];
        log.info("Successfully allocated: " + name);
    }

    public void dumpRecords(int dump, int indexId) throws Exception {
        StringBuilder out = new StringBuilder();
        int recordpos = 0;
        int[] idx = indexId > indexCount ? null : indexes.get(indexId);
        for (int r = 1; r <= Math.min(recordCount, 10); r++) {
            out.setLength(0);
            recordpos = recordSize * (idx == null ? r : idx[r]);
            int d = 0;
            for (FieldDefinition f : fieldList) {
                if (out.length() > 0) {
                    out.append(" / ");
                }
                f.printField(out, data, recordpos, true);
                if (++d == dump) {
                    break;
                }
            }
            log.info(out.toString());
        }
    }

    private void loadFromFile() throws Exception {

        String filename;
        filename = workdir + "/data/" + tableFile + ".TBL";
        log.info("Loading data: " + name + " -> " + filename);

        RandomAccessFile file;
        try {
            file = new RandomAccessFile(filename, "r");
        } catch (FileNotFoundException e) {
            file = null;
        }

        byte[] temp;
        ByteBuffer tempb;
        int readed;
        if (indexCount > 0) {
            if (file != null && file.length() > 0) {
                temp = new byte[4];
                readed = file.read(temp);
                if (readed != 4) {
                    throw new Exception("Error reading header");
                }

                tempb = ByteBuffer.wrap(temp);
                tempb.order(ByteOrder.LITTLE_ENDIAN);
                recordCount = tempb.getInt(0);
                if (recordCount < 0 || 2_000_000_000 < recordCount) {
                    throw new Exception("Invalid record count");
                }
            } else {
                recordCount = 0;
            }
        } else {
            recordCount = blockSize;
            blockSize = 0;
        }
        log.info("Record count: " + recordCount);

        dataByteArray = new byte[(recordCount + 1 + blockSize) * recordSize];
        temp = recordCount * 4 + 4 > dataByteArray.length ? new byte[recordCount * 4 + 4] : dataByteArray;

        tempb = ByteBuffer.wrap(temp);
        tempb.order(ByteOrder.LITTLE_ENDIAN);
        indexes.add(null);
        for (int i = 1; i <= indexCount; i++) {
            indexes.add(new int[recordCount + 1 + blockSize]);
            if (recordCount > 0) {
                log.info("Loading index #" + i);
                readed = file.read(temp, 4, recordCount * 4);
                if (readed != recordCount * 4) {
                    throw new Exception("Error reading index #" + i);
                }
                int[] index = indexes.get(i);
                for (int j = 1; j <= recordCount; j++) {
                    index[j] = tempb.getInt(j * 4);
                }
            }
        }
        if (recordCount > 0) {
            log.info("Loading data");
            readed = file.read(dataByteArray, recordSize, recordCount * recordSize);
            if (readed != recordCount * recordSize) {
                throw new Exception("Error reading data");
            }
        } else {
            log.info("Empty table");
        }
        log.info("Successfully loaded: " + name);
    }

    public void saveToFile() throws Exception {
        String filename;
        filename = workdir + "/data/" + tableFile + ".TBL";
        log.info("Saving data: " + name + " -> " + filename);

        RandomAccessFile file = new RandomAccessFile(filename, "rw");
        file.setLength(0);

        byte[] temp;
        ByteBuffer tempb;
        int readed;
        if (indexCount > 0) {
            temp = new byte[4];
            tempb = ByteBuffer.wrap(temp);
            tempb.order(ByteOrder.LITTLE_ENDIAN);
            tempb.putInt(recordCount);
            file.write(temp);
        }
        log.info("Record count: " + recordCount);

        byte[] indexData = new byte[(recordCount + 1) * 4];
        tempb = ByteBuffer.wrap(indexData);
        tempb.order(ByteOrder.LITTLE_ENDIAN);

        if (recordCount > 0) {
            for (int i = 1; i <= indexCount; i++) {
                log.info("Saving index #" + i);
                int[] index = indexes.get(i);
                for (int j = 0; j <= recordCount; j++) {
                    tempb.putInt(j * 4, index[j]);
                }
                file.write(indexData, 4, recordCount * 4);
            }
            log.info("Saving data");
            file.write(dataByteArray, recordSize, recordCount * recordSize);
        }
        log.info("Successfully saved: " + name);
    }

}
