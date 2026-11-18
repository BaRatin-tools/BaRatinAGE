package org.baratinage.ui.commons;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.ui.commons.variable_parameter.ParameterLabelsAndFields;
import org.baratinage.ui.container.SimpleGridPanel;
import org.json.JSONObject;

public class ParameterPriorDist extends AbstractParameterPriorDist {

    public final String bamName;

    public ParameterPriorDist(String bamName) {
        this.bamName = bamName;

        contextMenu.add(variableParameterBtn);
        contextMenu.add(lockAllValuesBtn);
        contextMenu.add(autoInitialValueBtn);
    }

    @Override
    public Parameter getParameter() {
        Distribution distribution = constantParameter.field.getDistribution();
        if (distribution == null) {
            return null;
        }
        if (!constantParameter.initialGuess.isValueValid()) {
            return null;
        }
        return new Parameter(bamName, constantParameter.initialGuess.getDoubleValue(), distribution);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("autoInitialValue", autoInitialValueBtn.isSelected());
        return json;
    }

    public void fromJSON(JSONObject json) {
        super.fromJSON(json);
        boolean autoInitialValue = json.optBoolean("autoInitialValue", false);
        autoInitialValueBtn.setSelected(autoInitialValue);
    }

    /*
     * the grid is expecting 7 columns
     * - icon
     * - label
     * - symbol + unit
     * - distribution type
     * - distribution parameters
     * - initial guess field
     * - menu
     */
    @Override
    public int addToGrid(SimpleGridPanel grid, int firstRow) {

        grid.add(constantParameter.icon, SimpleGridPanel.cell(0, firstRow));
        grid.add(constantParameter.label, SimpleGridPanel.cell(1, firstRow));
        grid.add(constantParameter.symbolAndUnit, SimpleGridPanel.cell(2, firstRow));
        grid.add(
                constantParameter.field.distributionCombobox,
                SimpleGridPanel.cell(3, firstRow));
        grid.add(menuButton, SimpleGridPanel.cell(6, firstRow));
        if (!isVariable()) {
            grid.add(
                    constantParameter.field.parameterFieldsPanel,
                    SimpleGridPanel.cell(4, firstRow));
            grid.add(constantParameter.initialGuess, SimpleGridPanel.cell(5, firstRow));
        } else {
            grid.add(periodSetCombobox, SimpleGridPanel.cell(4, firstRow).span(2, 1));
            if (currentPeriodSet != null) {
                for (int k = 0; k < currentPeriodSet.getNumberOfPeriods(); k++) {
                    firstRow++;
                    ParameterLabelsAndFields psf = variableSubParameters.get(k);
                    grid.add(psf.label, SimpleGridPanel.cell(3, firstRow));
                    grid.add(psf.field.parameterFieldsPanel, SimpleGridPanel.cell(4, firstRow));
                    grid.add(psf.initialGuess, SimpleGridPanel.cell(5, firstRow));
                }
            }
        }
        return firstRow + 1;
    }
}
