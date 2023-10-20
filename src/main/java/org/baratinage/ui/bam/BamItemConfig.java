package org.baratinage.ui.bam;

import org.json.JSONArray;
import org.json.JSONObject;

public record BamItemConfig(JSONObject jsonObject, String... filePaths) {

    public BamItemConfig addPaths(String... filePathsToAdd) {
        String[] oldFilePaths = filePaths();
        int nOld = oldFilePaths.length;
        int nAdd = filePathsToAdd.length;
        int nNew = nOld + nAdd;
        String[] newFilePaths = new String[nNew];
        int k = 0;
        for (String old : oldFilePaths) {
            newFilePaths[k] = old;
            k++;
        }
        for (String add : filePathsToAdd) {
            newFilePaths[k] = add;
            k++;
        }
        return new BamItemConfig(jsonObject(), newFilePaths);
    }

    public static JSONObject toJSON(BamItemConfig bamItemBackup) {
        JSONObject json = new JSONObject();
        json.put("jsonObject", bamItemBackup.jsonObject());
        String[] filePaths = bamItemBackup.filePaths();
        if (filePaths != null) {
            JSONArray filePathsJson = new JSONArray(filePaths);
            json.put("filePaths", filePathsJson);
        }
        return json;
    }

    public static BamItemConfig fromJSON(JSONObject json) {
        JSONObject jsonObject = json.getJSONObject("jsonObject");
        String[] filePaths = new String[0];
        if (json.has("filePaths")) {
            JSONArray filePathsJson = json.getJSONArray("filePaths");
            int n = filePathsJson.length();
            filePaths = new String[n];
            for (int k = 0; k < n; k++) {
                filePaths[k] = filePathsJson.getString(k);
            }
        }
        return new BamItemConfig(jsonObject, filePaths);
    }
}
