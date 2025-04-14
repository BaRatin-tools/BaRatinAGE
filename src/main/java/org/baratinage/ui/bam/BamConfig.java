package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.PredictionState;
import org.json.JSONArray;
import org.json.JSONObject;

public class BamConfig {
    public final BamItemType TYPE;
    public final int VERSION;
    public final List<String> FILE_PATHS;
    public final JSONObject JSON;

    public BamConfig(JSONObject config, List<String> filePaths) {
        this(config, filePaths.toArray(new String[filePaths.size()]));
    }

    public BamConfig(JSONObject config, String... filePaths) {
        FILE_PATHS = new ArrayList<>();
        for (String fp : filePaths) {
            FILE_PATHS.add(fp);
        }
        JSON = config;
        int version = -1;
        if (config.has("_version")) {
            version = config.getInt("_version");
        }
        VERSION = version;
        JSON.put("_version", VERSION);
        BamItemType type = null;
        if (config.has("_bamItemType")) {
            type = BamItemType.getBamItemType(config.getString("_bamItemType"));
        }
        TYPE = type;
        if (type != null) {
            JSON.put("_bamItemType", TYPE.id);
        }
    }

    public BamConfig(int version) {
        this(version, null);
    }

    public BamConfig(int version, BamItemType type) {
        FILE_PATHS = new ArrayList<>();
        JSON = new JSONObject();
        VERSION = version;
        JSON.put("_version", VERSION);
        TYPE = type;
        if (TYPE != null) {
            JSON.put("_bamItemType", TYPE.id);
        }
    }

    private static <A> JSONArray toJSONArray(A[] objects, Function<A, JSONObject> transformer) {
        JSONArray jsonArray = new JSONArray();
        for (A o : objects) {
            jsonArray.put(transformer.apply(o));
        }
        return jsonArray;
    }

    private static JSONArray toJSONArray(String[] strs) {
        JSONArray jsonArray = new JSONArray();
        if (strs != null) {
            for (String s : strs) {
                jsonArray.put(s);
            }
        }
        return jsonArray;
    }

    private static JSONArray toJSONArray(double[] doubles) {
        JSONArray jsonArray = new JSONArray();
        for (Double d : doubles) {
            jsonArray.put(d);
        }
        return jsonArray;
    }

    public static JSONObject getConfig(IModelDefinition modelDefinition) {
        JSONObject json = new JSONObject();
        json.put("modelId", modelDefinition.getModelId());
        json.put("inputNames", toJSONArray(modelDefinition.getInputNames()));
        json.put("outputNames", toJSONArray(modelDefinition.getOutputNames()));
        json.put("nParameters", modelDefinition.getNumberOfParameters());
        json.put("xTra", modelDefinition.getXtra(""));
        return json;
    }

    public static JSONObject getConfig(IPriors priors) {
        JSONArray jsonParameterArray = new JSONArray();
        Parameter[] parameters = priors.getParameters();
        if (parameters != null) {
            for (Parameter p : parameters) {
                if (p != null) {
                    jsonParameterArray.put(getConfig(p));
                }
            }
        }
        JSONObject json = new JSONObject();
        json.put("parameters", jsonParameterArray);
        return json;
    }

    private static JSONObject getConfig(Parameter parameter) {
        JSONObject json = new JSONObject();
        json.put("name", parameter.name);
        json.put("initalGuess", parameter.initalGuess);
        json.put("distribution", getConfig(parameter.distribution));
        return json;
    }

    private static JSONObject getConfig(Distribution distribution) {
        JSONObject json = new JSONObject();
        json.put("distributionType", getConfig(distribution.type));
        json.put("parameterValues", toJSONArray(distribution.parameterValues));
        return json;
    }

    private static JSONObject getConfig(DistributionType distributionType) {
        JSONObject json = new JSONObject();
        json.put("bamName", distributionType.bamName);
        json.put("parameterNames", toJSONArray(distributionType.parameterNames));
        return json;
    }

    public static JSONObject getConfig(IPredictionMaster predictionMaster) {
        JSONObject json = new JSONObject();
        PredExpSet predExpSet = predictionMaster.getPredExps();
        if (predExpSet != null) {
            json.put("predExpSet", toJSONArray(predExpSet.predExperiments, BamConfig::getConfig));
        }
        return json;
    }

    private static JSONObject getConfig(PredExp predExp) {
        return getConfig(predExp.predConfig);
    }

    private static JSONObject getConfig(PredictionConfig predictionConfig) {
        JSONObject json = new JSONObject();
        json.put("spagFileName", predictionConfig.predictionConfigFileName);
        json.put("inputs", toJSONArray(predictionConfig.inputs, BamConfig::getConfig));
        json.put("outputs", toJSONArray(predictionConfig.outputs, BamConfig::getConfig));
        json.put("states", toJSONArray(predictionConfig.states, BamConfig::getConfig));
        json.put("propagateParametricUncertainty", predictionConfig.propagateParametricUncertainty);
        json.put("printProgress", predictionConfig.printProgress);
        json.put("nPriorReplicates", predictionConfig.nPriorReplicates);
        return json;
    }

    private static JSONObject getConfig(PredictionState state) {
        JSONObject json = new JSONObject();
        json.put("spagFileName", state.spagFileName);
        json.put("envFileName", state.envFileName);
        json.put("structuralError", state.structuralError);
        json.put("transpose", state.transpose);
        json.put("createEnvelop", state.createEnvelop);
        return json;
    }

    private static JSONObject getConfig(PredictionOutput output) {
        JSONObject json = new JSONObject();
        json.put("spagFileName", output.spagFileName);
        json.put("envFileName", output.envFileName);
        json.put("structuralError", output.structuralError);
        json.put("transpose", output.transpose);
        json.put("createEnvelop", output.createEnvelop);
        return json;
    }

    private static JSONObject getConfig(PredictionInput input) {
        JSONArray jsonData = new JSONArray();
        for (double[] column : input.dataColumns) {
            jsonData.put(toJSONArray(column));
        }
        JSONObject json = new JSONObject();
        json.put("fileName", input.fileName);
        json.put("nObs", input.nObs);
        json.put("nSpag", input.nSpag);
        return json;
    }

}
