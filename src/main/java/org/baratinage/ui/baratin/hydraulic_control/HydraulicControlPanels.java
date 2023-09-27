package org.baratinage.ui.baratin.hydraulic_control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.Distribution.DISTRIBUTION;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.baratin.HydraulicConfiguration;
import org.baratinage.ui.component.SimpleTabContainer;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class HydraulicControlPanels extends RowColPanel implements IPriors, ChangeListener {

    private final List<OneHydraulicControl> controls;
    private int nVisibleHydraulicControls = 0;

    private final SimpleTabContainer tabs;

    public HydraulicControlPanels() {
        controls = new ArrayList<>();
        tabs = new SimpleTabContainer(SimpleTabContainer.LOC.LEFT);

        Lg.register(tabs, () -> {
            updateTabs();
        });

        appendChild(tabs, 1);
    }

    private void updateTabs() {
        int nTabs = tabs.getTabCount();
        if (nTabs > nVisibleHydraulicControls) {
            for (int k = nVisibleHydraulicControls; k < nTabs; k++) {
                tabs.remove(k);
            }
        } else if (nTabs < nVisibleHydraulicControls) {
            for (int k = nTabs; k < nVisibleHydraulicControls; k++) {
                OneHydraulicControl ohc = controls.get(k);
                tabs.addTab(
                        Lg.html("control_number", ohc.controlNumber),
                        HydraulicConfiguration.priorSpecificationIcon,
                        ohc);
            }
        }
    }

    private void addHydraulicControl() {
        int n = controls.size() + 1;
        OneHydraulicControl ohc = new OneHydraulicControl(n);
        ohc.addChangeListener(this);
        Lg.register(ohc, () -> {
            String text = Lg.html("control_number", ohc.controlNumber);
            ohc.nameLabel.setText(text);
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
            // Note: we actually keep the controls in memory, we simply stop displaying them
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
            this.controls.get(k).activationStage.setValue(controls.get(k).activationStage.getDoubleValue());
            this.controls.get(k).activationStageUncertainty
                    .setValue(controls.get(k).activationStageUncertainty.getDoubleValue());
            this.controls.get(k).coefficient.setValue(controls.get(k).coefficient.getDoubleValue());
            this.controls.get(k).coefficientUncertainty
                    .setValue(controls.get(k).coefficientUncertainty.getDoubleValue());
            this.controls.get(k).exponent.setValue(controls.get(k).exponent.getDoubleValue());
            this.controls.get(k).exponentUncertainty
                    .setValue(controls.get(k).exponentUncertainty.getDoubleValue());
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
                    DISTRIBUTION.GAUSSIAN,
                    ohc.activationStage.getDoubleValue(),
                    ohc.activationStageUncertainty.getDoubleValue() / 2);
            Distribution coefficientDistribution = new Distribution(
                    DISTRIBUTION.GAUSSIAN,
                    ohc.coefficient.getDoubleValue(),
                    ohc.coefficientUncertainty.getDoubleValue() / 2);
            Distribution exponentDistribution = new Distribution(
                    DISTRIBUTION.GAUSSIAN,
                    ohc.exponent.getDoubleValue(),
                    ohc.exponentUncertainty.getDoubleValue() / 2);

            parameters[k * 3 + 0] = new Parameter("k_" + k,
                    ohc.activationStage.getDoubleValue(),
                    activationStageDistribution);
            parameters[k * 3 + 1] = new Parameter("a_" + k,
                    ohc.coefficient.getDoubleValue(),
                    coefficientDistribution);
            parameters[k * 3 + 2] = new Parameter("c_" + k,
                    ohc.exponent.getDoubleValue(),
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
