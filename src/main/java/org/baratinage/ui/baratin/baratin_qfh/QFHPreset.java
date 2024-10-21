package org.baratinage.ui.baratin.baratin_qfh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public record QFHPreset(String id,
        String formula,
        String stageSymbole,
        List<QFHPresetParameter> parameters) {

    public record QFHPresetParameter(
            String symbole,
            String type,
            QFHPresetDistribution distribution) {
    }

    public record QFHPresetDistribution(
            double initial_guess,
            String distribution_id,
            Double[] parameters) {

    }

    static public final List<QFHPreset> PRESETS = new ArrayList<>();

    static {
        try {
            String jsonContent = ReadFile.readTextFile("resources/baratin_qfh_presets.json");
            JSONArray json = new JSONArray(jsonContent);
            for (int i = 0; i < json.length(); i++) {
                JSONObject presetJson = json.getJSONObject(i);
                JSONArray presetParametersJson = presetJson.getJSONArray("parameters");
                List<QFHPresetParameter> presetParameters = new ArrayList<>();
                for (int j = 0; j < presetParametersJson.length(); j++) {
                    JSONObject presetParJson = presetParametersJson.getJSONObject(j);

                    JSONObject presetParDistJson = presetParJson.optJSONObject("default_distribution");
                    QFHPresetDistribution presetDistribution = null;
                    if (presetParDistJson != null) {
                        JSONArray arr = presetParDistJson.getJSONArray("parameters");
                        Double[] parameters = new Double[arr.length()];
                        for (int k = 0; k < parameters.length; k++) {
                            parameters[k] = arr.optDouble(k);
                        }
                        presetDistribution = new QFHPresetDistribution(
                                presetParDistJson.getDouble("initial_guess"),
                                presetParDistJson.getString("type"),
                                parameters);
                    }
                    presetParameters.add(new QFHPresetParameter(
                            presetParJson.getString("symbole"),
                            presetParJson.optString("type"),
                            presetDistribution));
                }
                PRESETS.add(new QFHPreset(
                        presetJson.getString("id"),
                        presetJson.getString("formula"),
                        presetJson.getString("stage_symbole"), presetParameters));
            }
        } catch (IOException | JSONException e) {
            ConsoleLogger.error(e);
        }
    }

}
