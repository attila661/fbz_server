package com.mdb;

import static com.mdb.ServerMain.flagLogResponses;
import static com.mdb.Utilities.sleepMillisec;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.OutputStream;

@Slf4j
public class ResponseSender implements Runnable {

    private final int DEFAULT_BLOCK_SIZE = 65536;
    private final int DEFAULT_TIMEOUT_MS = 10000;

    private String id;
    private OutputStream out;
    private byte[] data;

    private Thread senderThread;
    private boolean interrupted = false;

    public class ReponseGuard implements Runnable {

        private final int sleepBlockMs = 100;

        public void run() {
            int elapsedMs = 0;
            while (elapsedMs < DEFAULT_TIMEOUT_MS) {
                int timeToSleepMs = elapsedMs + sleepBlockMs <= DEFAULT_TIMEOUT_MS ?
                                    sleepBlockMs : DEFAULT_TIMEOUT_MS - elapsedMs;
                sleepMillisec(timeToSleepMs);
                elapsedMs += timeToSleepMs;
                if (!senderThread.isAlive()) {
                    return; // already died
                }
            }
            try {
                log.info("ResponseGuard:  interrupt ## " + id);
                interrupted = true;
                senderThread.interrupt();
            } catch (Exception e) {
                // failed to interrupt, already died
            }
        }
    }

    public ResponseSender(String id, OutputStream out, byte[] data) {
        this.out = out;
        this.data = data;
        this.id = id;
        senderThread = new Thread(this);
        senderThread.start();
        ReponseGuard reponseGuard = new ReponseGuard();
        new Thread(reponseGuard).start();
    }

    public ResponseSender(String id, HttpExchange h, int resultCode, byte[] data) {
        this.data = data;
        this.id = id;
        try {
            h.sendResponseHeaders(resultCode, data.length);
            this.out = h.getResponseBody();
        } catch (Exception e) {
            log.error("Error sending response header: " + id, e);
        }
        senderThread = new Thread(this);
        senderThread.start();
        ReponseGuard reponseGuard = new ReponseGuard();
        new Thread(reponseGuard).start();
    }

    public ResponseSender(HttpExchange h, byte[] data, int resultCode) {
        this.data = data;
        this.id = "void";
        try {
            this.out = h.getResponseBody();
        } catch (Exception e) {
            log.error("Error sending response header: " + id, e);
        }
        senderThread = new Thread(this);
        senderThread.start();
        ReponseGuard reponseGuard = new ReponseGuard();
        new Thread(reponseGuard).start();
    }

    public ResponseSender(HttpExchange h, JSONObject resp, int resultCode) {
        if (flagLogResponses) {
            log.info("Response:\n" + resp.toString() + "\n");
        }
        try {
            this.data = resp.toString(2).getBytes("UTF-16");
        } catch (Exception e) {
            this.data = resp.toString(2).getBytes();
        }
        this.id = "void";
        try {
            Headers head = h.getResponseHeaders();
            head.add("Access-Control-Allow-Origin", "*");
            head.add("Access-Control-Allow-Credentials", "true");
            head.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            head.add("Access-Control-Allow-Headers",
                     "Origin, X-Requested-With, Content-Type, "
                     + "Accept, Accept-Encoding, Host, Api-Token, X-Auth-Method, X-HTTP-Method-Override");
            head.add("Access-Control-Expose-Headers", "Response-Time");
            head.add("Content-Type", "application/json; charset=utf-16");
            h.sendResponseHeaders(resultCode, data.length);
            this.out = h.getResponseBody();
        } catch (Exception e) {
            log.error("Error sending response header: " + id, e);
        }
        senderThread = new Thread(this);
        senderThread.start();
        ReponseGuard reponseGuard = new ReponseGuard();
        new Thread(reponseGuard).start();
    }

    public ResponseSender(HttpExchange h, StringBuilder sb, int resultCode) {
        if (flagLogResponses) {
            log.info("Response:\n" + sb.toString()+ "\n");
        }
        try {
            this.data = sb.toString().getBytes("UTF-16");
        } catch (Exception e) {
            this.data = sb.toString().getBytes();
        }
        this.id = "void";
        try {
            Headers head = h.getResponseHeaders();
            head.add("Access-Control-Allow-Origin", "*");
            head.add("Access-Control-Allow-Credentials", "true");
            head.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            head.add("Access-Control-Allow-Headers",
                     "Origin, X-Requested-With, Content-Type, "
                     + "Accept, Accept-Encoding, Host, Api-Token, X-Auth-Method, X-HTTP-Method-Override");
            head.add("Access-Control-Expose-Headers", "Response-Time");
            head.add("Content-Type", "application/json; charset=utf-16");
            h.sendResponseHeaders(resultCode, data.length);
            this.out = h.getResponseBody();
        } catch (Exception e) {
            log.error("Error sending response header: " + id, e);
        }
        senderThread = new Thread(this);
        senderThread.start();
        ReponseGuard reponseGuard = new ReponseGuard();
        new Thread(reponseGuard).start();
    }

    public void run() {
        try {
            if (out == null || data == null || data.length == 0) {
                log.info("ResponseSender: no data #" + id);
            } else {
                int sentBytes = 0;
                int totalBytes = data.length;
                while (sentBytes < totalBytes && !this.interrupted) {
                    int
                        block =
                        totalBytes - sentBytes <= DEFAULT_BLOCK_SIZE ? totalBytes - sentBytes : DEFAULT_BLOCK_SIZE;
                    out.write(data, sentBytes, block);
                    sentBytes += block;
                }
            }
        } catch (Exception e) {
            log.error("ResponseSender: write error #" + id);
        }
        if (this.interrupted) {
            log.info("ResponseSender: interrupt #< " + id);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            log.error("ResponseSender: close error #" + id);
        }
    }

}
