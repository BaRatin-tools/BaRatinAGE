package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Distribution.DISTRIBUTION;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class DistributionField {
    private final HashMap<DISTRIBUTION, List<SimpleNumberField>> allParameterFields;
    public final RowColPanel parameterFieldsPanel;
    public final List<SimpleNumberField> parameterFields;
    public final SimpleComboBox distributionCombobox;

    private final static DISTRIBUTION[] DISTRIBS = DISTRIBUTION.values();

    private int currentDistributionIndex;

    public DistributionField() {

        allParameterFields = new HashMap<>();
        for (DISTRIBUTION d : DISTRIBS) {
            List<SimpleNumberField> parameterFields = new ArrayList<>();
            for (String pName : d.parameterNames) {
                SimpleNumberField field = new SimpleNumberField();
                field.setPlaceholder(Lg.text(pName));
                parameterFields.add(field);
            }
            allParameterFields.put(d, parameterFields);
        }
        parameterFields = new ArrayList<>();
        parameterFieldsPanel = new RowColPanel();
        parameterFieldsPanel.setGap(5);
        distributionCombobox = new SimpleComboBox();
        distributionCombobox.addChangeListener((chEvt) -> {
            int index = distributionCombobox.getSelectedIndex();
            if (index == -1) {
                return;
            }
            currentDistributionIndex = index;
            DISTRIBUTION d = DISTRIBS[index];
            List<SimpleNumberField> fields = allParameterFields.get(d);
            parameterFieldsPanel.clear();
            parameterFields.clear();
            for (SimpleNumberField field : fields) {
                parameterFieldsPanel.appendChild(field);
                parameterFields.add(field);
            }
            parameterFieldsPanel.updateUI();
        });

        Lg.register(this, () -> {
            int n = DISTRIBS.length;
            String[] distribLabels = new String[n];
            for (int k = 0; k < n; k++) {
                List<SimpleNumberField> parameterFields = allParameterFields.get(DISTRIBS[k]);
                for (int i = 0; i < parameterFields.size(); i++) {
                    parameterFields.get(i).setPlaceholder(Lg.text(DISTRIBS[k].parameterNames[i]));
                }
                distribLabels[k] = Lg.text("dist_" + DISTRIBS[k].bamName);
            }
            int selectedIndex = distributionCombobox.getSelectedIndex();
            distributionCombobox.setEmptyItem(null);
            distributionCombobox.setItems(distribLabels, true);
            distributionCombobox.setSelectedItem(selectedIndex, true);
        });

        distributionCombobox.setSelectedItem(0);
        currentDistributionIndex = 0;
        distributionCombobox.fireChangeListeners();
    }

    public Distribution getDistribution() {
        int nPar = parameterFields.size();
        double[] parameterValues = new double[nPar];
        for (int k = 0; k < nPar; k++) {
            SimpleNumberField field = parameterFields.get(k);
            if (!field.isValueValid()) {
                return null;
            }
            parameterValues[k] = field.getDoubleValue();
        }
        return new Distribution(DISTRIBS[currentDistributionIndex], parameterValues);
    }

    public void setDistribution(Distribution distribution) {
        int index = -1;
        for (int k = 0; k < DISTRIBS.length; k++) {
            if (DISTRIBS[k].equals(distribution.distribution)) {
                index = k;
                break;
            }
        }
        if (index == -1) {
            System.err.println("DistributionField Error: Unknown distribution! The field cannot be configured!");
            return;
        }
        distributionCombobox.setSelectedItem(index, false);
        if (parameterFields.size() != distribution.parameterValues.length) {
            System.err.println(
                    "DistributionField Error: Number of parameters doesn't match! The field cannot be configured!");
            return;
        }
        // for (int k = 0; k < DISTRIBS[k].parameterNames.length; k++) {
        for (int k = 0; k < distribution.parameterValues.length; k++) {
            parameterFields.get(k).setValue(distribution.parameterValues[k]);
        }
    }
}
