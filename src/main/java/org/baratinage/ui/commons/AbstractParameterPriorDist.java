package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.variable_parameter.ParameterLabelsAndFields;
import org.baratinage.ui.commons.variable_parameter.PeriodSet;
import org.baratinage.ui.commons.variable_parameter.PeriodSetsSource;
import org.baratinage.ui.component.EquationLabel;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.SimpleGridPanel;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractParameterPriorDist {

    // constant parameter (the default)
    protected final ParameterLabelsAndFields constantParameter;

    // variable parameter
    protected PeriodSet currentPeriodSet = null;
    protected ChangeListener periodSetChangeListener = l -> onPeriodSetChange();
    protected ChangeListener periodSetsChangeListener = l -> onPeriodSetsChange();
    protected PeriodSetsSource periodSetsSource = null;
    protected final SimpleComboBox periodSetCombobox;
    protected final List<ParameterLabelsAndFields> variableSubParameters;

    // menu
    protected final JButton menuButton = new JButton();
    protected final JPopupMenu contextMenu = new JPopupMenu();
    protected final JCheckBoxMenuItem variableParameterBtn = new JCheckBoxMenuItem();
    protected final JCheckBoxMenuItem lockAllValuesBtn = new JCheckBoxMenuItem();
    protected final JCheckBoxMenuItem autoInitialValueBtn = new JCheckBoxMenuItem();

    // state
    private boolean isVariable = false;
    private boolean isEnabled = true;
    private boolean isLocked = false;

    public AbstractParameterPriorDist() {

        constantParameter = new ParameterLabelsAndFields();

        constantParameter.addChangeListener(l -> fireChangeListeners());

        periodSetCombobox = new SimpleComboBox();

        variableSubParameters = new ArrayList<>();

        menuButton.setIcon(AppSetup.ICONS.MORE_DOTS);

        menuButton.addActionListener((l) -> {
            contextMenu.show(menuButton, 0, menuButton.getHeight());
        });

        constantParameter.field.distributionCombobox.addChangeListener(l -> {
            variableSubParameters
                    .forEach(p -> p.field.setDistributionType(constantParameter.field.getDistributionType()));
        });

        variableParameterBtn.addItemListener(l -> {
            isVariable = variableParameterBtn.isSelected();
            fireLayoutChangeListeners();
        });

        lockAllValuesBtn.addItemListener(l -> {
            isLocked = lockAllValuesBtn.isSelected();
            updateChildrenEnabledStates();
        });
        autoInitialValueBtn.addItemListener(l -> {
            constantParameter.setAutoInitialGuess(autoInitialValueBtn.isSelected());
            variableSubParameters.forEach(p -> p.setAutoInitialGuess(autoInitialValueBtn.isSelected()));
            updateChildrenEnabledStates();
        });

        periodSetCombobox.addChangeListener(l -> {
            if (periodSetsSource == null) {
                setPeriodSet(null);
                return;
            }
            int index = periodSetCombobox.getSelectedIndex();
            if (index == -1) {
                setPeriodSet(null);
                return;
            }
            List<PeriodSet> periodSets = periodSetsSource.getPeriodSets();
            if (index >= periodSets.size()) {
                setPeriodSet(null);
                return;
            }
            setPeriodSet(periodSets.get(index));
        });

        T.t(this, () -> {
            variableParameterBtn.setText(T.text("variable_parameter"));
            lockAllValuesBtn.setText(T.text("parameter_dist_lock"));
            autoInitialValueBtn.setText(T.text("parameter_dist_auto_initial"));
        });

        autoInitialValueBtn.setSelected(true);

    }

    // ************************************************************************
    // period management

    private int getPeriodSetIndex(PeriodSet ps) {
        List<PeriodSet> periodSets = periodSetsSource.getPeriodSets();
        int index = -1;
        for (int k = 0; k < periodSets.size(); k++) {
            if (periodSets.get(k).equals(ps)) {
                index = k;
                break;
            }
        }
        return index;
    }

    private void setPeriodSet(PeriodSet ps) {
        if (ps != null) {
            int periodSetIndex = getPeriodSetIndex(ps);
            if (periodSetIndex == -1) {
                ConsoleLogger.error("Cannot set an unregistered PeriodSet");
                return;
            }
        }
        if (currentPeriodSet != null) {
            currentPeriodSet.removeChangeListener(periodSetChangeListener);
        }
        currentPeriodSet = ps;
        if (currentPeriodSet != null) {
            currentPeriodSet.addChangeListener(periodSetChangeListener);
        }
        onPeriodSetChange();
    }

    public PeriodSet getPeriodSet() {
        return currentPeriodSet;
    }

    private void onPeriodSetChange() {
        // Note: I could trim the existing array here when the number of period
        // int n = currentPeriodSet == null ? 0 : currentPeriodSet.getNumberOfPeriods();
        // while (variableSubParameters.size() > n) {
        // variableSubParameters.remove(variableSubParameters.size() - 1);
        // }
        // current behavior is intentional
        if (currentPeriodSet == null) {
            fireLayoutChangeListeners();
            return;
        }
        for (int k = 0; k < currentPeriodSet.getNumberOfPeriods(); k++) {
            ParameterLabelsAndFields psf = null;
            if (k >= variableSubParameters.size()) {
                psf = new ParameterLabelsAndFields();
                psf.addChangeListener(l -> fireChangeListeners());
                psf.field.setDistributionType(constantParameter.field.getDistributionType());
                psf.field.setParameters(constantParameter.field.getParameters());
                psf.meanField.setValue(constantParameter.meanField.getDoubleValue());
                psf.uncertaintyField.setValue(constantParameter.uncertaintyField.getDoubleValue());
                psf.setAutoInitialGuess(autoInitialValueBtn.isSelected());
                variableSubParameters.add(psf);
            } else {
                psf = variableSubParameters.get(k);
            }
            psf.label.setText(T.text("period_n", k + 1));
        }
        updateChildrenEnabledStates();
        fireLayoutChangeListeners();
    }

    private void onPeriodSetsChange() {
        List<PeriodSet> periodSets = this.periodSetsSource.getPeriodSets();
        int index = -1;
        String[] items = new String[periodSets.size()];
        for (int k = 0; k < items.length; k++) {
            PeriodSet ps = periodSets.get(k);
            items[k] = ps.getName();
            if (ps.equals(currentPeriodSet)) {
                index = k;
            }
        }
        periodSetCombobox.setItems(items);
        periodSetCombobox.setSelectedItem(index);
    }

    public void setPeriodSetSource(PeriodSetsSource periodSetsSource) {
        if (this.periodSetsSource != null) {
            this.periodSetsSource.removeChangeListener(periodSetsChangeListener);
        }
        this.periodSetsSource = periodSetsSource;
        if (periodSetsSource != null) {
            this.periodSetsSource.addChangeListener(periodSetsChangeListener);
            onPeriodSetsChange();
        }
    }

    // ************************************************************************
    // icon and labels

    public void setIcon(Icon icon) {
        constantParameter.icon.setIcon(icon);
    }

    public void setIcon(String text) {
        constantParameter.icon.setText(text);
    }

    public JLabel getIconLabel() {
        return constantParameter.icon;
    }

    public void setSymbolUnitLabels(String symbol, String unit) {
        constantParameter.symbolAndUnit.setLatexEquation(
                "%s \\quad [%s]".formatted(
                        EquationLabel.convertToLatex(symbol),
                        EquationLabel.convertToLatex(unit)));
    }

    public void setNameLabel(String name) {
        constantParameter.label.setText(name);
    }

    public String getNameLabel() {
        return constantParameter.label.getText();
    }

    public JLabel getSymbolAndUnitLabel() {
        return constantParameter.symbolAndUnit;
    }

    // ************************************************************************
    // component states

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        updateChildrenEnabledStates();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setLock(boolean locked) {
        isLocked = locked;
        lockAllValuesBtn.setSelected(locked);
        updateChildrenEnabledStates();
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setAsVariable(boolean variable) {
        if (variableParameterBtn.isSelected() != variable) {
            variableParameterBtn.setSelected(variable);
        }
    }

    public boolean isVariable() {
        return isVariable;
    }

    private void updateChildrenEnabledStates() {
        boolean enabledState = !isLocked && isEnabled;

        menuButton.setEnabled(isEnabled);
        variableParameterBtn.setEnabled(isEnabled && !isLocked);
        autoInitialValueBtn.setEnabled(isEnabled && !isLocked);

        constantParameter.setEnabled(enabledState);
        variableSubParameters.forEach(p -> p.setEnabled(enabledState));
        periodSetCombobox.setEnabled(enabledState);
    }

    // ************************************************************************
    // distribution

    public void setSupportedDistribution(List<DistributionType> distributionTypes) {
        constantParameter.field.setSupportedDistribution(distributionTypes);
    }

    public void setDistributionType(DistributionType distributionType) {
        constantParameter.field.setDistributionType(distributionType);
    }

    public DistributionType getDistributionType() {
        return constantParameter.field.getDistributionType();
    }

    // ************************************************************************
    // constant parameter

    public Double[] getDistributionParameters() {
        return constantParameter.field.getParameters();
    }

    public Double getInitialGuess() {
        return constantParameter.initialGuess.getDoubleValue();
    }

    public void setDistributionParameters(Double[] values) {
        constantParameter.field.setParameters(values);
    }

    public void setInitialGuess(Double value) {
        constantParameter.initialGuess.setValue(value);
    }

    public void fromGaussian(Double mean, Double std) {
        constantParameter.field.setDistributionType(DistributionType.GAUSSIAN);
        constantParameter.field.setParameters(new Double[] { mean, std });
        constantParameter.initialGuess.setValue(mean);
        setAsVariable(false);
    }

    // ************************************************************************
    // variable parameter

    public void fromGaussian(
            PeriodSet periodSet,
            Double[] meanValues,
            Double[] stdValues) {
        if (periodSetsSource == null) {
            ConsoleLogger.error("Cannot set a parameter prior panel if no period set source is set");
            return;
        }
        int periodSetIndex = getPeriodSetIndex(periodSet);
        if (periodSetIndex == -1) {
            ConsoleLogger
                    .error("Cannot set a parameter prior panel with a period set not found in the period set source");
            return;
        }
        periodSetCombobox.setSelectedItem(periodSetIndex, true);
        setPeriodSet(periodSet);
        int n = periodSet.getNumberOfPeriods();
        if (meanValues.length != n || stdValues.length != n) {
            ConsoleLogger
                    .error("Cannot set a parameter prior panel if the number of mean/std don't match the number of periods");
            return;
        }
        for (int k = 0; k < n; k++) {
            ParameterLabelsAndFields psf;
            if (k >= variableSubParameters.size()) {
                psf = new ParameterLabelsAndFields();
                psf.addChangeListener(l -> fireChangeListeners());

                variableSubParameters.add(psf);
            } else {
                psf = variableSubParameters.get(k);
            }
            psf.field.setDistributionType(DistributionType.GAUSSIAN);
            psf.field.setParameters(new Double[] { meanValues[k], stdValues[k] });
            psf.initialGuess.setValue(meanValues[k]);
        }
        setAsVariable(true);
    }

    // ************************************************************************
    // reactivity

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    protected void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    // layout change listener (to use when table layout has change)
    private final List<ChangeListener> layoutChangeListeners = new ArrayList<>();

    public void addLayoutChangeListener(ChangeListener l) {
        layoutChangeListeners.add(l);
    }

    public void removeLayoutChangeListener(ChangeListener l) {
        layoutChangeListeners.remove(l);
    }

    protected void fireLayoutChangeListeners() {
        for (ChangeListener l : layoutChangeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    // ************************************************************************
    // save / load

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

    // ************************************************************************
    // abstract methods

    public abstract Parameter getParameter();

    public abstract int addToGrid(SimpleGridPanel grid, int firstRow);

}
