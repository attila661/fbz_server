package com.mdb;

import static com.mdb.ServerMain.endPoints;
import static com.mdb.ServerMain.queueProcessors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class HandlerMain implements HttpHandler {

    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getHttpContext().getPath();
            QueueProcessor qp = queueProcessors.get(endPoints.get(path));
            synchronized (qp.requests) {
                qp.requests.add(h);
                qp.queueEvent.signal();
                //log.info(qp.name + " queue size: " + qp.requests.size());
            }
        } catch (Exception e) {
            log.error("HandlerMain problem", e);
        }
    }

}
