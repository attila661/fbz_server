package com.mdb;

import static com.mdb.AnsiHun.hB;
import static com.mdb.AnsiHun.hC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Record {

    public DatabaseTable table;
    public byte[] byteArray;
    public ByteBuffer byteBuffer;

    public Record(DatabaseTable table) {
        initialize(table);
    }

    public Record(String table) {
        initialize(ServerMain.tables.get(table));
    }

    private void initialize(DatabaseTable table) {
        this.table = table;
        this.byteArray = new byte[table.recordSize];
        this.byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void setField(String field, String value) {
        FieldDefinition fieldDefinition = table.fieldMap.get(field);
        int base = fieldDefinition.position;
        int size = fieldDefinition.size;
        switch (fieldDefinition.dataType) {
            case DATA_TYPE_INT32:
                byteBuffer.putInt(base, Integer.parseInt(value));
                break;
            case DATA_TYPE_STRING:
                int inputLength = value.length();
                for (int i = 0; i < size; i++) {
                    if (i >= inputLength) {
                        byteArray[base + i] = ' ';
                    } else {
                        int p = AnsiHun.hC.indexOf(value.charAt(i));
                        if (p >= 0) {
                            byteArray[base + i] = (byte) AnsiHun.hB[p];
                        } else {
                            char c = value.charAt(i);
                            if (c > 255) {
                                byteArray[base + i] = '?';
                            } else {
                                byteArray[base + i] = (byte) c;
                            }
                        }
                    }
                }
                break;
            default:
                throw new RuntimeException("Unknown data type: " + fieldDefinition.dataType);
        }
    }

}
