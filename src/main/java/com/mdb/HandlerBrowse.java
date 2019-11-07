package com.mdb;

import static com.mdb.ServerMain.browsers;
import static com.mdb.ServerMain.defaultCharset;
import static com.mdb.Utilities.getIntFromJsonOrDefault;
import static com.mdb.Utilities.isValidSid;
import static com.mdb.Utilities.postedJson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;

public class HandlerBrowse implements HttpHandler {

    JSONObject out = null;
    int resultCode = 0;

    public void handle(HttpExchange h) throws IOException {
        do {
            out = new JSONObject();
            try {
                JSONObject posted = postedJson(h, defaultCharset, 4196, "/browse");
                String sid;
                String from;
                String indexS;
                String cursorS;
                String countS;
                String shiftS;
                JSONObject keys;
                try {
                    sid = posted.getString("sid");
                    from = posted.getString("table");
                    indexS = posted.getString("index");
                    cursorS = posted.getString("cursor");
                    countS = posted.getString("count");
                    shiftS = posted.getString("shift");
                    keys = posted.getJSONObject("keys");
                } catch (Exception e) {
                    throw new Exception("unable to parse message");
                }

                if (!isValidSid(sid)) {
                    throw new Exception("access denied");
                    //todo:403 forbidden
                }

                int cursor;
                int shift;
                int count;
                try {
                    cursor = Integer.parseInt(cursorS);
                    shift = Integer.parseInt(shiftS);
                    count = Integer.parseInt(countS);
                } catch (Exception e) {
                    throw new Exception("unable to parse data: count/shift/cursor");
                }
                if (cursor < -2_000_000_000 || 2_000_000_000 < cursor) {
                    throw new Exception("cursor value out of range (-2_000_000_000 - +2_000_000_000)");
                }
                if (count < 1 || 50 < count) {
                    throw new Exception("count value out of range (1 - 50)");
                }
                if (shift < -2_000_000_000 || 2_000_000_000 < shift) {
                    throw new Exception("shift value out of range (-2_000_000_000 - +2_000_000_000)");
                }

                int after = getIntFromJsonOrDefault("after", posted, 0);
                if (after < 0 || 50 < after) {
                    throw new Exception("'after' value out of range (1 - 50)");
                }
                int before = getIntFromJsonOrDefault("before", posted, 0);
                if (before < 0 || 50 < before) {
                    throw new Exception("'before' value out of range (1 - 50)");
                }

                BrowserInterface browserInterface = browsers.get(from);

                if (browserInterface == null) {
                    throw new Exception("Unknown browser");
                }
                int index;
                try {
                    index = Integer.parseInt(indexS);
                } catch (Exception e) {
                    index = 0;
                }

                StringBuilder response = new StringBuilder();
                browserInterface.getLines(count, cursor, shift, before, after, index, keys, response);
                new ResponseSender(h, response, 200);
            } catch (Exception e) {
                out.put("status", "ERROR");
                out.put("message", e.getMessage());
                resultCode = 400;
                new ResponseSender(h, out, resultCode);
            }
        } while (false);
    }

}
