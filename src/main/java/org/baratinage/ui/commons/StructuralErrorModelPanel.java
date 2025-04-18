package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.translation.T;

import org.json.JSONArray;

public class StructuralErrorModelPanel extends GridPanel implements ChangeListener {

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
        lockLabel.setIcon(AppSetup.ICONS.LOCK);

        int colIndex = 2;
        insertChild(distributionLabel, colIndex, 0);
        colIndex++;
        insertChild(distributionParametersLabel, colIndex, 0);
        colIndex++;
        insertChild(initialGuessLabel, colIndex, 0);
        colIndex++;
        insertChild(lockLabel, colIndex, 0);

        insertChild(new SimpleSep(), 1, 1);
        insertChild(new SimpleSep(), 2, 1);
        insertChild(new SimpleSep(), 3, 1);
        insertChild(new SimpleSep(), 4, 1);

        T.t(this, initialGuessLabel, false, "initial_guess");
        T.t(this, distributionLabel, false, "distribution");
        T.t(this, distributionParametersLabel, false, "distribution_parameters");

        parameters = new ArrayList<>();

    }

    public void addParameter(String symbol, String unit, double initialGuess) {
        addParameter(symbol, unit, DistributionType.UNIFORM, initialGuess, 0, 10000);
    }

    public void addParameter(String symbol, String unit, DistributionType distribution, double initialGuess,
            double... parameterValues) {

        ParameterPriorDist gamma = new ParameterPriorDist("gamma_" + parameters.size());
        gamma.setNameLabel("");
        gamma.setSymbolUnitLabels(symbol, unit);
        gamma.setLocalLock(true);

        gamma.setDistributionType(DistributionType.UNIFORM);

        int nPars = parameterValues.length;
        Double[] nonPrimitiveParamValues = new Double[nPars];
        for (int k = 0; k < nPars; k++) {
            nonPrimitiveParamValues[k] = parameterValues[k];
        }
        gamma.setDistributionParameters(nonPrimitiveParamValues);
        gamma.setInitialGuess(initialGuess);

        addParameter(gamma);

    }

    private void addParameter(ParameterPriorDist parameter) {
        int index = parameters.size() + 2;
        parameters.add(parameter);
        int colIndex = 1;
        insertChild(parameter.symbolUnitLabel, colIndex, index);
        colIndex++;
        insertChild(parameter.distributionField.distributionCombobox, colIndex, index);
        colIndex++;
        insertChild(parameter.distributionField.parameterFieldsPanel, colIndex, index);
        colIndex++;
        insertChild(parameter.initialGuessField, colIndex, index);
        colIndex++;
        insertChild(parameter.lockCheckbox, colIndex, index);

        parameter.addChangeListener(this);

        T.updateHierarchy(this, parameter);
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
        // parameters.clear();
        for (int k = 0; k < nPar; k++) {
            // ParameterPriorDist ppd = new ParameterPriorDist("gamma_" + k);
            // ppd.fromJSON(json.getJSONObject(k));
            // addParameter(ppd);
            if (k < parameters.size()) {
                parameters.get(k).fromJSON(json.getJSONObject(k));
            } else {
                ConsoleLogger.error("number of parameters not matching!");
            }
        }
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeListeners();
    }

}
