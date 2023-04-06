package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.Class;

public class JsonJbamConverter {

    // see: https://github.com/stleary/JSON-java/issues/674
    public static <T> List<T> JSONArrayToList(JSONArray jsonArray, Class<T> cast) {
        List<T> results = new ArrayList<>(jsonArray.length());
        for (Object element : jsonArray) {
            if (JSONObject.NULL.equals(element)) {
                results.add(null);
            } else if (cast.isAssignableFrom(element.getClass())) {
                results.add(cast.cast(element));
            } else {
                results.add(null);
            }
        }
        return results;
    }

    // public static <T> T[] JSONArrayToArray(JSONArray jsonArray, Class<T> cast) {
    // T[] results = new T[jsonArray.length()];
    // for (int k = 0; k < jsonArray.length(); k++) {
    // Object element = jsonArray.get(k);

    // if (JSONObject.NULL.equals(element)) {
    // results[k] = null;
    // } else if (cast.isAssignableFrom(element.getClass())) {
    // results[k] = cast.cast(element);
    // } else {
    // results[k] = null;
    // }
    // }
    // return results;
    // }

    public static double[] JSONArrayToDoubleArray(JSONArray jsonArray) {
        int n = jsonArray.length();
        double[] results = new double[n];
        for (int k = 0; k < n; k++) {
            results[k] = jsonArray.optDouble(k, Double.NaN);
        }
        return results;
    }

    public static JSONObject toJSON(PredictionInput predictionInput) {
        JSONObject jsonPredictionInput = new JSONObject();
        List<double[]> dataColumn = predictionInput.getDataColumns();
        JSONArray jsonDataColumn = new JSONArray(dataColumn);
        jsonPredictionInput.put("name", predictionInput.getName());
        jsonPredictionInput.put("dataColumn", jsonDataColumn);
        return jsonPredictionInput;
    }

    public static PredictionInput toPredictionInput(JSONObject jsonPredictionInput) {
        // try {

        String name = jsonPredictionInput.optString("name");
        JSONArray jsonDataColumn = jsonPredictionInput.optJSONArray("dataColumn");
        List<double[]> dataColumn = new ArrayList<>();
        if (jsonDataColumn != null) {
            List<JSONArray> jsonArrayList = JSONArrayToList(jsonDataColumn, JSONArray.class);
            for (JSONArray col : jsonArrayList) {
                dataColumn.add(JSONArrayToDoubleArray(col));
            }
        }
        return new PredictionInput(name, dataColumn);
    }

    public static JSONObject toJSON(PredictionOutput predictionOutput) {
        JSONObject json = new JSONObject();
        json.put("name", predictionOutput.getName());
        json.put("structuralError", predictionOutput.getSructuralError());
        json.put("transpose", predictionOutput.getTranspose());
        json.put("createEnvelop", predictionOutput.getCreateEnvelop());
        return json;
    }

    public static PredictionOutput toPredictionOutput(JSONObject json) {
        String name = json.optString("name");
        boolean structuralError = json.optBoolean("structuralError");
        boolean transpose = json.optBoolean("transpose");
        boolean createEnvelop = json.optBoolean("createEnvelop");
        return new PredictionOutput(name, structuralError, transpose, createEnvelop);
    }

    public static JSONObject toJSON(PredictionConfig predictionConfig) {
        JSONObject json = new JSONObject();

        json.put("name", predictionConfig.getName());

        JSONArray predInputs = new JSONArray();
        for (PredictionInput predInput : predictionConfig.getPredictionInputs()) {
            predInputs.put(toJSON(predInput));
        }
        json.put("predictionInputs", predInputs);

        JSONArray predOutputs = new JSONArray();
        for (PredictionOutput predOutput : predictionConfig.getPredictionOutputs()) {
            predOutputs.put(toJSON(predOutput));
        }
        json.put("predictionOutputs", predOutputs);

        JSONArray predStates = new JSONArray();
        for (PredictionOutput predState : predictionConfig.getPredictionStates()) {
            predStates.put(toJSON(predState));
        }
        json.put("predictionStates", predStates);

        json.put("propagateParametricUncertainty", predictionConfig.getPropagateParametricUncertainty());
        json.put("printProgress", predictionConfig.getPrintProgress());
        json.put("nPriorReplicates", predictionConfig.getNPriorReplicates());

        return json;
    }

    public static PredictionConfig toPredictionConfig(JSONObject json) {
        String name = json.optString("name");

        List<PredictionInput> predInputsList = JSONArrayToList(json.optJSONArray("predictionInputs"),
                PredictionInput.class);
        PredictionInput[] predInputsArray = new PredictionInput[predInputsList.size()];
        for (int k = 0; k < predInputsList.size(); k++) {
            predInputsArray[k] = predInputsList.get(k);
        }

        List<PredictionOutput> predOutputsList = JSONArrayToList(json.optJSONArray("predictionOutputs"),
                PredictionOutput.class);
        PredictionOutput[] predOutputsArray = new PredictionOutput[predOutputsList.size()];
        for (int k = 0; k < predOutputsList.size(); k++) {
            predOutputsArray[k] = predOutputsList.get(k);
        }

        List<PredictionOutput> predStatesList = JSONArrayToList(json.optJSONArray("predictionStates"),
                PredictionOutput.class);
        PredictionOutput[] predStatesArray = new PredictionOutput[predStatesList.size()];
        for (int k = 0; k < predStatesList.size(); k++) {
            predStatesArray[k] = predStatesList.get(k);
        }

        boolean propagateParametricUncertainty = json.optBoolean("propagateParametricUncertainty");
        boolean printProgress = json.optBoolean("printProgress");
        int nPriorReplicates = json.optInt("nPriorReplicates");

        return new PredictionConfig(
                name,
                predInputsArray,
                predOutputsArray,
                predStatesArray,
                propagateParametricUncertainty,
                printProgress,
                nPriorReplicates);

    }

    // FIXME: to code!
    public static JSONObject toJSON(PredictionResult predictionResult) {
        JSONObject json = new JSONObject();
        // String workspace, PredictionConfig predictionConfig
        // json.put("workspace", predictionResult.getWorkspace());
        return json;
    }

    // FIXME: to code!
    public static PredictionResult toPredictionResult(JSONObject json) {
        return new PredictionResult("", null);
    }

}
