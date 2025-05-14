package org.baratinage.ui.commons;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleNumberField;

public class ParameterPriorDistSimplified extends AbstractParameterPriorDist {

    public final JLabel iconLabel;
    public final JLabel symbolUnitLabel;
    public final JLabel nameLabel;
    public final SimpleNumberField meanValueField;
    public final SimpleNumberField uncertaintyValueField;
    public final JCheckBox lockCheckbox;

    private static String vAlignFixString = "<sup>&nbsp;</sup><sub>&nbsp;</sub>";

    private static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private boolean isEnabled = true;
    private boolean isLocked = false;

    public ParameterPriorDistSimplified() {

        iconLabel = new JLabel();
        symbolUnitLabel = new JLabel();
        nameLabel = new JLabel();
        symbolUnitLabel.setFont(MONOSPACE_FONT);

        meanValueField = new SimpleNumberField();
        uncertaintyValueField = new SimpleNumberField();

        lockCheckbox = new JCheckBox();
        lockCheckbox.addItemListener((chEvt) -> {
            isLocked = lockCheckbox.isSelected();
            updateEnabledStates();
        });

        T.updateHierarchy(this, meanValueField);
        T.updateHierarchy(this, uncertaintyValueField);
    }

    public ParameterPriorDistSimplified(Icon icon, String symbol, String unit) {
        this();
        setIcon(icon);
        setSymbolUnitLabels(symbol, unit);
    }

    @Override
    public void setIcon(Icon icon) {
        iconLabel.setIcon(icon);
    }

    @Override
    public void setSymbolUnitLabels(String symbol, String unit) {
        symbolUnitLabel.setText(String.format("<html>%s[%s]%s</html>", symbol, unit, vAlignFixString));
    }

    @Override
    public void setNameLabel(String name) {
        nameLabel.setText(name);
    }

    @Override
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        updateEnabledStates();
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setLock(boolean locked) {
        isLocked = locked;
        lockCheckbox.setSelected(locked);
        updateEnabledStates();
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    private void updateEnabledStates() {
        boolean globallyLocked = !isEnabled;
        boolean locallyLocked = isLocked;
        meanValueField.setEnabled(!globallyLocked && !locallyLocked);
        uncertaintyValueField.setEnabled(!globallyLocked && !locallyLocked);
        // lockCheckbox.setEnabled(!globallyLocked);
    }

    public void setDefaultValues(double mean, double uncertainty) {
        meanValueField.setValue(mean);
        uncertaintyValueField.setValue(uncertainty);
    }

    @Override
    public Parameter getParameter() {
        if (!meanValueField.isValueValid() || !uncertaintyValueField.isValueValid()) {
            return null;
        }
        double mean = meanValueField.getDoubleValue();
        double std = uncertaintyValueField.getDoubleValue() / 2;
        Distribution d = new Distribution(DistributionType.GAUSSIAN, mean, std);
        return new Parameter(vAlignFixString, mean, d);
    }

    @Override
    public DistributionType getDistributionType() {
        return DistributionType.GAUSSIAN;
    }

    @Override
    public Double[] getDistributionParameters() {
        Double uncertainty = uncertaintyValueField.getDoubleValue();

        return new Double[] {
                meanValueField.getDoubleValue(),
                uncertainty == null ? null : uncertainty / 2
        };
    }

    @Override
    public Double getInitialGuess() {
        return meanValueField.getDoubleValue();
    }

    @Override
    public void setDistributionType(DistributionType distributionType) {
        // ignored since always gaussian...
    }

    @Override
    public void setDistributionParameters(Double[] values) {
        if (values.length == 2) {
            meanValueField.setValue(values[0]);
            uncertaintyValueField.setValue(values[1] == null ? null : values[1] * 2);
        }
    }

    @Override
    public void setInitialGuess(Double value) {
        // ignored
    }
}
