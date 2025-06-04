package org.baratinage.ui.commons;

import java.awt.Font;
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
import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleNumberField;
import org.json.JSONObject;

public class ParameterPriorDist extends AbstractParameterPriorDist implements ChangeListener {
    public final String bamName;
    public final JLabel iconLabel;
    public final JLabel symbolUnitLabel;
    public final JLabel nameLabel;
    public final SimpleNumberField initialGuessField;
    public final DistributionField distributionField;

    public final JButton menuButton = new JButton();
    public final JPopupMenu contextMenu = new JPopupMenu();
    public final JCheckBoxMenuItem lockAllValuesBtn = new JCheckBoxMenuItem();
    public final JCheckBoxMenuItem autoInitialValueBtn = new JCheckBoxMenuItem();

    private boolean isEnabled = true;
    private boolean isLocked = false;

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

        menuButton.setIcon(AppSetup.ICONS.MORE_DOTS);

        contextMenu.add(lockAllValuesBtn);
        contextMenu.add(autoInitialValueBtn);

        menuButton.addActionListener((l) -> {
            contextMenu.show(menuButton, 0, menuButton.getHeight());
        });

        lockAllValuesBtn.addItemListener(l -> {
            isLocked = lockAllValuesBtn.isSelected();
            updateEnabledStates();
        });
        autoInitialValueBtn.addItemListener(l -> {
            updateEnabledStates();
            updateInitialGuessField();
        });

        distributionField.addChangeListener((l) -> {
            updateInitialGuessField();
        });

        T.updateHierarchy(this, initialGuessField);
        T.updateHierarchy(this, distributionField);

        T.t(this, () -> {
            lockAllValuesBtn.setText(T.text("parameter_dist_lock"));
            autoInitialValueBtn.setText(T.text("parameter_dist_auto_initial"));
        });

        autoInitialValueBtn.setSelected(true);
    }

    private void updateEnabledStates() {
        boolean globallyLocked = !isEnabled;
        boolean allValuesLocked = isLocked;
        boolean initialValueLocked = autoInitialValueBtn.isSelected();

        distributionField.setEnabled(!globallyLocked && !allValuesLocked);
        initialGuessField.setEnabled(!globallyLocked && !allValuesLocked && !initialValueLocked);
        menuButton.setEnabled(!globallyLocked);
        autoInitialValueBtn.setEnabled(!allValuesLocked);
    }

    private void updateInitialGuessField() {
        if (!autoInitialValueBtn.isSelected()) {
            return;
        }
        Distribution dist = distributionField.getDistribution();
        if (dist != null) {
            Double median = dist.getMedian();
            if (median != null) {
                initialGuessField.setValue(median);
            }
        } else {
            initialGuessField.setValue(Double.NaN);
        }
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
        lockAllValuesBtn.setSelected(locked);
        updateEnabledStates();
    }

    @Override
    public boolean isLocked() {
        return isLocked;
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
}
