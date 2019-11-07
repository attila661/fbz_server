package com.mdb;

import static com.mdb.ServerMain.shutDownFlag;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class QueueProcessor implements Runnable {

    public Map<String, HttpHandler> handlers;
    public String name;
    public ThreadEvent queueEvent = new ThreadEvent();
    public List<HttpExchange> requests = new ArrayList<>();
    public Thread currentThread;

    public QueueProcessor(String name, Map<String, HttpHandler> handlers) {
        this.name = name;
        this.handlers = handlers;
    }

    private boolean waitEvent() {
        try {
            queueEvent.await();
        } catch (Exception e) {
            log.error("Problem in QueueProcessor: " + name + ":waitEvent", e);
        }
        if (shutDownFlag) {
            return true;
        } else {
            return false;
        }
    }

    public void run() {
        Thread.currentThread().setName("QueueProcessor_" + name);
        log.info("QueueProcessor_" + name + " started");
        while (true) {
            if (waitEvent()) {
                break;
            }
            HttpExchange h;
            while (true) {
                synchronized (requests) {
                    if (requests.size() > 0) {
                        h = requests.remove(0);
                    } else {
                        break;
                    }
                }
                try {
                    if (h.getRequestMethod().equals("OPTIONS")) {
                        new ResponseSender(h,new JSONObject(),200);
                        break;
                    }
                    handlers.get(h.getHttpContext().getPath()).handle(h);
                } catch (Exception e) {
                    log.error("Problem in QueueProcessor: " + name + ":run", e);
                }
            }
        }
        log.info("QueueProcessor_" + name + " completed");
    }

}
