package com.mdb;

import static com.mdb.ReportingSnapshot.getsnapshot;
import static com.mdb.ServerMain.browsers;
import static com.mdb.ServerMain.defaultCharset;
import static com.mdb.Utilities.isValidSid;
import static com.mdb.Utilities.postedJson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class HandlerReport implements HttpHandler {

    JSONObject out = null;
    int resultCode = 0;

    public void handle(HttpExchange h) throws IOException {
        do {
            out = new JSONObject();
            try {

                //JSONObject posted = postedJson(h, defaultCharset, 4196, "/report");
                //String report;
                //String sid;

                JSONObject posted = new JSONObject();
                String report = "";

//                try {
//                    sid = posted.getString("sid");
//                    report = posted.getString("report");
//                } catch (Exception e) {
//                    throw new Exception("unable to parse message");
//                }
//                if (!isValidSid(sid)) {
//                    throw new Exception("access denied");
//                    //todo:403 forbidden
//                }
//
//                if (true) {
//                    throw new Exception("Endpoint temporary disabled");
//                }
//
                StringBuilder response = new StringBuilder();
                calculate(report, posted, response);

                new ResponseSender(h, response, 200);
            } catch (Exception e) {
                log.error("Error:", e);
                out.put("status", "ERROR");
                out.put("message", e.getMessage());
                resultCode = 400;
                new ResponseSender(h, out, resultCode);
            }
        } while (false);
    }

    private void calculate(String report, JSONObject posted, StringBuilder response) throws Exception {

//        log.info("Get data - KESZ");
//        ReportingSnapshot s1 = getsnapshot(Arrays.asList(
//            "KESZ.btul",
//            "KESZ.bcik",
//            "KESZ.bkod",
//            "KESZ.bbru",
//            "KESZ?"
//        ));
//        log.info("Get data done");

        log.info("Get data - BBEJ");
        ReportingSnapshot snapshot = getsnapshot(Arrays.asList(
            "BBEJ.btul",
            "BBEJ.bcik",
            "BBEJ.bkod",
            "BBEJ.bbru",
            "BBEJ?"
        ));
        log.info("Get data done");

        Integer aBbej = snapshot.sizes.get("BBEJ?");

        int[] cnt = new int[100];
        int[] bkod = (int[]) snapshot.data.get("BBEJ.bkod");
        byte[] btul = (byte[]) snapshot.data.get("BBEJ.btul");

        boolean[] btulFilter = new boolean[256];
        btulFilter[1] = true;

        for (int i = 1; i <= aBbej; i++) {
            int t = btul[i] & 0xff;
            int e = bkod[i] % 100;
            if (btulFilter[t]) {
                cnt[e]++;
            }
        }

        for (int i = 0; i < 100; i++) {
            if (cnt[i] > 0) {
                log.info(String.format("20%02d - %8d", i, cnt[i]));
            }
        }

    }


}
