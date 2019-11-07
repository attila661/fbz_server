package com.mdb;

import static com.mdb.Utilities.postedJson;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;

public class HandlerPing implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {
        JSONObject out = new JSONObject();
        out.put("server", "fbz_server");
        out.put("charset", "áÁéÉíÍóÓöÖőŐúÚüÜűŰ");
        new ResponseSender(h, out, 200);
    }

}
