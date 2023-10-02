package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class DistributionField implements ChangeListener {
    private final HashMap<DistributionType, List<SimpleNumberField>> allParameterFields;
    public final RowColPanel parameterFieldsPanel;
    public final List<SimpleNumberField> parameterFields;
    public final SimpleComboBox distributionCombobox;

    private final static DistributionType[] DISTRIBUTION_TYPES = DistributionType.values();

    private int currentDistributionIndex;

    public DistributionField() {

        allParameterFields = new HashMap<>();
        for (DistributionType d : DISTRIBUTION_TYPES) {
            List<SimpleNumberField> parameterFields = new ArrayList<>();
            for (String pName : d.parameterNames) {
                SimpleNumberField field = new SimpleNumberField();
                field.setPlaceholder(Lg.text(pName));
                field.addChangeListener(this);
                parameterFields.add(field);
            }
            allParameterFields.put(d, parameterFields);
        }
        parameterFields = new ArrayList<>();
        parameterFieldsPanel = new RowColPanel();
        parameterFieldsPanel.setGap(5);
        distributionCombobox = new SimpleComboBox();
        distributionCombobox.addChangeListener((chEvt) -> {
            fireChangeListeners();
            int index = distributionCombobox.getSelectedIndex();
            if (index == -1) {
                return;
            }
            currentDistributionIndex = index;
            DistributionType d = DISTRIBUTION_TYPES[index];
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
            int n = DISTRIBUTION_TYPES.length;
            String[] distribLabels = new String[n];
            for (int k = 0; k < n; k++) {
                List<SimpleNumberField> parameterFields = allParameterFields.get(DISTRIBUTION_TYPES[k]);
                for (int i = 0; i < parameterFields.size(); i++) {
                    parameterFields.get(i).setPlaceholder(Lg.text(DISTRIBUTION_TYPES[k].parameterNames[i]));
                }
                distribLabels[k] = Lg.text("dist_" + DISTRIBUTION_TYPES[k].bamName);
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

    public void setEnabled(boolean enabled) {
        distributionCombobox.setEnabled(enabled);
        for (List<SimpleNumberField> fields : allParameterFields.values()) {
            for (SimpleNumberField field : fields) {
                field.setEnabled(enabled);
            }
        }
    }

    private SimpleNumberField getParameterField(int index) {
        DistributionType distType = getDistributionType();
        if (distType == null) {
            return null;
        }
        if (index < 0 || index >= distType.parameterNames.length) {
            return null;
        }
        return parameterFields.get(index);
    }

    public DistributionType getDistributionType() {
        return currentDistributionIndex < 0 ? null : DISTRIBUTION_TYPES[currentDistributionIndex];
    }

    public Double getParameter(int index) {
        SimpleNumberField field = getParameterField(index);
        return field != null && field.isValueValid() ? field.getDoubleValue() : null;
    }

    public Double[] getParameters() {
        DistributionType distributionType = getDistributionType();
        if (distributionType == null) {
            return null;
        }
        int nPars = distributionType.parameterNames.length;
        Double[] parValues = new Double[nPars];
        for (int k = 0; k < nPars; k++) {
            parValues[k] = getParameter(k);
        }
        return parValues;
    }

    public void setDistributionType(DistributionType distributionType) {
        if (distributionType != null) {
            int index = -1;
            for (int k = 0; k < DISTRIBUTION_TYPES.length; k++) {
                if (DISTRIBUTION_TYPES[k].equals(distributionType)) {
                    index = k;
                    break;
                }
            }
            if (index >= 0) {
                distributionCombobox.setSelectedItem(index, false);
            }
        }
    }

    public void setParameter(int index, Double value) {
        DistributionType distributionType = getDistributionType();
        if (distributionType == null) {
            return;
        }
        if (index < 0 || index >= distributionType.parameterNames.length) {
            return;
        }
        SimpleNumberField field = parameterFields.get(index);
        field.setValue(value);
    }

    public void setParameters(Double... values) {
        DistributionType distributionType = getDistributionType();
        if (distributionType == null) {
            return;
        }
        int n = distributionType.parameterNames.length;
        if (n != values.length) {
            return;
        }
        for (int k = 0; k < n; k++) {
            parameterFields.get(k).setValue(values[k]);
        }
    }

    public Distribution getDistribution() {
        DistributionType distributionType = getDistributionType();
        if (distributionType == null) {
            return null;
        }
        int nPars = distributionType.parameterNames.length;
        double[] parValues = new double[nPars];
        for (int k = 0; k < nPars; k++) {
            SimpleNumberField field = getParameterField(k);
            if (!field.isValueValid()) {
                return null;
            }
            parValues[k] = field.getDoubleValue();
        }
        return new Distribution(distributionType, parValues);
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    private void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeListeners();
    }

}
