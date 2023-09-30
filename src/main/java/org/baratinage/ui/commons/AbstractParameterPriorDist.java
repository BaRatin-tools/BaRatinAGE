package org.baratinage.ui.commons;

import javax.swing.Icon;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractParameterPriorDist {

    public abstract void setIcon(Icon icon);

    public abstract void setSymbolUnitLabels(String symbol, String unit);

    public abstract void setNameLabel(String name);

    public abstract void setLocalLock(boolean locked);

    public abstract void setGlobalLock(boolean locked);

    public abstract void configure(boolean isLocked, Parameter parameter);

    public abstract boolean isLocked();

    public abstract Parameter getParameter();

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        Parameter p = getParameter();

        if (p != null) {
            JSONObject parameterJSON = new JSONObject();

            parameterJSON.put("name", p.name);
            parameterJSON.put("initalGuess", p.initalGuess);
            parameterJSON.put("distributionBamName", p.distribution.distribution.bamName);
            JSONArray paramValues = new JSONArray();
            int nParam = p.distribution.parameterValues.length;
            for (int k = 0; k < nParam; k++) {
                paramValues.put(k, p.distribution.parameterValues[k]);
            }
            parameterJSON.put("parameters", paramValues);

            json.put("parameter", parameterJSON);
        }

        json.put("isLocked", isLocked());

        return json;
    }

    public void fromJSON(JSONObject json) {
        Parameter p = null;
        if (json.has("parameter")) {
            JSONObject parameterJSON = json.getJSONObject("parameter");
            JSONArray paramValues = parameterJSON.getJSONArray("parameters");
            int nParam = paramValues.length();
            double[] parameterValues = new double[nParam];
            for (int k = 0; k < nParam; k++) {
                parameterValues[k] = paramValues.getDouble(k);
            }
            String distributionBamName = parameterJSON.getString("distributionBamName");
            Distribution d = Distribution.buildDistributionFromBamName(distributionBamName, parameterValues);
            p = new Parameter(parameterJSON.getString("name"), parameterJSON.getDouble("initalGuess"), d);
        }
        boolean locked = json.getBoolean("isLocked");
        configure(locked, p);
    }
}
