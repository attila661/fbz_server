package com.mdb;

import static com.mdb.ServerMain.browsers;
import static com.mdb.ServerMain.flagLogRequests;
import static com.mdb.ServerMain.flagSidChecking;
import static com.mdb.ServerMain.mutexSid;
import static com.mdb.ServerMain.sidMap;

import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class Utilities {

    public static void sleepMillisec(Integer millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
            log.warn("Interrupt occured", e);
        }
    }

    public static JSONObject postedJson(HttpExchange h, String charset, int maxBytes, String info) throws Exception {
        InputStream in = h.getRequestBody();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int bytes = 0;
        for (int n = in.read(buf); n > 0; n = in.read(buf)) {
            out.write(buf, 0, n);
            bytes += n;
            if (bytes > maxBytes) {
                throw new Exception("Payload size out of range (" + maxBytes + " bytes)");
            }
        }
        try {
            if (flagLogRequests) {
                log.info("Request: " + info + "\n" + new String(out.toByteArray()) + "\n");
            }
            JSONObject posted = new JSONObject(new String(out.toByteArray(), charset));
            return posted;
        } catch (Exception e) {
            throw new Exception("Unable to parse posted data, JSON string expected");
        }
    }

    public static boolean isValidSid(String sid) {
        long now = System.currentTimeMillis();
        boolean result;
        mutexSid.lock();
        SessionInfo si = sidMap.get(sid);
        if (si == null) {
            result = !flagSidChecking;
        } else {
            si.lastActivity = now;
            si.accessCounter++;
            result = true;
            //todo: check expiration
        }
        mutexSid.unlock();
        return result;
    }

    public static int setResponseStats(int resultCode, String status, String message, JSONObject out) {
        out.put("resultCode", resultCode);
        out.put("status", status);
        out.put("message", message);
        return resultCode;
    }

    public static ByteBuffer getkey(FieldDefinition f, JSONObject keys) throws Exception {
        switch (f.dataType) {
            case DATA_TYPE_STRING:
                byte[] temp = new byte[f.size];
                ByteBuffer tempBb = ByteBuffer.wrap(temp);
                tempBb.order(ByteOrder.LITTLE_ENDIAN);
                String inp = keys.getString(f.name);
                int l = Math.min(f.size, inp.length());
                //todo: optimize
                for (int i = 0; i < l; i++) {
                    int p = AnsiHun.hC.indexOf(inp.charAt(i));
                    if (p >= 0) {
                        p = AnsiHun.hB[p];
                    } else {
                        p = inp.charAt(i);
                        if (p > 255) {
                            p = 32;
                        }
                    }
                    temp[i] = (byte) p;
                }
                return tempBb;
            case DATA_TYPE_INT32:
                int int32value = 0;
                try {
                    switch (f.textFormat) {
                        case TEXT_FORMAT_COMPOSITE1:
                        case TEXT_FORMAT_COMPOSITE2:
                        case TEXT_FORMAT_COMPOSITE2Z:
                        case TEXT_FORMAT_ZEROPADDED:
                        case TEXT_FORMAT_SPACEPADDED:
                        case TEXT_FORMAT_NUMBER:
                            int32value = Integer.parseInt(keys.getString(f.name).replaceAll(" |/", ""));
                            //todo: optimized parseint
                            break;
                        case TEXT_FORMAT_DATE:
                            String date = keys.getString(f.name);
                            int32value = (Integer.parseInt(date.substring(0, 4)) << 16)
                                         + (Integer.parseInt(date.substring(5, 7)) << 8)
                                         + Integer.parseInt(date.substring(8, 10));
                            break;
                    }
                } catch (Exception e) {
                    int32value = 0;
                }
                return IndexEngine.bbFromInt32(int32value);
            case DATA_TYPE_INT16:
                int int16value = 0;
                try {
                    int16value = Integer.parseInt(keys.getString(f.name));
                } catch (Exception e) {
                    int16value = 0;
                }
                return IndexEngine.bbFromInt16((char) int16value);
            case DATA_TYPE_INT8:
                int int8value = 0;
                try {
                    int8value = Integer.parseInt(keys.getString(f.name));
                } catch (Exception e) {
                    int8value = 0;
                }
                return IndexEngine.bbFromInt8((byte) int8value);
            default:
                throw new Exception("Not implemented datatype for browser getkey method: " + f.dataType);
        }
    }

    public static String readFileToStringHun(String filename) throws Exception {

        RandomAccessFile f = new RandomAccessFile(filename, "r");
        int l = (int) f.length();
        byte[] data = new byte[l + 1];
        f.read(data, 0, l);
        f.close();
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < l; i += 2) {
            char c = bb.getChar(i);
            switch (c) {
                case 41411:
                    c = 'á';
                    break;
                case 33219:
                    c = 'Á';
                    break;
                case 43459:
                    c = 'é';
                    break;
                case 35267:
                    c = 'É';
                    break;
                case 44483:
                    c = 'í';
                    break;
                case 36291:
                    c = 'Í';
                    break;
                case 46019:
                    c = 'ó';
                    break;
                case 37827:
                    c = 'Ó';
                    break;
                case 46787:
                    c = 'ö';
                    break;
                case 38595:
                    c = 'Ö';
                    break;
                case 37317:
                    c = 'ő';
                    break;
                case 37061:
                    c = 'Ő';
                    break;
                case 47811:
                    c = 'ú';
                    break;
                case 39619:
                    c = 'Ú';
                    break;
                case 48323:
                    c = 'ü';
                    break;
                case 40131:
                    c = 'Ü';
                    break;
                case 45509:
                    c = 'ű';
                    break;
                case 45253:
                    c = 'Ű';
                    break;
                default:
                    c = (char) (bb.get(i) & 0xff);
                    if (c > 127) {
                        log.warn("Char " + c + " " + filename + ":" + i);
                    }
                    i--;
                    break;
            }
            sb.append(c);
        }
        return sb.toString();

    }

    public static int getIntFromJsonOrDefault(String key, JSONObject json, int defaultValue) {
        try {
            return Integer.parseInt(json.getString(key));
        } catch (Exception e) {
        }
        try {
            return json.getInt(key);
        } catch (Exception e) {
        }
        return defaultValue;
    }
}