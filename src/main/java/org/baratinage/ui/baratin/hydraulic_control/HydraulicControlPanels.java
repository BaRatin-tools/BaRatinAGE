package org.baratinage.ui.baratin.hydraulic_control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.Distribution.DISTRIB;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.lg.LgElement;

public class HydraulicControlPanels extends RowColPanel implements IPriors, ChangeListener {

    private final List<OneHydraulicControl> controls;
    private int nVisibleHydraulicControls = 0;

    private final JTabbedPane tabs;

    public HydraulicControlPanels() {
        controls = new ArrayList<>();
        tabs = new JTabbedPane();

        Lg.register(new LgElement<JTabbedPane>(tabs) {
            @Override
            public void setTranslatedText() {
                int n = tabs.getTabCount();
                for (int k = 0; k < n; k++) {
                    String text = Lg.getText("ui", "control_number");
                    OneHydraulicControl ohc = (OneHydraulicControl) object.getComponent(k);
                    text = Lg.format(text, ohc.controlNumber);
                    object.setTitleAt(k, text);
                }
            }
        });

        appendChild(tabs, 1);
    }

    private void updateTabs() {
        int nTabs = tabs.getTabCount();
        if (nTabs > nVisibleHydraulicControls && nVisibleHydraulicControls > 0) {
            for (int k = nVisibleHydraulicControls - 1; k < nTabs; k++) {
                tabs.remove(k);
            }
        } else if (nTabs < nVisibleHydraulicControls && nVisibleHydraulicControls > 0) {
            for (int k = nTabs; k < nVisibleHydraulicControls; k++) {
                tabs.addTab("k_" + k, controls.get(k));
            }
        }
        Lg.updateTexts();
    }

    private void addHydraulicControl() {
        int n = controls.size();
        OneHydraulicControl ohc = new OneHydraulicControl(n);
        ohc.addChangeListener(this);
        Lg.register(new LgElement<OneHydraulicControl>(ohc) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText("ui", "control_number");
                text = Lg.format(text, object.controlNumber);
                object.nameLabel.setText(text);
            }
        });
        controls.add(ohc);
        nVisibleHydraulicControls = controls.size();
    }

    public void setHydraulicControls(int nControls) {
        if (nControls < controls.size()) {
            // remove controls
            nVisibleHydraulicControls = nControls;
            updateTabs();
            fireChangeListeners();
            // we actually keep the controls in memory, we simply stop displaying them
        } else if (nControls > controls.size()) {
            // add controls
            int n = nControls - controls.size();
            for (int k = 0; k < n; k++) {
                addHydraulicControl();
            }
            updateTabs();
            fireChangeListeners();
        }
    }

    public void setHydraulicControls(List<OneHydraulicControl> controls) {
        int n = controls.size();
        setHydraulicControls(n);
        for (int k = 0; k < n; k++) {
            this.controls.get(k).activationStage.setValue(controls.get(k).activationStage.getValue());
            this.controls.get(k).activationStageUncertainty
                    .setValue(controls.get(k).activationStageUncertainty.getValue());
            this.controls.get(k).coefficient.setValue(controls.get(k).coefficient.getValue());
            this.controls.get(k).coefficientUncertainty
                    .setValue(controls.get(k).coefficientUncertainty.getValue());
            this.controls.get(k).exponent.setValue(controls.get(k).exponent.getValue());
            this.controls.get(k).exponentUncertainty
                    .setValue(controls.get(k).exponentUncertainty.getValue());
            this.controls.get(k).updateTextFields();
        }
        fireChangeListeners();
    }

    public List<OneHydraulicControl> getHydraulicControls() {
        return controls.subList(0, nVisibleHydraulicControls);
    }

    @Override
    public Parameter[] getParameters() {
        Parameter[] parameters = new Parameter[nVisibleHydraulicControls * 3];
        for (int k = 0; k < nVisibleHydraulicControls; k++) {
            OneHydraulicControl ohc = controls.get(k);
            Distribution activationStageDistribution = new Distribution(
                    DISTRIB.GAUSSIAN,
                    ohc.activationStage.getValue(),
                    ohc.activationStageUncertainty.getValue() / 2);
            Distribution coefficientDistribution = new Distribution(
                    DISTRIB.GAUSSIAN,
                    ohc.coefficient.getValue(),
                    ohc.coefficientUncertainty.getValue() / 2);
            Distribution exponentDistribution = new Distribution(
                    DISTRIB.GAUSSIAN,
                    ohc.exponent.getValue(),
                    ohc.exponentUncertainty.getValue() / 2);

            parameters[k * 3 + 0] = new Parameter("k_" + k,
                    ohc.activationStage.getValue(),
                    activationStageDistribution);
            parameters[k * 3 + 1] = new Parameter("a_" + k,
                    ohc.coefficient.getValue(),
                    coefficientDistribution);
            parameters[k * 3 + 2] = new Parameter("c_" + k,
                    ohc.exponent.getValue(),
                    exponentDistribution);
        }
        return parameters;
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        fireChangeListeners();
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

}
