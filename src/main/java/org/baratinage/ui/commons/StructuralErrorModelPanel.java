package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.lg.Lg;
import org.json.JSONArray;

public class StructuralErrorModelPanel extends GridPanel {

    private static final ImageIcon lockIcon = SvgIcon.buildFeatherAppImageIcon(
            "lock.svg", AppConfig.AC.ICON_SIZE * 0.8f);
    private final List<ParameterPriorDist> parameters;

    public StructuralErrorModelPanel() {
        setColWeight(3, 1);
        setGap(5);
        setPadding(5);

        JLabel initialGuessLabel = new JLabel();
        initialGuessLabel.setText("Valeure initiale");

        JLabel distributionLabel = new JLabel();
        distributionLabel.setText("Distribution");

        JLabel distributionParametersLabel = new JLabel();
        distributionParametersLabel.setText("Paramètres de la distribution");

        JLabel lockLabel = new JLabel();
        lockLabel.setIcon(lockIcon);

        JSeparator initialGuessSep = new JSeparator(JSeparator.HORIZONTAL);
        JSeparator distributionSep = new JSeparator(JSeparator.HORIZONTAL);
        JSeparator parametersSep = new JSeparator(JSeparator.HORIZONTAL);
        JSeparator lockSep = new JSeparator(JSeparator.HORIZONTAL);

        insertChild(initialGuessLabel, 1, 0);
        insertChild(distributionLabel, 2, 0);
        insertChild(distributionParametersLabel, 3, 0);
        insertChild(lockLabel, 4, 0);

        insertChild(initialGuessSep, 1, 1);
        insertChild(distributionSep, 2, 1);
        insertChild(parametersSep, 3, 1);
        insertChild(lockSep, 4, 1);

        Lg.register(this, () -> {
            initialGuessLabel.setText(Lg.text("initial_guess"));
            distributionLabel.setText(Lg.text("distribution"));
            distributionParametersLabel.setText(Lg.text("distribution_parameters"));
        });

        parameters = new ArrayList<>();

    }

    public void addParameter(String symbol, String unit, double initialGuess) {
        addParameter(symbol, unit, DistributionType.UNIFORM, initialGuess, 0, 10000);
    }

    public void addParameter(String symbol, String unit, DistributionType distribution, double initialGuess,
            double... parameterValues) {

        int index = parameters.size() + 1;
        ParameterPriorDist gamma = new ParameterPriorDist();
        gamma.setNameLabel("");
        gamma.setSymbolUnitLabels(symbol, unit);
        gamma.setLocalLock(true);
        parameters.add(gamma);

        insertChild(gamma.symbolUnitLabel, 0, index);
        insertChild(gamma.initialGuessField, 1, index);
        insertChild(gamma.distributionField.distributionCombobox, 2, index);
        insertChild(gamma.distributionField.parameterFieldsPanel, 3, index);
        insertChild(gamma.lockCheckbox, 4, index);

        gamma.setDistributionType(DistributionType.UNIFORM);
        int nPars = parameterValues.length;
        Double[] nonPrimitiveParamValues = new Double[nPars];
        for (int k = 0; k < nPars; k++) {
            nonPrimitiveParamValues[k] = parameterValues[k];
        }
        gamma.setDistributionParameters(nonPrimitiveParamValues);
        gamma.setInitialGuess(initialGuess);
    }

    public Parameter[] getParameters() {
        int nPar = parameters.size();
        Parameter[] pars = new Parameter[nPar];
        for (int k = 0; k < nPar; k++) {
            pars[k] = parameters.get(k).getParameter();
        }
        return pars;
    }

    public JSONArray toJSON() {
        JSONArray json = new JSONArray();
        int nPar = parameters.size();
        for (int k = 0; k < nPar; k++) {
            json.put(k, parameters.get(k).toJSON());
        }
        return json;
    }

    public void fromJSON(JSONArray json) {
        int nPar = json.length();
        parameters.clear();
        for (int k = 0; k < nPar; k++) {
            ParameterPriorDist ppd = new ParameterPriorDist();
            parameters.add(ppd);
            ppd.fromJSON(json.getJSONObject(k));
        }
    }

}
