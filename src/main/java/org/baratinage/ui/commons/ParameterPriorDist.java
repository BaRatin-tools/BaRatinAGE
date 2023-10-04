package org.baratinage.ui.commons;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.ui.component.SimpleNumberField;

public class ParameterPriorDist extends AbstractParameterPriorDist implements ChangeListener {
    public final String bamName;
    public final JLabel iconLabel;
    public final JLabel symbolUnitLabel;
    public final JLabel nameLabel;
    public final SimpleNumberField initialGuessField;

    public final DistributionField distributionField;
    public final JCheckBox lockCheckbox;

    private static String vAlignFixString = "<sup>&nbsp;</sup><sub>&nbsp;</sub>";

    private static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    public ParameterPriorDist(String bamName) {
        this.bamName = bamName;

        iconLabel = new JLabel();
        symbolUnitLabel = new JLabel();
        nameLabel = new JLabel();
        symbolUnitLabel.setFont(MONOSPACE_FONT);
        initialGuessField = new SimpleNumberField();
        initialGuessField.addChangeListener(this);

        distributionField = new DistributionField();
        distributionField.addChangeListener(this);

        lockCheckbox = new JCheckBox();
        lockCheckbox.addChangeListener((chEvt) -> {
            updateLock();
            fireChangeListeners();
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
        updateLock();
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
            distributionField.setEnabled(!isLocked);
        } else {
            initialGuessField.setEnabled(false);
            distributionField.setEnabled(false);
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
        return new Parameter(bamName, initialGuessField.getDoubleValue(), distribution);
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
