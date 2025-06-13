package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public record ShiftDetectionConfig(
        JSONObject config,
        List<String> filePaths,
        List<ShiftDetectionConfig> children) {

    public ShiftDetectionConfig(JSONObject config) {
        this(
                config.getJSONObject("config"),
                new ArrayList<>(),
                new ArrayList<>());
        JSONArray arr = config.getJSONArray("children");
        for (int k = 0; k < arr.length(); k++) {
            ShiftDetectionConfig rcdc = new ShiftDetectionConfig(arr.getJSONObject(k));
            children.add(rcdc);
        }
    }

    public JSONObject getFullConfig() {
        JSONObject json = new JSONObject();
        json.put("config", config);
        JSONArray arr = new JSONArray();
        for (ShiftDetectionConfig child : children) {
            arr.put(child.getFullConfig());
        }
        json.put("children", arr);
        return json;
    }

    public List<String> getAllFilePaths() {
        List<String> paths = new ArrayList<>();
        paths.addAll(filePaths);
        for (ShiftDetectionConfig child : children) {
            paths.addAll(child.getAllFilePaths());
        }
        return paths;
    }
}
