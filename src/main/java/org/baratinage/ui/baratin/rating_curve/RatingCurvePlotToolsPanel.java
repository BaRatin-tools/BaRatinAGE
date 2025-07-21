package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.Plot;

public class RatingCurvePlotToolsPanel extends SimpleFlowPanel {

    public final JCheckBox logScaleDischargeAxis;
    public final JCheckBox switchAxisCheckbox;
    public final JCheckBox smoothTotalEnvelopCheckbox;
    public final JCheckBox cropTotalEnvelopCheckbox;

    public RatingCurvePlotToolsPanel() {
        super();

        logScaleDischargeAxis = new JCheckBox();
        logScaleDischargeAxis.setSelected(false);
        logScaleDischargeAxis.setText("log_scale_discharge_axis");

        switchAxisCheckbox = new JCheckBox();
        switchAxisCheckbox.setSelected(false);
        switchAxisCheckbox.setText("swap_xy_axis");

        smoothTotalEnvelopCheckbox = new JCheckBox();
        smoothTotalEnvelopCheckbox.setSelected(true);
        smoothTotalEnvelopCheckbox.setText("smooth_total_envelop");

        cropTotalEnvelopCheckbox = new JCheckBox();
        cropTotalEnvelopCheckbox.setSelected(false);
        cropTotalEnvelopCheckbox.setText("crop_total_envelop_zero");

        logScaleDischargeAxis.addActionListener((e) -> {
            fireChangeListeners();
        });

        switchAxisCheckbox.addActionListener((e) -> {
            fireChangeListeners();
        });

        smoothTotalEnvelopCheckbox.addActionListener((e) -> {
            fireChangeListeners();
        });
        cropTotalEnvelopCheckbox.addActionListener((e) -> {
            fireChangeListeners();
        });

        setGap(5);
        addChild(logScaleDischargeAxis, false);
        addChild(switchAxisCheckbox, false);
        addChild(smoothTotalEnvelopCheckbox, false);
        addChild(cropTotalEnvelopCheckbox, false);

        T.t(this, switchAxisCheckbox, false, "swap_xy_axis");
        T.t(this, logScaleDischargeAxis, false, "log_scale_discharge_axis");
        T.t(this, smoothTotalEnvelopCheckbox, false, "smooth_total_envelop");
        T.t(this, cropTotalEnvelopCheckbox, false, "crop_total_envelop_zero");
    }

    public void configure(boolean logDischargeAxis, boolean axisFlipped, boolean totalEnvSmoothed,
            boolean cropTotalEnv) {
        removeAll();
        if (logDischargeAxis) {
            addChild(logScaleDischargeAxis, false);
        }
        if (axisFlipped) {
            addChild(switchAxisCheckbox, false);
        }
        if (totalEnvSmoothed) {
            addChild(smoothTotalEnvelopCheckbox, false);
        }
        if (cropTotalEnv) {
            addChild(cropTotalEnvelopCheckbox, false);
        }
    }

    public void updatePlotAxis(Plot plot) {
        // apply log/linear scale
        if (logScaleDischargeAxis.isSelected()) {
            if (switchAxisCheckbox.isSelected()) {
                plot.plot.setDomainAxis(plot.axisXlog);
            } else {
                plot.plot.setRangeAxis(plot.axisYlog);
            }
        }
        // update labels
        if (axisFlipped()) {
            plot.axisY.setLabel(T.text("stage") + " [m]");
            plot.axisX.setLabel(T.text("discharge") + " [m3/s]");
            plot.axisXlog.setLabel(T.text("discharge") + " [m3/s]");
        } else {
            plot.axisX.setLabel(T.text("stage") + " [m]");
            plot.axisY.setLabel(T.text("discharge") + " [m3/s]");
            plot.axisYlog.setLabel(T.text("discharge") + " [m3/s]");
        }
    }

    public boolean axisFlipped() {
        return switchAxisCheckbox.isSelected();
    }

    public void setAxisFlipped(boolean value) {
        switchAxisCheckbox.setSelected(value);
    }

    public boolean totalEnvSmoothed() {
        return smoothTotalEnvelopCheckbox.isSelected();
    }

    public void setTotalEnvSmoothed(boolean value) {
        smoothTotalEnvelopCheckbox.setSelected(value);
    }

    public boolean cropTotalEnv() {
        return cropTotalEnvelopCheckbox.isSelected();
    }

    public void setCropTotalEnv(boolean value) {
        cropTotalEnvelopCheckbox.setSelected(value);
    }

    public boolean logDischargeAxis() {
        return logScaleDischargeAxis.isSelected();
    }

    public void setLogDischargeAxis(boolean value) {
        logScaleDischargeAxis.setSelected(value);
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
