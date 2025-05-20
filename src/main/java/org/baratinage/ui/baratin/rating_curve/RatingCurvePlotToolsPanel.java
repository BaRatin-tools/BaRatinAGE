package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;

public class RatingCurvePlotToolsPanel extends SimpleFlowPanel {

    private boolean axisFliped = false;
    private boolean dischargeAxisInLog = false;
    private boolean smoothTotalEnvelop = true;

    private final JCheckBox switchDischargeAxisScale;
    private final JCheckBox switchAxisCheckbox;
    private final JCheckBox smoothTotalEnvelopCheckbox;

    public RatingCurvePlotToolsPanel() {
        super();

        switchDischargeAxisScale = new JCheckBox();
        switchDischargeAxisScale.setSelected(false);
        switchDischargeAxisScale.setText("log_scale_discharge_axis");

        switchAxisCheckbox = new JCheckBox();
        switchAxisCheckbox.setSelected(false);
        switchAxisCheckbox.setText("swap_xy_axis");

        smoothTotalEnvelopCheckbox = new JCheckBox();
        smoothTotalEnvelopCheckbox.setSelected(true);
        smoothTotalEnvelopCheckbox.setText("smooth_total_envelop");

        switchDischargeAxisScale.addActionListener((e) -> {
            dischargeAxisInLog = switchDischargeAxisScale.isSelected();
            fireChangeListeners();
        });

        switchAxisCheckbox.addActionListener((e) -> {
            axisFliped = switchAxisCheckbox.isSelected();
            fireChangeListeners();
        });

        smoothTotalEnvelopCheckbox.addActionListener((e) -> {
            smoothTotalEnvelop = smoothTotalEnvelopCheckbox.isSelected();
            fireChangeListeners();
        });

        setGap(5);
        addChild(switchDischargeAxisScale, false);
        addChild(switchAxisCheckbox, false);
        addChild(smoothTotalEnvelopCheckbox, false);

        T.t(this, switchAxisCheckbox, false, "swap_xy_axis");
        T.t(this, switchDischargeAxisScale, false, "log_scale_discharge_axis");
        T.t(this, smoothTotalEnvelopCheckbox, false, "smooth_total_envelop");
    }

    public void configure(boolean logDischargeAxis, boolean axisFlipped, boolean totalEnvSmoothed) {
        removeAll();
        if (logDischargeAxis) {
            addChild(switchDischargeAxisScale, false);
        }
        if (axisFlipped) {
            addChild(switchAxisCheckbox, false);
        }
        if (totalEnvSmoothed) {
            addChild(smoothTotalEnvelopCheckbox, false);
        }
    }

    public boolean axisFlipped() {
        return axisFliped;
    }

    public void setAxisFlipped(boolean value) {
        axisFliped = value;
        switchAxisCheckbox.setSelected(value);
    }

    public boolean totalEnvSmoothed() {
        return smoothTotalEnvelop;
    }

    public void setTotalEnvSmoothed(boolean value) {
        smoothTotalEnvelop = value;
        smoothTotalEnvelopCheckbox.setSelected(value);
    }

    public boolean logDischargeAxis() {
        return dischargeAxisInLog;
    }

    public void setLogDischargeAxis(boolean value) {
        dischargeAxisInLog = value;
        switchDischargeAxisScale.setSelected(value);
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
}
