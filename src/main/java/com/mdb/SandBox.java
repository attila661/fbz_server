package com.mdb;

import static com.mdb.AnsiHun.hun;
import static com.mdb.AnsiHun.toUpperInplace;
import static com.mdb.IndexEngine.insert;
import static com.mdb.ServerMain.browsers;
import static com.mdb.ServerMain.tables;
import static com.mdb.ServerMain.workdir;
import static com.mdb.Utilities.readFileToStringHun;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@Slf4j
public class SandBox {

    public static byte[] bytes;
    public static byte[] bytesU;
    public static int records = 0;
    public static int reclen = 0;

    public static void sandboxMain(String args[]) {

//        if(true){
//            return;
//        }

        log.info("Initializing data...");
        try {
            new DatabaseTable("VEVO");
            new DatabaseTable("SZAL");
            new DatabaseTable("TULA");

            new DatabaseTable("menu");

            new DatabaseTable("_tables");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error loading tables");
            System.exit(1);
        }

        generateAndCheckTestTables();

        try {

            bytes = FileUtils.readFileToByteArray(new File(workdir + "/" + "qscikk"));
            int l = bytes.length;
            bytesU = new byte[l];
            System.arraycopy(bytes, 0, bytesU, 0, l);
            toUpperInplace(bytesU);
            //writeByteArrayToFile(new File("work/conv/qscikku"), bytesU);
            //String upc = ba2s(bytesU);
            //writeStringToFile(new File("devdata/conv/qscikkus"), upc);

            reclen = 40;
            records = l / reclen;

            int[] cnt = new int[255];
            for (int i = 0; i < l; i++) {
                cnt[bytes[i] & 0xFF]++;
            }

//            for (int i = 0; i < 255; i++) {
//                if (cnt[i] > 0) {
//                    System.out.println(i + "\t" + (char) i + "\t" + cnt[i]);
//                }
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void generateAndCheckTestTables() {

        try {
            byte[] t1 = new byte[4 + 100 * 4 + 100];
            ByteBuffer t1bb = ByteBuffer.wrap(t1);
            t1bb.order(ByteOrder.LITTLE_ENDIAN);
            t1bb.putInt(100);
            for (int i = 1; i <= 100; i++) {
                t1bb.putInt(i);
            }
            for (int i = 1; i <= 100; i++) {
                t1bb.put((byte) (i * 2));
            }
            RandomAccessFile file = new RandomAccessFile(workdir + "/data/T1.TBL", "rw");
            file.setLength(0);
            file.write(t1);
            file.close();
        } catch (Exception e) {
            log.error("Error generating test table/1");
            System.exit(1);
            e.printStackTrace();
        }

        try {
            byte[] t1 = new byte[4 + 100 * 4 + 100];
            ByteBuffer t1bb = ByteBuffer.wrap(t1);
            t1bb.order(ByteOrder.LITTLE_ENDIAN);
            t1bb.putInt(100);
            for (int i = 1; i <= 100; i++) {
                t1bb.putInt(i);
            }
            for (int i = 1; i <= 100; i++) {
                t1bb.put((byte) (1 + (i * 2) / 10));
            }
            RandomAccessFile file = new RandomAccessFile(workdir + "/data/T2.TBL", "rw");
            file.setLength(0);
            file.write(t1);
            file.close();
        } catch (Exception e) {
            log.error("Error generating test table/2");
            System.exit(1);
            e.printStackTrace();
        }

        try {
            byte[] t1 = new byte[4 + 16 * 4 + 16 * 2];
            ByteBuffer t1bb = ByteBuffer.wrap(t1);
            t1bb.order(ByteOrder.LITTLE_ENDIAN);
            t1bb.putInt(16);
            for (int i = 1; i <= 16; i++) {
                t1bb.putInt(i);
            }

            for (int i = 1; i <= 16; i++) {
                t1bb.put((byte) (10 * ((i - 1) / 4 + 1)));
                t1bb.put((byte) (i * 10));
            }

            RandomAccessFile file = new RandomAccessFile(workdir + "/data/T3.TBL", "rw");
            file.setLength(0);
            file.write(t1);
            file.close();
        } catch (Exception e) {
            log.error("Error generating test table/3");
            System.exit(1);
            e.printStackTrace();
        }

        try {
            new DatabaseTable("T1");
            new DatabaseTable("T2");
            new DatabaseTable("T3");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error loading test tables");
            System.exit(1);
        }

//        byte[] it = new byte[1];
//        ByteBuffer itbb = ByteBuffer.wrap(it);
//        itbb.order(ByteOrder.LITTLE_ENDIAN);
//        DatabaseTable db;
//        log.info("---------------- test T1 first");
//        db = tables.get("T1");
//        for (int i = 1; i <= 5; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, true);
//            log.info("Find T1: " + i + " -> pos: " + pos);
//        }
//        for (int i = 196; i <= 202; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, true);
//            log.info("Find T1: " + i + " -> pos: " + pos);
//        }
//        log.info("---------------- test T2 first");
//        db = tables.get("T2");
//        for (int i = 0; i <= 3; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, true);
//            log.info("Find T2: " + i + " -> pos: " + pos);
//        }
//        for (int i = 18; i <= 22; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, true);
//            log.info("Find T2: " + i + " -> pos: " + pos);
//        }
//        log.info("---------------- test T1 last");
//        db = tables.get("T1");
//        for (int i = 1; i <= 5; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, false);
//            log.info("Find T1: " + i + " -> pos: " + pos);
//        }
//        for (int i = 196; i <= 202; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, false);
//            log.info("Find T1: " + i + " -> pos: " + pos);
//        }
//        log.info("---------------- test T2 last");
//        db = tables.get("T2");
//        for (int i = 0; i <= 3; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, false);
//            log.info("Find T2: " + i + " -> pos: " + pos);
//        }
//        for (int i = 18; i <= 22; i++) {
//            itbb.put(0, (byte) i);
//            int pos = IndexEngine.find(db.data, db.recordSize, Arrays.asList(0), db.indexes.get(1),
//                                       Arrays.asList(itbb), Arrays.asList(1),
//                                       Arrays.asList(DataType.DATA_TYPE_INT8),
//                                       1, db.recordCount, false);
//            log.info("Find T2: " + i + " -> pos: " + pos);
//        }
//
//        log.info("---------------- test T3 first");
//        db = tables.get("T3");
//        log.info(t3test(db, 1, 1));
//        log.info(t3test(db, 10, 9));
//        log.info(t3test(db, 10, 10));
//        log.info(t3test(db, 10, 11));
//        log.info(t3test(db, 10, 20));
//        log.info(t3test(db, 10, 50));
//        log.info(t3test(db, 40, 150));
//        log.info(t3test(db, 40, 160));
//        log.info(t3test(db, 40, 170));
//        log.info(t3test(db, 50, 0));

        JSONObject keys;
        keys = new JSONObject();
        keys.put("id", "10");
        browsers.put("test_table_1", new BrowserEngine("T1", "test view 1", Arrays.asList("id")));

        browsers.put("test_table_3", new BrowserEngine("T3", "test view 3", Arrays.asList("id1", "id2")));

        browsers.put("VEVO",
                     new BrowserEngine("VEVO", hun("Vevo''k teljes re'szletez'se"), Arrays.asList(
                         "kod", "name", "cim1", "cim2", "cim3", "cim4")));

        browsers.put("SZAL",
                     new BrowserEngine("SZAL", hun("Sza'lli'to'k minima'l"), Arrays.asList(
                         "kod", "name")));

        browsers.put("TULA",
                     new BrowserEngine("TULA", hun("Tulajdonosok minima'l"), Arrays.asList(
                         "kod", "name")));

//        StringBuilder out = new StringBuilder();
//        try {
//            browsers.get("test_table_1").getLines(10, 3, 0, 1, keys, out);
//            System.out.println(out.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            new DatabaseTable("CIKK");
            browsers
                .put("CIKK",
                     new BrowserEngine("CIKK", hun("Cikksza'mok minima'l"), Arrays.asList(
                         "ckod", "name", "ear")));

//            DatabaseTable ck = tables.get("CIKK");
//
//            FieldDefinition name = ck.fieldMap.get("name");
//            FieldDefinition ckod = ck.fieldMap.get("ckod");
//
//            List<Integer> itemSize = new ArrayList<>();
//            itemSize.add(name.size);
//            itemSize.add(ckod.size);
//
//            List<DataType> dataType = new ArrayList<>();
//            dataType.add(name.dataType);
//            dataType.add(ckod.dataType);
//
//            List<Integer> offset = new ArrayList<>();
//            offset.add(name.position);
//            offset.add(ckod.position);
//
//            int base = 5;
//            int baseOffset = ck.indexes.get(2)[base] * ck.recordSize;
//            List<ByteBuffer> item = new ArrayList<>();
//            item.add(bbFromByteBuffer(ck.data, baseOffset + name.position, 30));
//            item.add(bbFromInt16(ck.data.getChar(baseOffset + ckod.position)));
//
//            for (int i = 1; i <= 10; i++) {
//                int bo = ck.indexes.get(2)[i] * ck.recordSize;
//                int result = compare(item, itemSize, dataType, ck.data, bo, offset);
//                log.info(String.format("cmp %4d vs %4d -> %2d", base, i, result));
//            }

            new DatabaseTable("BBEJ");
            browsers.put("BBEJ",
                         new BrowserEngine("BBEJ", hun("Beve'telezett ba'la'k prototi'pus"), Arrays.asList(
                             "bkod", "bcik", "tetl", "bzsz", "bear", "bbru", "dttm", "src")));

            new DatabaseTable("BKIA");

            browsers.put("BKIA",
                         new BrowserEngine("BKIA",
                                           hun("Kiadott ba'la'k prototi'pus"), Arrays.asList(
                             "bkod", "bcik", "tetl", "bzsz", "bear", "bbru", "dttm", "src")));

            new DatabaseTable("KESZ");
            browsers.put("KESZ",
                         new BrowserEngine("KESZ", hun("Ke'szlet ba'la'k prototi'pus"), Arrays.asList(
                             "bkod", "bcik", "tetl", "bzsz", "bear", "bbru", "dttm", "src")));

            new DatabaseTable("GON");
            browsers.put("GON",
                         new BrowserEngine("GON", hun("Go:ngyo:legek re'szletez'se"), Arrays.asList(
                             "name", "kg1", "use")));

        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseTable table = tables.get("_tables");
        Record record = new Record(table);
        for (DatabaseTable t : tables.values()) {
            record.setField("name", t.name);
            record.setField("description", t.descrption);
            //log.info(t.name + ": " + insert(table, record));
            insert(table, record);
        }
        browsers.put("_tables",
                     new BrowserEngine("_tables", hun("Database Tables"), Arrays.asList(
                         "name", "description")));
        try {
            table = tables.get("menu");
            record = new Record(table);
            String data = readFileToStringHun("workdir/menu.tsv");
            String[] lines = data.split("\n");
            int n = lines.length;
            String[] names = new String[n];
            String[] titles = new String[n];
            String[] parents = new String[n];
            String[] icons = new String[n];
            String[] actions = new String[n];
            for (int i = 0; i < n; i++) {
                String[] fields = (lines[i] + "\t\n").split("\t");
                record.setField("name", names[i] = fields[0]);
                record.setField("title", titles[i] = fields[1]);
                record.setField("parent", parents[i] = fields[2]);
                record.setField("icon", icons[i] = fields[3]);
                record.setField("action", actions[i] = fields[4]);
                //log.info(fields[0] + ": " + insert(table, record));
                insert(table, record);
            }
            browsers.put("menu",
                         new BrowserEngine("menu", hun("Main menu"), Arrays.asList(
                             "name", "title", "parent", "icon", "action")));

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}
