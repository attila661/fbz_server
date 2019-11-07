package com.mdb;

import static com.mdb.ServerMain.defaultCharset;
import static com.mdb.ServerMain.mutexSid;
import static com.mdb.ServerMain.sessionCounter;
import static com.mdb.ServerMain.sidMap;
import static com.mdb.ServerMain.tables;
import static com.mdb.Utilities.postedJson;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class HandlerLogin implements HttpHandler {

    private JSONObject out = new JSONObject();
    private int resultCode;

    public void handle(HttpExchange h) throws IOException {
        do {
            try {
                JSONObject posted = postedJson(h, defaultCharset, 128, "/login");
                String user;
                String passwd;
                try {
                    user = posted.getString("user").trim();
                    passwd = posted.getString("password").trim();
                } catch (Exception e) {
                    out.put("status", "ERROR");
                    out.put("message", "unable to parse user/password from message");
                    resultCode = 400;
                    break;
                }

                if (!isSuccesfulLogin(user, passwd, out)) {
                    break;
                }

                SessionInfo si = new SessionInfo();
                mutexSid.lock();
                si.started = System.currentTimeMillis();
                si.lastActivity = si.started;
                si.user = user;
                si.sid = si.started + "#" + (++sessionCounter);
                sidMap.put(si.sid, si);
                mutexSid.unlock();

                getMenu(out);

                //tables.get("T1").saveToFile();

                out.put("sid", si.sid);
                out.put("status", "OK");
                out.put("message", "successful login");

                resultCode = 200;
            } catch (Exception e) {
                out.put("status", "ERROR");
                out.put("message", e.getMessage());
                resultCode = 400;
            }
        } while (false);
        new ResponseSender(h, out, resultCode);
    }

    private boolean isSuccesfulLogin(String user, String passwd, JSONObject out) {

        //todo users/passwords
        if (!user.equals("admin") || !passwd.equals("admin")) {
            out.put("status", "ERROR");
            out.put("message", "access denied");
            resultCode = 403;
            return false;
        }

        out.put("firstname", "Admin");
        out.put("lastname", "Administrator");
        return true;
    }

    private void getMenu(JSONObject out) throws Exception {

        ReportingSnapshot reportingSnapshot = ReportingSnapshot.getsnapshot(Arrays.asList(
            "menu!name",
            "menu!parent",
            "menu!title",
            "menu!icon",
            "menu!action",
            "menu#1",
            "menu?"
        ));

        String[] name = (String[]) reportingSnapshot.data.get("menu!name");
        String[] parent = (String[]) reportingSnapshot.data.get("menu!parent");
        String[] title = (String[]) reportingSnapshot.data.get("menu!title");
        String[] icon = (String[]) reportingSnapshot.data.get("menu!icon");
        String[] action = (String[]) reportingSnapshot.data.get("menu!action");
        int size = reportingSnapshot.sizes.get("menu?");

        JSONArray menu = getChilds("", name, title, parent, icon, action, size);
        out.put("menu", menu);

    }

    private static JSONArray getChilds(String p, String[] name, String[] title, String[] parent,
                                       String[] icon, String[] action, int size) {
        JSONArray result = new JSONArray();
        for (int i = 1; i <= size; i++) {
            if (name[i].length() > 0) {
                if (p.equals(parent[i])) {
                    JSONObject item = new JSONObject();
                    item.put("name", name[i]);
                    item.put("title", title[i]);
                    item.put("icon", icon[i]);
                    item.put("action", action[i]);
                    JSONArray childs = getChilds(name[i], name, title, parent, icon, action, size);
                    if (childs.length() > 0) {
                        item.put("children", childs);
                    }
                    name[i] = "";
                    result.put(item);
                }
            }
        }
        return result;
    }

}
