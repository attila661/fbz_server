package com.mdb;

import static com.mdb.ServerMain.defaultCharset;
import static com.mdb.ServerMain.flagLogRequests;
import static com.mdb.ServerMain.flagLogResponses;
import static com.mdb.ServerMain.tdbData;
import static com.mdb.ServerMain.tdbMaxSize;
import static com.mdb.ServerMain.tdbSize;
import static com.mdb.Utilities.isValidSid;
import static com.mdb.Utilities.postedJson;
import static com.mdb.Utilities.setResponseStats;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class HandlerRequest implements HttpHandler {

    JSONObject out = null;
    int resultCode = 0;

    public void handle(HttpExchange h) throws IOException {
        out = new JSONObject();
        resultCode = 500;
        handleMain(h);
        new ResponseSender(h, out, resultCode);
    }

    private int handleMain(HttpExchange h) {
        try {
            JSONObject posted = postedJson(h, defaultCharset, 4196, "/request");
            String command;
            try {
                if (!isValidSid(posted.getString("sid"))) {
                    throw new Exception("unauthorized");
                }
            } catch (Exception e) {
                return resultCode = setResponseStats(403, "ERROR", "access denied", out);
            }
            try {
                command = posted.getString("command");
                return mainCommandHandler(command, posted);
            } catch (Exception e) {
                return resultCode = setResponseStats(400, "ERROR", "bad request: 'command' key", out);
            }
        } catch (Exception e) {
            return resultCode = setResponseStats(400, "ERROR", e.getMessage(), out);
        }
    }

    private int mainCommandHandler(String command, JSONObject posted) {
        switch (command) {
            case "setflag":
                return commandSetFlag(posted);
            case "delete":
                return commandDelete(posted);
            case "insert":
                return commandInsert(posted);
            default:
                return resultCode = setResponseStats(400, "ERROR", "unknown command: " + command, out);
        }
    }

    private int commandInsert(JSONObject posted) {
        if (true) {
            return resultCode = setResponseStats(400, "ERROR", "Endpoint temporary disabled", out);
        }
        String table;
        JSONObject fields = new JSONObject();
        try {
            table = posted.getString("table");
            fields = posted.getJSONObject("fields");
        } catch (Exception e) {
            return resultCode = setResponseStats(400, "ERROR", "unable to parse table/fields in message", out);
        }

        //todo: hardcoded
        if (!table.equals("test_table_1")) {
            return resultCode = setResponseStats(200, "ERROR", "table not found: " + table, out);
        } else if (fields.length() != 1) {
            return resultCode = setResponseStats(200, "ERROR", "invalid field list", out);
        }
        try {
            if (!fields.keySet().contains("id")) {
                throw new Exception("invalid field list");
            }
        } catch (Exception e) {
            return resultCode = setResponseStats(200, "ERROR", "invalid field list", out);
        }
        int id;
        try {
            id = Integer.parseInt(fields.getString("id"));
        } catch (Exception e) {
            return resultCode = setResponseStats(200, "ERROR", "invalid field value: id", out);
        }
        if (id < 1 || 999 < id) {
            return resultCode = setResponseStats(200, "ERROR", "value out of range: id", out);
        }
        int pos = Arrays.binarySearch(tdbData, 1, tdbSize + 1, id);
        if (pos > 0) {
            return resultCode = setResponseStats(200, "ERROR", "duplicated key", out);
        }
        if (tdbSize >= tdbMaxSize) {
            return resultCode = setResponseStats(200, "ERROR", "Table full", out);
        }
        pos = -1 - pos;
        System.arraycopy(tdbData, pos, tdbData, pos + 1, tdbSize + 1 - pos);
        tdbData[pos] = id;
        tdbSize++;
        return resultCode = setResponseStats(200, "OK", "successful INSERT", out);
    }

    private int commandDelete(JSONObject posted) {
        if (true) {
            return resultCode = setResponseStats(400, "ERROR", "Endpoint temporary disabled", out);
        }
        String table;
        JSONObject keys = new JSONObject();
        try {
            table = posted.getString("table");
            keys = posted.getJSONObject("keys");
        } catch (Exception e) {
            return resultCode = setResponseStats(400, "ERROR", "unable to parse table/fields in message", out);
        }

        //todo: hardcoded
        if (!table.equals("test_table_1")) {
            return resultCode = setResponseStats(200, "ERROR", "table not found: " + table, out);
        } else if (keys.length() != 1) {
            return setResponseStats(400, "ERROR", "invalid key list", out);
        }

        try {
            if (!keys.keySet().contains("id")) {
                throw new Exception("invalid key list");
            }
        } catch (Exception e) {
            return setResponseStats(400, "ERROR", "invalid key list", out);
        }
        int id;
        try {
            id = Integer.parseInt(keys.getString("id"));
        } catch (Exception e) {
            return setResponseStats(400, "ERROR", "invalid key", out);
        }
        id = Arrays.binarySearch(tdbData, 1, tdbSize + 1, id);
        if (id < 0) {
            return setResponseStats(200, "ERROR", "NOT_FOUND", out);
        }
        System.arraycopy(tdbData, id + 1, tdbData, id, tdbSize - id);
        tdbSize--;
        return setResponseStats(200, "OK", "deleted", out);
    }

    private int commandSetFlag(JSONObject posted) {
        try {
            String flag = posted.getString("flag");
            String value = posted.getString("value");
            if (!value.equals("true") && !value.equals("false")) {
                return setResponseStats(400, "ERROR", "invalid flag value: " + value, out);
            }
            switch (flag) {
                case "flagLogRequests":
                    flagLogRequests = value.equals("true");
                    break;
                case "flagLogResponses":
                    flagLogResponses = value.equals("true");
                    break;
                default:
                    return setResponseStats(400, "ERROR", "unknown flag: " + flag, out);
            }
            return setResponseStats(200, "OK", "accepted", out);
        } catch (Exception e) {
            return setResponseStats(400, "ERROR", "unable to parse flag/value in message", out);
        }
    }

}
