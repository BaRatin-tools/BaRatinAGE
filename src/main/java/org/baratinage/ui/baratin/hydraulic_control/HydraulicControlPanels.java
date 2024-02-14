package org.baratinage.ui.baratin.hydraulic_control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.json.JSONArray;
import org.json.JSONObject;

public class HydraulicControlPanels extends RowColPanel implements IPriors {

    private final List<OneHydraulicControl> controls;
    private final TabContainer tabs;
    private final boolean kMode;

    public HydraulicControlPanels() {
        this(true);
    }

    public HydraulicControlPanels(boolean kMode) {
        this.kMode = kMode;
        controls = new ArrayList<>();
        tabs = new TabContainer(TabContainer.SIDE.TOP);

        appendChild(tabs, 1);

        T.t(this, () -> {
            int n = tabs.getTabCount();
            for (int k = 0; k < n; k++) {
                OneHydraulicControl ohc = controls.get(k);
                tabs.setTitleAt(k, T.html("control_nbr", ohc.controlNumber));
            }
        });
    }

    private void updateTabs() {
        int nTabs = tabs.getTabCount();
        int nCtrl = controls.size();
        if (nTabs > nCtrl) {
            for (int k = nTabs - 1; k >= nCtrl; k--) {
                tabs.remove(k);
            }
        } else if (nTabs < nCtrl) {
            for (int k = nTabs; k < nCtrl; k++) {
                OneHydraulicControl ohc = controls.get(k);
                tabs.addTab(
                        T.html("control_nbr", ohc.controlNumber),
                        BamItemType.HYDRAULIC_CONFIG.getIcon(),
                        ohc);
            }
        }
        tabs.updateUI();
    }

    private void addHydraulicControl() {
        int n = controls.size() + 1;
        OneHydraulicControl ohc = new OneHydraulicControl(kMode, n);
        ohc.addChangeListener((chEvt) -> {
            fireChangeListeners();
        });
        controls.add(ohc);
        T.updateHierarchy(this, ohc);
    }

    public void setHydraulicControls(int nControls) {
        if (nControls > controls.size()) {
            // add controls
            int n = nControls - controls.size();
            for (int k = 0; k < n; k++) {
                addHydraulicControl();
            }
        } else if (nControls < controls.size()) {
            for (int k = controls.size() - 1; k >= nControls; k--) {
                OneHydraulicControl ohc = controls.remove(k);
                T.clear(ohc);
            }
        }
        updateTabs();
        fireChangeListeners();

    }

    public List<OneHydraulicControl> getHydraulicControls() {
        return controls;
    }

    @Override
    public Parameter[] getParameters() {
        int n = controls.size();
        Parameter[] parameters = new Parameter[n * 3];

        for (int k = 0; k < n; k++) {
            Parameter[] pars = controls.get(k).getParameters();
            if (pars == null) {
                return null;
            }
            parameters[k * 3 + 0] = pars[0].getRenamedClone((kMode ? "k_" : "b_") + k);
            parameters[k * 3 + 1] = pars[1].getRenamedClone("a_" + k);
            parameters[k * 3 + 2] = pars[2].getRenamedClone("c_" + k);
        }
        return parameters;
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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray controlsJSON = new JSONArray();
        int nCtrl = controls.size();
        for (int k = 0; k < nCtrl; k++) {
            controlsJSON.put(k, controls.get(k).toJSON());
        }
        json.put("controls", controlsJSON);
        return json;

    }

    public void fromJSON(JSONObject json) {
        JSONArray controlsJSON = json.getJSONArray("controls");
        int n = controlsJSON.length();
        setHydraulicControls(n);
        for (int k = 0; k < n; k++) {
            controls.get(k).fromJSON(controlsJSON.getJSONObject(k));

        }
    }

}
