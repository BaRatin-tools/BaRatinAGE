package org.baratinage.ui.commons;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.ui.component.SimpleNumberField;

public class ParameterPriorDist extends AbstractParameterPriorDist {
    public final JLabel iconLabel;
    public final JLabel symbolUnitLabel;
    public final JLabel nameLabel;
    public final SimpleNumberField initialGuessField;

    public final DistributionField distributionField;
    public final JCheckBox lockCheckbox;

    private static String vAlignFixString = "<sup>&nbsp;</sup><sub>&nbsp;</sub>";

    private static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    public ParameterPriorDist() {

        iconLabel = new JLabel();
        symbolUnitLabel = new JLabel();
        nameLabel = new JLabel();
        symbolUnitLabel.setFont(MONOSPACE_FONT);
        initialGuessField = new SimpleNumberField();

        distributionField = new DistributionField();

        lockCheckbox = new JCheckBox();
        lockCheckbox.addChangeListener((chEvt) -> {
            updateLock();
        });
    }

    @Override
    public void setIcon(Icon icon) {
        iconLabel.setIcon(icon);
    }

    @Override
    public void setSymbolUnitLabels(String symbol, String unit) {
        symbolUnitLabel.setText(String.format("<html>%s[%s]%s</html>", symbol, unit, vAlignFixString));
    }

    public void setNameLabel(String name) {
        nameLabel.setText(name);
    }

    @Override
    public void setLocalLock(boolean locked) {
        lockCheckbox.setSelected(locked);
    }

    @Override
    public void setGlobalLock(boolean locked) {
        lockCheckbox.setEnabled(!locked);
        updateLock();
    }

    private void updateLock() {
        if (lockCheckbox.isEnabled()) {
            boolean isLocked = lockCheckbox.isSelected();
            initialGuessField.setEnabled(!isLocked);
            distributionField.distributionCombobox.setEnabled(!isLocked);
            for (SimpleNumberField field : distributionField.parameterFields) {
                field.setEnabled(!isLocked);
            }
        } else {
            initialGuessField.setEnabled(false);
            distributionField.distributionCombobox.setEnabled(false);
            for (SimpleNumberField field : distributionField.parameterFields) {
                field.setEnabled(false);
            }
        }
    }

    @Override
    public boolean isLocked() {
        return lockCheckbox.isSelected();
    }

    @Override
    public Parameter getParameter() {
        Distribution distribution = distributionField.getDistribution();
        if (distribution == null) {
            return null;
        }
        if (!initialGuessField.isValueValid()) {
            return null;
        }
        return new Parameter("", initialGuessField.getDoubleValue(), distribution);
    }

    @Override
    public DistributionType getDistributionType() {
        return distributionField.getDistributionType();
    }

    @Override
    public Double[] getDistributionParameters() {
        return distributionField.getParameters();
    }

    @Override
    public Double getInitialGuess() {
        return initialGuessField.getDoubleValue();
    }

    @Override
    public void setDistributionType(DistributionType distributionType) {
        distributionField.setDistributionType(distributionType);
    }

    @Override
    public void setDistributionParameters(Double[] values) {
        distributionField.setParameters(values);
    }

    @Override
    public void setInitialGuess(Double value) {
        initialGuessField.setValue(value);
    }
}
