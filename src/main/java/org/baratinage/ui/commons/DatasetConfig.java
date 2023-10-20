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
    public final List<DatasetConfig> nested;

    public DatasetConfig(String name,
            String hashString,
            String[] headers,
            String filePath,
            DatasetConfig... nested) {
        this.name = name;
        this.hashString = hashString;
        this.headers = headers;
        this.filePath = filePath;
        this.nested = new ArrayList<>();
        for (DatasetConfig adc : nested) {
            this.nested.add(adc);
        }
    }

    public String[] getAllFilePaths() {
        List<String> filePaths = new ArrayList<>();
        if (filePath != null) {
            filePaths.add(filePath);
        }
        for (DatasetConfig dc : nested) {
            String[] nestedFilePaths = dc.getAllFilePaths();
            for (String nestedFilePath : nestedFilePaths) {
                filePaths.add(nestedFilePath);
            }
        }
        return filePaths.toArray(new String[filePaths.size()]);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("hashString", hashString);
        json.put("headers", new JSONArray(headers));
        json.put("filePath", filePath);
        if (nested.size() > 0) {
            JSONArray nestedJson = new JSONArray();
            for (int k = 0; k < nested.size(); k++) {
                nestedJson.put(k, nested.get(k).toJSON());
            }
            json.put("nested", nestedJson);
        }
        return json;
    }
}
