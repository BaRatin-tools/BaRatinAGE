package org.baratinage.ui.commons;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.Distribution.DISTRIBUTION;
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

    public ParameterPriorDistSimplified() {

        iconLabel = new JLabel();
        symbolUnitLabel = new JLabel();
        nameLabel = new JLabel();
        symbolUnitLabel.setFont(MONOSPACE_FONT);

        meanValueField = new SimpleNumberField();
        uncertaintyValueField = new SimpleNumberField();

        lockCheckbox = new JCheckBox();
        lockCheckbox.addChangeListener((chEvt) -> {
            updateLock();
        });
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
    public void setLocalLock(boolean locked) {
        lockCheckbox.setSelected(locked);
    }

    @Override
    public void setGlobalLock(boolean locked) {
        lockCheckbox.setEnabled(locked);
        updateLock();
    }

    private void updateLock() {
        if (lockCheckbox.isEnabled()) {
            boolean isLocked = lockCheckbox.isSelected();
            meanValueField.setEnabled(!isLocked);
            uncertaintyValueField.setEnabled(!isLocked);
        } else {
            meanValueField.setEnabled(false);
            uncertaintyValueField.setEnabled(false);
        }
    }

    public void setDefaultValues(double mean, double uncertainty) {
        meanValueField.setValue(mean);
        uncertaintyValueField.setValue(uncertainty);
    }

    @Override
    public void configure(boolean isLocked, Parameter parameter) {
        setLocalLock(isLocked);

        if (parameter != null) {
            // assuming gaussian distribution!
            double meanValue = parameter.distribution.parameterValues[0];
            double uncertaintyValue = parameter.distribution.parameterValues[1] * 2;
            meanValueField.setValue(meanValue);
            uncertaintyValueField.setValue(uncertaintyValue);
        }
    }

    @Override
    public boolean isLocked() {
        return lockCheckbox.isSelected();
    }

    @Override
    public Parameter getParameter() {
        if (!meanValueField.isValueValid() || !uncertaintyValueField.isValueValid()) {
            return null;
        }
        double mean = meanValueField.getDoubleValue();
        double std = uncertaintyValueField.getDoubleValue() / 2;
        Distribution d = new Distribution(DISTRIBUTION.GAUSSIAN, mean, std);
        return new Parameter(vAlignFixString, mean, d);
    }
}
