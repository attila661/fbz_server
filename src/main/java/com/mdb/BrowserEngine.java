package com.mdb;

import static com.mdb.FieldDefinition.getTextFormatName;
import static com.mdb.ServerMain.tables;
import static com.mdb.Utilities.getkey;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class BrowserEngine implements BrowserInterface {

    public String tableName = "";
    public String viewInfo = "{}";
    public String indexInfo = "{}";
    public String browserDescription = "";

    public List<String> fieldNames = new ArrayList<>();
    public List<Integer> fieldIndexes = new ArrayList<>();
    public List<Integer> ouputFields = new ArrayList<>();
    public List<List<Integer>> keyIndexes = new ArrayList<>();


    public BrowserEngine(String tableName, String browserDescription, List<String> fieldNames) {
        this.tableName = tableName;
        this.browserDescription = browserDescription;
        DatabaseTable table = tables.get(tableName);

        this.fieldNames = fieldNames;
        fieldNames.stream().forEach(f -> fieldIndexes.add(table.fieldMap.get(f).schemaIndex));
        JSONArray h = new JSONArray();
        fieldNames.stream().forEach(f -> {
            JSONObject i = new JSONObject();
            i.put("field", table.fieldMap.get(f).name);
            i.put("description", table.fieldMap.get(f).description);
            i.put("format", getTextFormatName(table.fieldMap.get(f).textFormat));
            i.put("length", table.fieldMap.get(f).length);
            i.put("description", table.fieldMap.get(f).description);
            h.put(i);
        });
        viewInfo = h.toString(2);
        indexInfo = table.indexDef.toString(2);

        keyIndexes.add(null);
        Set<String> keyNames = new HashSet<>();
        for (int i = 1; i <= table.indexCount; i++) {
            List<Integer> indexes = new ArrayList<>();
            for (int j : table.keyIndexes.get(i)) {
                keyNames.add(table.fieldList.get(j).name);
                indexes.add(j);
            }
            keyIndexes.add(indexes);
        }

        IntStream.concat(fieldIndexes.stream().mapToInt(i -> i),
                         keyNames.stream().mapToInt(i -> table.fieldMap.get(i).schemaIndex))
            .sorted().distinct().forEach(ouputFields::add);

    }

    @Override
    public void getLines(int count, int cursor, int shift, int before, int after,
                         int idx, JSONObject keys, StringBuilder output)
        throws Exception {

        FieldDefinition fieldDefinition;
        DatabaseTable tbl = tables.get(tableName);

        boolean reverse = idx < 0 ? true : false;
        if (reverse) {
            idx = -idx;
        }

        if (tbl.indexCount == 0) {
            idx = 0;
        } else {
            if (idx < 1 || tbl.indexCount < idx) {
                throw new Exception("Index selection out of range (1-" + tbl.indexCount + ")");
            }
        }

        int mode;
        int index = 0;
        try {
            String pos = keys.getString("#position");
            switch (pos) {
                case "first":
                    mode = -1;
                    break;
                case "last":
                    mode = 1;
                    break;
                default:
                    try {
                        index = Math.abs(Integer.parseInt(pos));
                    } catch (Exception e) {
                        index = 1;
                    }
                    mode = 0;
            }
        } catch (Exception e) {
            mode = keys.length() == 0 ? -1 : 0;
        }

        int totalSize = tbl.recordCount;
        switch (mode) {
            case -1:
                index = reverse ? totalSize : 1;
                break;
            case 1:
                index = reverse ? 1 : totalSize;
                break;
            default:
                if (tbl.indexCount > 0) {
                    List<ByteBuffer> items = new ArrayList<>();
                    List<DataType> dataTypes = new ArrayList<>();
                    List<Integer> offsets = new ArrayList<>();
                    List<Integer> itemSizes = new ArrayList<>();

                    for (int key : keyIndexes.get(idx)) {
                        fieldDefinition = tbl.fieldList.get(key);
                        dataTypes.add(fieldDefinition.dataType);
                        itemSizes.add(fieldDefinition.size);
                        offsets.add(fieldDefinition.position);
                        items.add(getkey(fieldDefinition, keys));
                    }
                    index = IndexEngine.find(tbl.data, tbl.recordSize, offsets,
                                             tbl.indexes.get(idx), items, itemSizes, dataTypes,
                                             1, tbl.recordCount, true);
                }
                break;
        }
        if (reverse) {
            if (index > 0) {
                if (index > totalSize) {
                    index = totalSize + 1;
                }
                index = totalSize + 1 - index;
            } else {
                if (index < -totalSize) {
                    index = -totalSize - 1;
                }
                index = -totalSize - 2 - index;
            }
        }

        BrowserNavigatorStruct posStruct = new BrowserNavigatorStruct(count, cursor, shift, before, after,
                                                                      totalSize, index);
        //posStruct.calculatePositionsOld();
        posStruct.calculatePositions();

        output.append(String.format("{\n  \"totalsize\":\"%d\",\n", totalSize));
        output.append(String.format("  \"viewsize\":\"%d\",\n", posStruct.viewSize));
        output.append(String.format("  \"viewpos\":\"%d\",\n", posStruct.viewPosStart));
        output.append(String.format("  \"cursorpos\":\"%d\",\n", posStruct.cursor));

        int[] selectedIndex = idx > 0 ? tbl.indexes.get(idx) : null;

        if (totalSize < 1) {
            output.append("  \"cursor\":{\"#position\":\"first\"},\n");
        } else {
            output.append("  \"cursor\":{\n");
            output.append("     ");
            int currsorAbsolutePosition = getRecordAbsolutePosition(posStruct.cursor + posStruct.viewPosStart,
                                                                    selectedIndex, totalSize, reverse);
            for (int f = 0; f < ouputFields.size(); f++) {
                fieldDefinition = tbl.fieldList.get(ouputFields.get(f));
                if (f > 0) {
                    output.append(',');
                }
                output.append('"');
                output.append(fieldDefinition.name);
                output.append("\":\"");
                fieldDefinition.printField(output, tbl.data, currsorAbsolutePosition * tbl.recordSize, true);
                output.append('"');
            }
            output.append("\n  },\n");
        }

        printDataSection("before", posStruct.beforePosStart, posStruct.beforePosEnd,
                         output, selectedIndex, tbl, reverse);
        printDataSection("data", posStruct.viewPosStart, posStruct.viewPosEnd,
                         output, selectedIndex, tbl, reverse);
        printDataSection("after", posStruct.afterPosStart, posStruct.afterPosEnd,
                         output, selectedIndex, tbl, reverse);

        output.append("  \"status\":\"OK\",\n");
        output.append("  \"message\":\"successful\"\n}\n");
    }

    private void printDataSection(String label, int startLogicalPosition, int endLogicalPosition,
                                  StringBuilder output, int[] index, DatabaseTable table, boolean reverse)
        throws Exception {

        FieldDefinition fieldDefinition;
        output.append("  \"");
        output.append(label);
        output.append("\":[\n");
        for (int i = startLogicalPosition; i <= endLogicalPosition; i++) {
            output.append("    {");
            int recordOffset;
            getRecordAbsolutePosition(i, index, table.recordCount, reverse);
            if (index != null) {
                recordOffset = getRecordAbsolutePosition(i, index, table.recordCount, reverse) * table.recordSize;
            } else {
                recordOffset = i * table.recordSize;
                output.append("\"#position\":\"");
                output.append(i);
                output.append("\",");
            }
            for (int f = 0; f < ouputFields.size(); f++) {
                fieldDefinition = table.fieldList.get(ouputFields.get(f));
                if (f > 0) {
                    output.append(',');
                }
                output.append('"');
                output.append(fieldDefinition.name);
                output.append("\":\"");
                fieldDefinition.printField(output, table.data, recordOffset, true);
                output.append('"');
            }
            output.append('}');
            if (i != endLogicalPosition) {
                output.append(',');
            }
            output.append('\n');
        }
        output.append("  ],\n");

    }

    private int getRecordAbsolutePosition(int logicalPosition, int[] selectedIndex, int totalSize, boolean reverse) {
        if (selectedIndex != null) {
            return selectedIndex[reverse ? totalSize + 1 - logicalPosition : logicalPosition];
        } else {
            return logicalPosition;
        }

    }

    @Override
    public void getViewInfo(StringBuilder output) {
        output.append("{\"view\":" + viewInfo + ",\n");
        output.append("\"indexes\":" + indexInfo + "}\n");
        return;
    }

}
