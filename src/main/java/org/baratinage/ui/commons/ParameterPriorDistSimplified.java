package org.baratinage.ui.commons;

import javax.swing.Icon;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.ui.commons.variable_parameter.ParameterLabelsAndFields;
import org.baratinage.ui.container.SimpleGridPanel;

public class ParameterPriorDistSimplified extends AbstractParameterPriorDist {

    public ParameterPriorDistSimplified() {

        contextMenu.add(variableParameterBtn);
        contextMenu.add(lockAllValuesBtn);
    }

    public ParameterPriorDistSimplified(Icon icon, String symbol, String unit) {
        this();
        setIcon(icon);
        setSymbolUnitLabels(symbol, unit);
    }

    public void setDefaultValues(double mean, double uncertainty) {
        constantParameter.meanField.setValue(mean);
        constantParameter.uncertaintyField.setValue(uncertainty);
    }

    @Override
    public Parameter getParameter() {
        if (!constantParameter.meanField.isValueValid() || !constantParameter.uncertaintyField.isValueValid()) {
            return null;
        }
        double mean = constantParameter.meanField.getDoubleValue();
        double std = constantParameter.uncertaintyField.getDoubleValue() / 2;
        Distribution d = new Distribution(DistributionType.GAUSSIAN, mean, std);
        return new Parameter(constantParameter.label.getText(), mean, d);
    }

    public Double[] getMeanValues() {
        if (currentPeriodSet == null) {
            return new Double[0];
        }
        int n = currentPeriodSet.getNumberOfPeriods();
        Double[] means = new Double[n];
        for (int k = 0; k < n; k++) {
            ParameterLabelsAndFields psf = variableSubParameters.get(k);
            means[k] = psf.meanField.isValueValid() ? psf.meanField.getDoubleValue() : null;
        }
        return means;
    }

    public Double[] getStdValues() {
        if (currentPeriodSet == null) {
            return new Double[0];
        }
        int n = currentPeriodSet.getNumberOfPeriods();
        Double[] stds = new Double[n];
        for (int k = 0; k < n; k++) {
            ParameterLabelsAndFields psf = variableSubParameters.get(k);
            stds[k] = psf.uncertaintyField.isValueValid() ? psf.uncertaintyField.getDoubleValue() / 2.0 : null;
        }
        return stds;
    }

    public Double getMeanValue() {
        return constantParameter.meanField.isValueValid() ? constantParameter.meanField.getDoubleValue() : null;
    }

    public Double getUncertaintyValue() {
        return constantParameter.uncertaintyField.isValueValid()
                ? constantParameter.uncertaintyField.getDoubleValue()
                : null;
    }

    public Double getStdValue() {
        Double uncertainty = getUncertaintyValue();
        return uncertainty == null ? null : uncertainty / 2.0;
    }

    /*
     * The grid is expecting 6 columns:
     * - icon
     * - label
     * - symbol+unit
     * - mean
     * - uncertainty
     * - lock/menu
     */
    @Override
    public int addToGrid(SimpleGridPanel grid, int firstRow) {
        grid.add(constantParameter.icon, SimpleGridPanel.cell(0, firstRow));
        grid.add(constantParameter.label, SimpleGridPanel.cell(1, firstRow));
        grid.add(constantParameter.symbolAndUnit, SimpleGridPanel.cell(2, firstRow));
        grid.add(menuButton, SimpleGridPanel.cell(5, firstRow));
        if (!isVariable()) {
            grid.add(constantParameter.meanField, SimpleGridPanel.cell(3, firstRow));
            grid.add(constantParameter.uncertaintyField, SimpleGridPanel.cell(4, firstRow));
        } else {
            grid.add(periodSetCombobox, SimpleGridPanel.cell(3, firstRow).span(2, 1));
            if (currentPeriodSet != null) {
                for (int k = 0; k < currentPeriodSet.getNumberOfPeriods(); k++) {
                    firstRow++;
                    ParameterLabelsAndFields psf = variableSubParameters.get(k);
                    grid.add(psf.label, SimpleGridPanel.cell(1, firstRow).span(2, 1));
                    grid.add(psf.meanField, SimpleGridPanel.cell(3, firstRow));
                    grid.add(psf.uncertaintyField, SimpleGridPanel.cell(4, firstRow));
                }
            }
        }
        return firstRow + 1;
    }
}
