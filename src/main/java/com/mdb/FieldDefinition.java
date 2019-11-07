package com.mdb;

import static com.mdb.AnsiHun.b2c;

import java.nio.ByteBuffer;

enum DataType {
    DATA_TYPE_UNKNOWN,
    DATA_TYPE_INT8,
    DATA_TYPE_INT16,
    DATA_TYPE_INT32,
    DATA_TYPE_STRING,
    DATA_TYPE_CHAR,
}

enum TextFormat {
    TEXT_FORMAT_UNKNOWN,
    TEXT_FORMAT_NUMBER,
    TEXT_FORMAT_FIXED1,
    TEXT_FORMAT_COMPOSITE1,
    TEXT_FORMAT_COMPOSITE2,
    TEXT_FORMAT_COMPOSITE2Z,
    TEXT_FORMAT_FIXED2,
    TEXT_FORMAT_ZEROPADDED,
    TEXT_FORMAT_SPACEPADDED,
    TEXT_FORMAT_STRING,
    TEXT_FORMAT_CHAR,
    TEXT_FORMAT_INTEGER,
    TEXT_FORMAT_TIME,
    TEXT_FORMAT_DATE,
    TEXT_FORMAT_NOP,
}

public class FieldDefinition {

    public String name = "";
    public String description = "";
    public int position = 0;
    public DataType dataType = DataType.DATA_TYPE_UNKNOWN;
    public int size = 0;
    public int count = 0;
    public TextFormat textFormat = TextFormat.TEXT_FORMAT_UNKNOWN;
    public int length = 0;
    public int schemaIndex = 0;
    ;

    public FieldDefinition(String name, String description, int position, DataType dataType, int size, int count,
                           TextFormat textFormat, int length, int schemaIndex) {
        this.name = name;
        this.description = description;
        this.position = position;
        this.dataType = dataType;
        this.size = size;
        this.count = count;
        this.textFormat = textFormat;
        this.length = length;
        this.schemaIndex = schemaIndex;
    }

    public static DataType getDataType(String dt) throws Exception {
        switch (dt.toLowerCase()) {
            case "int8":
                return DataType.DATA_TYPE_INT8;
            case "int16":
                return DataType.DATA_TYPE_INT16;
            case "int32":
                return DataType.DATA_TYPE_INT32;
            case "string":
                return DataType.DATA_TYPE_STRING;
            case "char":
                return DataType.DATA_TYPE_CHAR;
            default:
                throw new Exception("Unknown datatype: " + dt);
        }
    }

    public static TextFormat getTextFormat(String tf) throws Exception {
        switch (tf.toLowerCase()) {
            case "composite1":
                return TextFormat.TEXT_FORMAT_COMPOSITE1;
            case "composite2":
                return TextFormat.TEXT_FORMAT_COMPOSITE2;
            case "composite2z":
                return TextFormat.TEXT_FORMAT_COMPOSITE2Z;
            case "number":
                return TextFormat.TEXT_FORMAT_NUMBER;
            case "fixed1":
                return TextFormat.TEXT_FORMAT_FIXED1;
            case "fixed2":
                return TextFormat.TEXT_FORMAT_FIXED2;
            case "spacepadded":
                return TextFormat.TEXT_FORMAT_SPACEPADDED;
            case "zeropadded":
                return TextFormat.TEXT_FORMAT_ZEROPADDED;
            case "string":
                return TextFormat.TEXT_FORMAT_STRING;
            case "char":
                return TextFormat.TEXT_FORMAT_CHAR;
            case "nop":
                return TextFormat.TEXT_FORMAT_NOP;
            case "time":
                return TextFormat.TEXT_FORMAT_TIME;
            case "date":
                return TextFormat.TEXT_FORMAT_DATE;
            default:
                throw new Exception("Unknown textformat: " + tf);
        }
    }

    public static String getTextFormatName(TextFormat tf) {
        switch (tf) {
            case TEXT_FORMAT_COMPOSITE1:
                return "composite1";
            case TEXT_FORMAT_COMPOSITE2:
                return "composite2";
            case TEXT_FORMAT_COMPOSITE2Z:
                return "composite2Z";
            case TEXT_FORMAT_FIXED1:
                return "fixed1";
            case TEXT_FORMAT_FIXED2:
                return "fixed2";
            case TEXT_FORMAT_NUMBER:
                return "number";
            case TEXT_FORMAT_SPACEPADDED:
                return "spacepadded";
            case TEXT_FORMAT_ZEROPADDED:
                return "zeropadded";
            case TEXT_FORMAT_STRING:
                return "string";
            case TEXT_FORMAT_CHAR:
                return "char";
            case TEXT_FORMAT_TIME:
                return "time";
            case TEXT_FORMAT_DATE:
                return "date";
            default:
                return tf.name();
        }
    }

