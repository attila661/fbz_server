package com.mdb;

import static com.mdb.ServerMain.*;
import static com.mdb.Utilities.getkey;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class IndexEngine {

    public static final int e_notf = -1;
    public static final int e_inte = -2;
    public static final int e_used = -3;
    public static final int e_dupl = -4;
    public static final int e_ukod = -5;
    public static final int e_mnem = -6;
    public static final int e_full = -7;
    public static final int e_empt = -8;
    public static final int e_dele = -9;
    public static final int e_noti = -10;

    public static ByteBuffer bbFromInt8(byte value) {
        byte[] temp = new byte[1];
        ByteBuffer tempBb = ByteBuffer.wrap(temp);
        tempBb.order(ByteOrder.LITTLE_ENDIAN);
        temp[0] = value;
        return tempBb;
    }

    public static ByteBuffer bbFromInt16(char value) {
        byte[] temp = new byte[2];
        ByteBuffer tempBb = ByteBuffer.wrap(temp);
        tempBb.order(ByteOrder.LITTLE_ENDIAN);
        temp[0] = (byte) (value & 0xFF);
        temp[1] = (byte) (value >> 8);
        return tempBb;
    }

    public static ByteBuffer bbFromInt32(int value) {
        byte[] temp = new byte[4];
        ByteBuffer tempBb = ByteBuffer.wrap(temp);
        tempBb.order(ByteOrder.LITTLE_ENDIAN);
        temp[0] = (byte) (value & 0xFF);
        value >>= 8;
        temp[1] = (byte) (value & 0xFF);
        value >>= 8;
        temp[2] = (byte) (value & 0xFF);
        value >>= 8;
        temp[3] = (byte) (value & 0xFF);
        return tempBb;
    }

    public static ByteBuffer bbFromByteBuffer(ByteBuffer source, int offset, int length) {
        byte[] temp = new byte[length];
        ByteBuffer tempBb = ByteBuffer.wrap(temp);
        tempBb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < length; i++) {
            temp[i] = source.get(offset + i);
        }
        return tempBb;
    }

    public static int compare(List<ByteBuffer> item, List<Integer> itemSize, List<DataType> dataType,
                              ByteBuffer data, int baseOffset, List<Integer> offset) {
        for (int keyIndex = 0; keyIndex < item.size(); keyIndex++) {
            switch (dataType.get(keyIndex)) {
                case DATA_TYPE_STRING:
                    //todo: optimize
                    int i;
                    int ln = itemSize.get(keyIndex);
                    int temp = 0;
                    for (i = 0; i < ln; i++) {
                        temp = AnsiHun.ch2huIcsOrder[item.get(keyIndex).get(i) & 0xff] -
                               AnsiHun.ch2huIcsOrder[data.get(baseOffset + offset.get(keyIndex) + i) & 0xff];
                        if (temp != 0) {
                            return temp > 0 ? 1 : -1;
                        }
                    }
                    if (keyIndex + 1 == item.size()) {
                        return 0;
                    }
                    continue;
                case DATA_TYPE_INT8:
                    if (item.get(keyIndex).get(0) == data.get(baseOffset + offset.get(keyIndex))) {
                        if (keyIndex + 1 == item.size()) {
                            return 0;
                        }
                        continue;
                    }
                    if ((item.get(keyIndex).get(0) & 0xFF) > (data.get(baseOffset + offset.get(keyIndex)) & 0xFF)) {
                        return 1;
                    } else {
                        return -1;
                    }
                case DATA_TYPE_INT16:
                    if (item.get(keyIndex).getChar(0) == data.getChar(baseOffset + offset.get(keyIndex))) {
                        if (keyIndex + 1 == item.size()) {
                            return 0;
                        }
                        continue;
                    }
                    if (item.get(keyIndex).getChar(0) > data.getChar(baseOffset + offset.get(keyIndex))) {
                        return 1;
                    } else {
                        return -1;
                    }
                case DATA_TYPE_INT32:
                    if (item.get(keyIndex).getInt(0) == data.getInt(baseOffset + offset.get(keyIndex))) {
                        if (keyIndex + 1 == item.size()) {
                            return 0;
                        }
                        continue;
                    }
                    if (item.get(keyIndex).getInt(0) > data.getInt(baseOffset + offset.get(keyIndex))) {
                        return 1;
                    } else {
                        return -1;
                    }
                default:
                    throw new RuntimeException("Datatype comparator not implemented: " + dataType.get(keyIndex));
            }
        }
        return 0;
    }

    public static int find(ByteBuffer data, int recordSize, List<Integer> offset, int[] index,
                           List<ByteBuffer> item, List<Integer> itemSize, List<DataType> dataType,
                           int regionStart, int regionEnd, boolean first) {
        if (regionEnd == 0) {
            return -1;
        }
        int relation;
        if (first) {
            relation = compare(item, itemSize, dataType, data, recordSize * index[regionStart], offset);
            if (relation == -1) {
                return -regionStart;
            }
            if (relation == 0) {
                return regionStart;
            }
            relation = compare(item, itemSize, dataType, data, recordSize * index[regionEnd], offset);
            if (relation == 1) {
                return -(regionEnd + 1);
            }
            while (regionEnd > regionStart + 1) {
                int middle = (regionStart + regionEnd) / 2;
                relation = compare(item, itemSize, dataType, data, recordSize * index[middle], offset);
                if (relation == 1) {
                    regionStart = middle;
                } else {
                    regionEnd = middle;
                }
            }
            if (compare(item, itemSize, dataType, data, recordSize * index[regionStart], offset) == 0) {
                return regionStart;
            }
            if (compare(item, itemSize, dataType, data, recordSize * index[regionEnd], offset) == 0) {
                return regionEnd;
            }
        } else {
            relation = compare(item, itemSize, dataType, data, recordSize * index[regionStart], offset);
            if (relation == -1) {
                return -regionStart;
            }
            relation = compare(item, itemSize, dataType, data, recordSize * index[regionEnd], offset);
            if (relation == 1) {
                return -(regionEnd + 1);
            }
            if (relation == 0) {
                return regionEnd;
            }
            while (regionEnd > regionStart + 1) {
                int middle = (regionStart + regionEnd) / 2;
                relation = compare(item, itemSize, dataType, data, recordSize * index[middle], offset);
                if (relation == -1) {
                    regionEnd = middle;
                } else {
                    regionStart = middle;
                }
            }
            if (compare(item, itemSize, dataType, data, recordSize * index[regionEnd], offset) == 0) {
                return regionEnd;
            }
            if (compare(item, itemSize, dataType, data, recordSize * index[regionStart], offset) == 0) {
                return regionStart;
            }
        }
        return -regionEnd;
    }

    public static int insert(DatabaseTable table, Record record) {
        if (table.indexCount == 0) {
            return e_noti;
        }
        ByteBuffer recordByteBuffer = record.byteBuffer;
        List<DataType> dataTypes = new ArrayList<>();
        List<ByteBuffer> items = new ArrayList<>();
        List<Integer> offsets = new ArrayList<>();
        List<Integer> itemSizes = new ArrayList<>();
        int pos;
        //todo: table full
        for (int indexId = 1; indexId <= table.indexCount; indexId++) {
            getKeys(table, recordByteBuffer, indexId, items, dataTypes, offsets, itemSizes);
            pos = find(table.data, table.recordSize,
                       offsets, table.indexes.get(indexId), items, itemSizes, dataTypes, 1,
                       table.recordCount - (indexId == 1 ? 0 : 1), false);
            if (indexId == 1) {
                if (pos > 0) {
                    return e_dupl;
                } else {
                    pos = -pos;
                }
                if (pos <= table.recordCount) {
                    System.arraycopy(table.indexes.get(indexId), pos,
                                     table.indexes.get(indexId), pos + 1,
                                     table.recordCount - pos + 1);
                }
                table.indexes.get(indexId)[pos] = ++table.recordCount;
                System.arraycopy(record.byteArray, 0,
                                 table.dataByteArray, table.recordSize * table.recordCount,
                                 table.recordSize);
            } else {
                pos = pos > 0 ? pos + 1 : -pos;
                if (pos < table.recordCount) {
                    System.arraycopy(table.indexes.get(indexId), pos,
                                     table.indexes.get(indexId), pos + 1,
                                     table.recordCount - pos);
                }
                table.indexes.get(indexId)[pos] = table.recordCount;
            }
        }
        return table.recordCount;
    }

    private static void getKeys(DatabaseTable table, ByteBuffer record, int indexId,
                                List<ByteBuffer> items, List<DataType> dataTypes,
                                List<Integer> offsets, List<Integer> itemSizes) {

        items.clear();
        dataTypes.clear();
        offsets.clear();
        itemSizes.clear();

        for (int key : table.keyIndexes.get(indexId)) {
            FieldDefinition fieldDefinition = table.fieldList.get(key);
            dataTypes.add(fieldDefinition.dataType);
            itemSizes.add(fieldDefinition.size);
            offsets.add(fieldDefinition.position);
            items.add(
                bbFromByteBuffer(record, fieldDefinition.position, fieldDefinition.size * fieldDefinition.count));
        }

    }

    public static ByteBuffer newRecord(DatabaseTable table) {
        byte[] newRecordByteArray = new byte[table.recordSize];
        ByteBuffer newRecordByteBuffer = ByteBuffer.wrap(newRecordByteArray);
        newRecordByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return newRecordByteBuffer;
    }

    public static ByteBuffer newRecord(String table) {
        return newRecord(tables.get(table));
    }

}
