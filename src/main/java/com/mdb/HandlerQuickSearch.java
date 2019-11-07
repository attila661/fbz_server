package com.mdb;

import static com.mdb.AnsiHun.ba2s;
import static com.mdb.AnsiHun.qsFindAll;
import static com.mdb.AnsiHun.string2byteArrayU;
import static com.mdb.ServerMain.defaultCharset;
import static com.mdb.Utilities.isValidSid;
import static com.mdb.Utilities.postedJson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class HandlerQuickSearch implements HttpHandler {

    JSONObject out = null;
    int resultCode = 0;

    public void handle(HttpExchange h) throws IOException {
        try {
            out = new JSONObject();
            JSONObject posted = postedJson(h, defaultCharset, 4196, "/quicksearch");
            if (!isValidSid(posted.getString("sid"))) {
                throw new Exception("unauthorized");
            }
            handlerMain(posted);
        } catch (Exception e) {
            out.put("status", "ERROR");
            out.put("message", e.getMessage());
            resultCode = 400;
        }
        new ResponseSender(h, out, resultCode);
    }

    private void handlerMain(JSONObject posted) {
        String table;
        try {
            table = posted.getString("table");
        } catch (Exception e) {
            out.put("status", "ERROR");
            out.put("message", "unable to get table from message");
            resultCode = 400;
            return;
        }
        if (!table.equals("test_qstable_1")) {
            out.put("status", "ERROR");
            out.put("message", "table not found: " + table);
            resultCode = 400;
            return;
        }

        boolean[] calc = null;
        if (posted.keySet().contains("include")) {
            try {
                JSONArray inc = posted.getJSONArray("include");
                for (int i = 0; i < inc.length(); i++) {
                    boolean group[] = new boolean[SandBox.records];
                    JSONArray is = inc.getJSONArray(i);
                    boolean incl = false;
                    for (int s = 0; s < is.length(); s++) {
                        String pattern = is.getString(s);
                        if (pattern.length() == 0) {
                            continue;
                        }
                        byte[] pb = string2byteArrayU(pattern);
                        qsFindAll(SandBox.bytesU, SandBox.reclen, pb, group);
                        incl = true;
                    }
                    if (incl) {
                        if (calc == null) {
                            calc = group;
                        } else {
                            for (int j = 0; j < SandBox.records; j++) {
                                calc[j] &= group[j];
                            }
                        }
                    }
                }
            } catch (Exception e) {
                out.put("status", "ERROR");
                out.put("message", "unable to parse include section");
                resultCode = 400;
            }
        }
        if (calc == null) {
            calc = new boolean[SandBox.records];
            Arrays.fill(calc, true);
        }

        if (posted.keySet().contains("exclude")) {
            try {
                boolean excl = false;
                JSONArray exc = posted.getJSONArray("exclude");
                boolean group[] = new boolean[SandBox.records];
                for (int s = 0; s < exc.length(); s++) {
                    String pattern = exc.getString(s);
                    if (pattern.length() == 0) {
                        continue;
                    }
                    byte[] pb = string2byteArrayU(pattern);
                    excl = true;
                    qsFindAll(SandBox.bytesU, SandBox.reclen, pb, group);
                }
                if (excl) {
                    for (int j = 0; j < SandBox.records; j++) {
                        if (group[j]) {
                            calc[j] = false;
                        }
                    }
                }
            } catch (Exception e) {
                out.put("status", "ERROR");
                out.put("message", "unable to parse include section");
                resultCode = 400;
            }
        }

        int limit = 0;
        try {
            limit = Integer.parseInt(posted.getString("limit"));
        } catch (Exception e) {
            //
        }
        if (limit < 1 || 50 < limit) {
            limit = 20;
        }

        JSONArray a = new JSONArray();
        int c = 0;
        for (int i = 0; i < SandBox.records; i++) {
            if (calc[i]) {
                a.put(ba2s(SandBox.bytes, i * SandBox.reclen, SandBox.reclen - 1));
                if (++c >= limit) {
                    break;
                }
            }
        }
        out.put("result", a);
        out.put("status", "OK");
        out.put("message", "success");
        resultCode = 200;

    }

}