    public void printField(StringBuilder out, ByteBuffer data, int recordPosition, boolean escape) throws Exception {
        switch (dataType) {
            case DATA_TYPE_INT8:
                int byteValue = data.get(recordPosition + position) & 0xFF;
                switch (textFormat) {
                    case TEXT_FORMAT_ZEROPADDED:
                        out.append(String.format("%0" + length + "d", byteValue));
                        break;
                    case TEXT_FORMAT_SPACEPADDED:
                        out.append(String.format("%" + length + "d", byteValue));
                        break;
                    case TEXT_FORMAT_NUMBER:
                        out.append(String.format("%d", byteValue));
                        break;
                    default:
                        throw new Exception("Unknown printField type " + dataType + "/" + textFormat);
                }
                break;
            case DATA_TYPE_INT16:
                int int16value = data.getChar(recordPosition + position);
                switch (textFormat) {
                    case TEXT_FORMAT_ZEROPADDED:
                        out.append(String.format("%0" + length + "d", int16value));
                        break;
                    case TEXT_FORMAT_FIXED1:
                        out.append(String.format("%" + length + ".1f", (double) (int16value / 10)));
                        break;
                    default:
                        throw new Exception("Unknown printField type " + dataType + "/" + textFormat);
                }
                break;
            case DATA_TYPE_INT32:
                int int32value = data.getInt(recordPosition + position);
                switch (textFormat) {
                    case TEXT_FORMAT_FIXED1:
                        out.append(String.format("%" + length + ".1f", ((double) int32value / 10)));
                        break;
                    case TEXT_FORMAT_COMPOSITE2Z:
                        out.append(String.format("%0" + (length - 3) + "d/%02d", int32value / 100, int32value % 100));
                        break;
                    case TEXT_FORMAT_COMPOSITE2:
                        out.append(String.format("%" + (length - 3) + "d/%02d", int32value / 100, int32value % 100));
                        break;
                    case TEXT_FORMAT_TIME:
                        if (int32value <= 0) {
                            out.append(String.format("-          -       "));
                        } else {
                            int sec = int32value & 63;
                            int min = (int32value >>= 6) & 63;
                            int hrs = (int32value >>= 6) & 31;
                            int day = (int32value >>= 5) & 31;
                            int mon = (int32value >>= 5) & 15;
                            int yrs = ((int32value >>= 4) & 31) + 2000;
                            out.append(String.format("%4d/%02d/%02d %02d:%02d:%02d", yrs, mon, day, hrs, min, sec));
                        }
                        break;
                    case TEXT_FORMAT_DATE:
                        if (int32value <= 0) {
                            out.append(String.format("----/--/--"));
                        } else {
                            int day = int32value & 0xFF;
                            int mon = (int32value & 0xFF00) >> 8;
                            int yrs = int32value >> 16;
                            out.append(String.format("%4d-%02d-%02d", yrs, mon, day));
                        }
                        break;
                    case TEXT_FORMAT_ZEROPADDED:
                        out.append(String.format("%0" + length + "d", int32value));
                        break;
                    case TEXT_FORMAT_SPACEPADDED:
                        out.append(String.format("%" + length + "d", int32value));
                        break;
                    case TEXT_FORMAT_NUMBER:
                        out.append(String.format("%d", int32value));
                        break;
                    default:
                        throw new Exception("Unknown printField type " + dataType + "/" + textFormat);
                }
                break;
            case DATA_TYPE_STRING:
                switch (textFormat) {
                    case TEXT_FORMAT_STRING:
                        for (int i = 0; i < length; i++) {
                            char c = b2c[data.get(recordPosition + position + i) & 0xFF];
                            if ((c == '\\' || c == '"') && escape) {
                                out.append('\\');
                            }
                            out.append(c);
                        }
                        break;
                    case TEXT_FORMAT_NOP:
                        out.append('?');
                        break;
                    default:
                        throw new Exception("Unknown printField type " + dataType + "/" + textFormat);
                }
                break;
            case DATA_TYPE_CHAR:
                char c = b2c[data.get(recordPosition + position) & 0xFF];
                if ((c == '\\' || c == '"') && escape) {
                    out.append('\\');
                }
                out.append(c);
                return;
            default:
                throw new Exception("Unknown printField type " + dataType + "/" + textFormat);
        }
    }
}
