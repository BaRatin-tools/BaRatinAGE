package org.baratinage.ui.commons;

import javax.swing.Icon;

import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractParameterPriorDist {

    public abstract void setIcon(Icon icon);

    public abstract void setSymbolUnitLabels(String symbol, String unit);

    public abstract void setNameLabel(String name);

    public abstract void setEnabled(boolean enabled);

    public abstract boolean isEnabled();

    public abstract void setLock(boolean locked);

    public abstract boolean isLocked();

    public abstract Parameter getParameter();

    public abstract DistributionType getDistributionType();

    public abstract void setDistributionType(DistributionType distributionType);

    public abstract Double[] getDistributionParameters();

    public abstract void setDistributionParameters(Double[] values);

    public abstract Double getInitialGuess();

    public abstract void setInitialGuess(Double value);

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();

        DistributionType distributionType = getDistributionType();
        if (distributionType != null) {
            json.put("distributionBamName", distributionType.bamName);
        }

        json.put("initialGuess", getInitialGuess());

        if (distributionType != null) {
            Double[] parameterValues = getDistributionParameters();
            int nPars = parameterValues.length;
            JSONArray parameterValuesJSON = new JSONArray();
            for (int k = 0; k < nPars; k++) {
                parameterValuesJSON.put(k, parameterValues[k]);
            }
            json.put("distributionParameters", parameterValuesJSON);
        }

        json.put("isLocked", isLocked());

        return json;
    }

    public void fromJSON(JSONObject json) {

        if (json.has("distributionBamName")) {
            DistributionType distributionType = DistributionType
                    .getDistribFromBamName(json.getString("distributionBamName"));
            if (distributionType != null) {
                setDistributionType(distributionType);
            }
        }

        if (json.has("initialGuess")) {
            Double initialGuessValue = json.optDouble("initialGuess");
            if (initialGuessValue != null) {
                setInitialGuess(initialGuessValue);
            }
        }

        if (json.has("distributionParameters")) {
            JSONArray distParametersJSON = json.getJSONArray("distributionParameters");
            int nPars = distParametersJSON.length();
            Double[] distParameterValues = new Double[nPars];
            for (int k = 0; k < nPars; k++) {
                double d = distParametersJSON.optDouble(k);
                distParameterValues[k] = Double.isNaN(d) ? null : d;
            }
            setDistributionParameters(distParameterValues);
        }

        boolean locked = json.getBoolean("isLocked");
        setLock(locked);
    }
}
