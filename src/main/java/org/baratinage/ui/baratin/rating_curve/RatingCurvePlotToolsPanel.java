package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.container.RowColPanel;

public class RatingCurvePlotToolsPanel extends RowColPanel {

    private boolean axisFliped = false;
    private boolean dischargeAxisInLog = false;
    private boolean smoothTotalEnvelop = true;

    private final JCheckBox switchDischargeAxisScale;
    private final JCheckBox switchAxisCheckbox;
    private final JCheckBox smoothTotalEnvelopCheckbox;

    public RatingCurvePlotToolsPanel() {
        super(AXIS.ROW, ALIGN.START);

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
        appendChild(switchDischargeAxisScale, 0);
        appendChild(switchAxisCheckbox, 0);
        appendChild(smoothTotalEnvelopCheckbox, 0);

        T.t(this, switchAxisCheckbox, false, "swap_xy_axis");
        T.t(this, switchDischargeAxisScale, false, "log_scale_discharge_axis");
        T.t(this, smoothTotalEnvelopCheckbox, false, "smooth_total_envelop");
    }

    public void configure(boolean logDischargeAxis, boolean axisFlipped, boolean totalEnvSmoothed) {
        clear();
        if (logDischargeAxis) {
            appendChild(switchDischargeAxisScale, 0);
        }
        if (axisFlipped) {
            appendChild(switchAxisCheckbox, 0);
        }
        if (totalEnvSmoothed) {
            appendChild(smoothTotalEnvelopCheckbox, 0);
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
