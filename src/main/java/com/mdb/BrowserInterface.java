package com.mdb;

import org.json.JSONObject;

public interface BrowserInterface {

    void getLines(int count, int cursor, int shift, int before, int after,
                  int index, JSONObject keys, StringBuilder output) throws Exception;

    void getViewInfo(StringBuilder output);

}
