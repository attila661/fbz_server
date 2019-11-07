package com.mdb;

import static com.mdb.AnsiHun.b2c;
import static com.mdb.ServerMain.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportingSnapshot {

    public Map<String, Object> data = new TreeMap<>();
    public Map<String, int[]> indexes = new TreeMap<>();
    public Map<String, Integer> sizes = new TreeMap<>();

    //    ReportingSnapshot snapshot = getsnapshot(Arrays.asList(
    //        "menu.name",                                          // data field
    //        "menu$parent",                                        // data field string array
    //        "menu!parent",                                        // data field string array, trim
    //        "menu#1",                                             // index array
    //        "menu?"                                               // size
    //    ));

    public static ReportingSnapshot getsnapshot(List<String> items) throws Exception {
        ReportingSnapshot snapshot = new ReportingSnapshot();
        for (String item : items) {
            int pos = item.indexOf('.');
            if (pos >= 0) {
                getVector(snapshot, item, pos);
                continue;
            }
            pos = item.indexOf('#');
            if (pos >= 0) {
                getIndex(snapshot, item, pos);
                continue;
            }
            pos = item.indexOf('?');
            if (pos >= 0) {
                getSize(snapshot, item, pos);
                continue;
            }
            pos = item.indexOf('$');
            if (pos >= 0) {
                getStringArrayVector(snapshot, item, pos, false);
                continue;
            }
            pos = item.indexOf('!');
            if (pos >= 0) {
                getStringArrayVector(snapshot, item, pos, true);
                continue;
            }
            throw new Exception("Unknown getsnapshot command: " + item);
        }
        return snapshot;
    }

    private static void getStringArrayVector(ReportingSnapshot snapshot, String item, int pos, boolean trim)
        throws Exception {
        String tableName = item.substring(0, pos);
        String fieldName = item.substring(pos + 1);
        DatabaseTable table = tables.get(tableName);
        FieldDefinition fieldDefinition = table.fieldMap.get(fieldName);

        int recordSize = table.recordSize;
        int recordCount = table.recordCount;
        int base = fieldDefinition.position;

        if (fieldDefinition.dataType != DataType.DATA_TYPE_STRING) {
            throw new Exception("String data_type expected getStringArrayVector: " + fieldDefinition.dataType);
        }
        byte[] sourceArray = table.dataByteArray;
        String[] out = new String[recordCount + 1];
        int fieldSize = fieldDefinition.size;
        StringBuilder stringBuilder = new StringBuilder();

        for (int r = 0; r <= recordCount; r++) {
            for (int i = 0; i < fieldSize; i++) {
                stringBuilder.append(b2c[sourceArray[base + i] & 0xFF]);
            }
            base += recordSize;
            out[r] = trim ? stringBuilder.toString().trim() : stringBuilder.toString();
            stringBuilder.setLength(0);
        }
        snapshot.data.put(item, out);
    }

    private static void getVector(ReportingSnapshot snapshot, String item, int pos) throws Exception {
        String tableName = item.substring(0, pos);
        String fieldName = item.substring(pos + 1);
        DatabaseTable table = tables.get(tableName);
        FieldDefinition fieldDefinition = table.fieldMap.get(fieldName);

        int recordSize = table.recordSize;
        int recordCount = table.recordCount;
        int base = fieldDefinition.position;
        ByteBuffer source = table.data;
        switch (fieldDefinition.dataType) {
            case DATA_TYPE_INT8:
            case DATA_TYPE_CHAR:
                byte[] tempb = new byte[recordCount + 1];
                for (int i = 0; i <= recordCount; i++) {
                    tempb[i] = source.get(base);
                    base += recordSize;
                }
                snapshot.data.put(item, tempb);
                break;
            case DATA_TYPE_INT16:
                char[] tempc = new char[recordCount + 1];
                for (int i = 0; i <= recordCount; i++) {
                    tempc[i] = source.getChar(base);
                    base += recordSize;
                }
                snapshot.data.put(item, tempc);
                break;
            case DATA_TYPE_INT32:
                int[] tempi = new int[recordCount + 1];
                for (int i = 0; i <= recordCount; i++) {
                    tempi[i] = source.getInt(base);
                    base += recordSize;
                }
                snapshot.data.put(item, tempi);
                break;
            case DATA_TYPE_STRING:
                byte[] sourceArray = table.dataByteArray;
                byte[] tempBbArray = new byte[(recordCount + 1) * fieldDefinition.size];
                int fieldSize = fieldDefinition.size;
                for (int i = 0; i <= recordCount; i++) {
                    System.arraycopy(sourceArray, base, tempBbArray, i * fieldSize, fieldSize);
                    base += recordSize;
                }
                snapshot.data.put(item, tempBbArray);
                break;
            default:
                throw new Exception("Not supported data type in getVector: " + fieldDefinition.dataType);
        }
    }

    private static void getIndex(ReportingSnapshot snapshot, String item, int pos) {
        String tableName = item.substring(0, pos);
        DatabaseTable table = tables.get(tableName);
        int[] idx = table.indexes.get(Integer.valueOf(item.substring(pos + 1)));
        int[] out = new int[table.recordCount + 1];
        System.arraycopy(idx, 0, out, 0, table.recordCount + 1);
        snapshot.indexes.put(item, out);
    }

    private static void getSize(ReportingSnapshot snapshot, String item, int pos) {
        String tableName = item.substring(0, pos);
        snapshot.sizes.put(item, tables.get(tableName).recordCount);
    }

}
