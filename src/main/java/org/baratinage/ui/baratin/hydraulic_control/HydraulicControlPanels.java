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
import org.baratinage.ui.lg.Lg;
import org.json.JSONArray;
import org.json.JSONObject;

public class HydraulicControlPanels extends RowColPanel implements IPriors, ChangeListener {

    private final List<OneHydraulicControl> controls;
    private int nVisibleHydraulicControls = 0;

    private final TabContainer tabs;

    public HydraulicControlPanels() {
        controls = new ArrayList<>();
        tabs = new TabContainer(TabContainer.SIDE.TOP);

        Lg.register(this, () -> {
            int n = tabs.getTabCount();
            for (int k = 0; k < n; k++) {
                OneHydraulicControl ohc = controls.get(k);
                tabs.setTitleTextAt(k, Lg.html("control_number", ohc.controlNumber));
            }
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
                        BamItemType.HYDRAULIC_CONFIG.getIcon(),
                        ohc);
            }
        }
    }

    private void addHydraulicControl() {
        int n = controls.size() + 1;
        OneHydraulicControl ohc = new OneHydraulicControl(n);
        ohc.addChangeListener(this);
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
            // FIXME: might be confusing for the user by the way...
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

    public List<OneHydraulicControl> getHydraulicControls() {
        return controls.subList(0, nVisibleHydraulicControls);
    }

    @Override
    public Parameter[] getParameters() {
        Parameter[] parameters = new Parameter[nVisibleHydraulicControls * 3];

        for (int k = 0; k < nVisibleHydraulicControls; k++) {
            Parameter[] pars = controls.get(k).getParameters();
            if (pars == null) {
                return null;
            }
            parameters[k * 3 + 0] = pars[0].getRenamedClone("k_" + k);
            parameters[k * 3 + 1] = pars[1].getRenamedClone("a_" + k);
            parameters[k * 3 + 2] = pars[2].getRenamedClone("c_" + k);
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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray controlsJSON = new JSONArray();
        for (int k = 0; k < nVisibleHydraulicControls; k++) {
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
