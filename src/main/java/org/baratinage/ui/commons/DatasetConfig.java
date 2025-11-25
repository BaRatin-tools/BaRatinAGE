package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatasetConfig {
    public final String name;
    public final String hashString;
    public final String[] headers;
    public final String filePath;

    public DatasetConfig(String name,
            String hashString,
            String[] headers,
            String filePath) {
        this.name = name;
        this.hashString = hashString;
        this.headers = headers;
        this.filePath = filePath;
    }

    public String[] getAllFilePaths() {
        List<String> filePaths = new ArrayList<>();
        if (filePath != null) {
            filePaths.add(filePath);
        }
        return filePaths.toArray(new String[filePaths.size()]);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("hashString", hashString);
        json.put("headers", new JSONArray(headers));
        json.put("filePath", filePath);
        return json;
    }
}
