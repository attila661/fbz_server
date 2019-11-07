package com.mdb;

import static com.mdb.ServerMain.browsers;
import static com.mdb.ServerMain.defaultCharset;
import static com.mdb.Utilities.isValidSid;
import static com.mdb.Utilities.postedJson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;

public class HandlerBrowserInfo implements HttpHandler {

    JSONObject out = null;
    int resultCode = 0;

    public void handle(HttpExchange h) throws IOException {
        do {
            out = new JSONObject();
            try {

                JSONObject posted = postedJson(h, defaultCharset, 4196, "/browserinfo");
                String sid;
                String view;
                try {
                    sid = posted.getString("sid");
                    view= posted.getString("view");
                } catch (Exception e) {
                    throw new Exception("unable to parse message");
                }
                if (!isValidSid(sid)) {
                    throw new Exception("access denied");
                    //todo:403 forbidden
                }

                BrowserInterface browserInterface = browsers.get(view);
                StringBuilder response = new StringBuilder();
                browserInterface.getViewInfo(response);

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
