package com.mdb;

import static com.mdb.SandBox.sandboxMain;
import static com.mdb.Utilities.readFileToStringHun;
import static com.mdb.Utilities.sleepMillisec;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ServerMain {

    public static boolean flagLogRequests = true;
    public static boolean flagLogResponses = true;
    public static boolean flagSidChecking = true;

    public static String workdir = "./workdir";

    public static final int DEFAULT_PORT = 4080;
    public static volatile boolean shutDownFlag = false;

    //public static final String defaultCharsetdefaultCharset = "ISO-8859-1";
    public static final String defaultCharset = "UTF-8";

    public static Map<String, String> endPoints = new HashMap<>();
    public static Map<String, QueueProcessor> queueProcessors = new HashMap<>();

    public Map<Long, Screen> mainScreens = new TreeMap<>();

    public static HttpServer service = null;

    public static HashMap<String, SessionInfo> sidMap = new HashMap<>();
    public static int sessionCounter = 0;
    public static final Lock mutexSid = new ReentrantLock(true);
    public static final Lock mutexData = new ReentrantLock(true);

    public static int tdbSize = 1000;
    public static int tdbMaxSize = 2000;
    public static int[] tdbData = new int[tdbMaxSize + 1];

    public static Map<String, BrowserInterface> browsers = new TreeMap<>();
    public static Map<String, DatabaseTable> tables = new TreeMap<>();


    public static Map<String, Map<String, HttpHandler>> queueDefinitionsPublic =
        new TreeMap<String, Map<String, HttpHandler>>() {
            {
                put("Default", new HashMap<String, HttpHandler>() {
                    {
                        put("/quicksearch", new HandlerQuickSearch());
                        put("/request", new HandlerRequest());
                        put("/browse", new HandlerBrowse());
                        put("/browserinfo", new HandlerBrowserInfo());
                        //put("/getscreen", new HandlerBrowserInfo());
                        put("/login", new HandlerLogin());
                    }
                });
                put("LowPriority", new HashMap<String, HttpHandler>() {
                    {
                        put("/report", new HandlerReport());
                    }
                });
                put("HighPriority", new HashMap<String, HttpHandler>() {
                    {
                        put("/ping", new HandlerPing());
                        put("/v2ping", new HandlerV2Ping());
                    }
                });
            }
        };

    public static void main(String[] args) {
        log.info("Starting ServerMain");

        //todo - hardcoded testdata
        for (int i = 1; i <= tdbSize; i++) {
            tdbData[i] = i;
        }

        try {
            initConfiguration();
            AnsiHun.init();

            sandboxMain(args);

            initQueues();
            initThreads();
            initHandlers(args);
        } catch (Exception e) {
            log.error("Unable to start server", e);
            System.exit(1);
        }
        while (shutDownFlag == false) {
            sleepMillisec(10);
        }
        log.info("Starting ServerMain");
    }

    private static void initConfiguration() throws Exception {
        JSONObject config = new JSONObject(readFileToStringHun("config/_config.inf"));
        for (String key : config.keySet()) {
            switch (key) {
                case "flagLogRequests":
                    flagLogRequests = config.getBoolean(key);
                    break;
                case "flagLogResponses":
                    flagLogResponses = config.getBoolean(key);
                    break;
                case "flagSidChecking":
                    flagSidChecking = config.getBoolean(key);
                    break;
                case "data":
                    workdir = config.getString(key);
                    break;
                default:
                    log.warn("Unknown config parameter: " + key);
            }
        }
    }

    private static void initThreads() {
        queueProcessors.entrySet().stream().forEach(q -> {
            q.getValue().currentThread = new Thread(q.getValue());
            q.getValue().currentThread.start();
        });
    }

    private static void initQueues() {
        log.info("Setup Queue Processing");
        queueDefinitionsPublic.entrySet().stream()
            .forEach(q -> q.getValue().entrySet().stream()
                .forEach(e -> {
                    endPoints.put(e.getKey(), q.getKey());
                    log.info(String.format("QueueDefinition: %20s -> %-20s", q.getKey(), e.getKey()));
                }));
        if (queueDefinitionsPublic.values().stream().mapToInt(e -> e.size()).sum() != endPoints.size()) {
            log.error("Error validating queue definitions, duplicate endpoints");
            throw new IllegalStateException();
        }
        queueDefinitionsPublic.entrySet().stream()
            .forEach(q -> queueProcessors.put(q.getKey(), new QueueProcessor(q.getKey(), q.getValue())));
    }

    public static void initHandlers(String[] args) {
        log.info("Setup Handlers");
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            port = DEFAULT_PORT;
            log.warn("Unable to read optional port number command line argument, using default");
        }
        log.info("Starting service at port: " + port);
        try {
            service = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            log.error("Can not create server at " + port, e);
            throw new IllegalStateException();
        }

        HandlerMain hm = new HandlerMain();
        endPoints.keySet().stream().forEach(e -> service.createContext(e, hm));
        service.start();
    }

}
