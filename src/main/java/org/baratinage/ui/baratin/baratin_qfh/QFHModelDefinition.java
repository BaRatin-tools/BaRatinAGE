package org.baratinage.ui.baratin.baratin_qfh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.baratin.HydraulicConfigurationQFH;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONArray;
import org.json.JSONObject;

public class QFHModelDefinition extends RowColPanel implements IModelDefinition {

    private final SimpleComboBox presetComboBox = new SimpleComboBox();

    private final RowColPanel presetContentPanel = new RowColPanel(AXIS.COL);

    private final HashMap<String, QFHTextFileEquation> equationPanels;
    private QFHTextFileEquation currentEquationPanel;

    public QFHPriors priorsPanel;

    public QFHModelDefinition() {
        super(AXIS.COL);

        setPadding(5);
        setGap(5);

        equationPanels = new HashMap<>();

        QFHTextFileEquation eqPanel = new QFHTextFileEquation(true);
        eqPanel.addChangeListener(l -> {
            fireChangeListeners(); // custom equation or stage variable selection has changed
        });
        eqPanel.priorsPanel.addChangeListener(l -> {
            fireChangeListeners(); // changes in parameters
        });
        equationPanels.put("custom_qfh", eqPanel);
        for (QFHPreset preset : QFHPreset.PRESETS) {
            String eqId = preset.id();
            eqPanel = new QFHTextFileEquation(false);
            eqPanel.setFromPreset(preset);
            eqPanel.priorsPanel.addChangeListener(l -> {
                fireChangeListeners(); // changes in parameters
            });
            equationPanels.put(eqId, eqPanel);
        }

        appendChild(presetComboBox, 0);
        appendChild(presetContentPanel, 1);

        presetComboBox.addChangeListener((e) -> {
            presetContentPanel.clear();

            int index = presetComboBox.getSelectedIndex();

            // currentEquationPanel = index >= 0 && index < eqIds.size() ?
            // equationPanels.get(eqIds.get(index)) : null;
            currentEquationPanel = equationPanels.get(getPresetId(index));

            updateUI();
            if (currentEquationPanel == null) {
                priorsPanel = null;
                fireChangeListeners(); // changed equation type
                return;
            }
            presetContentPanel.appendChild(currentEquationPanel, 1);
            priorsPanel = currentEquationPanel.priorsPanel;
            fireChangeListeners(); // changed equation type
        });

        // presetComboBox.setSelectedItem(0);

        // resetPresetComboxBox();

        JLabel[] labels = new JLabel[QFHPreset.PRESETS.size() + 1];
        labels[0] = buildComboboxLabel("custom_equation", null);
        int k = 1;
        for (QFHPreset p : QFHPreset.PRESETS) {
            labels[k] = buildComboboxLabel(p.id(), p.iconId());
            k++;
        }
        presetComboBox.setItems(labels, true);

    }

    private JLabel buildComboboxLabel(String id, String iconId) {
        Icon icon = HydraulicConfigurationQFH.equationQFHIcon;
        ;
        if (iconId != null) {
            try {
                icon = AppSetup.ICONS.getCustomAppImageIcon(String.format("%s.svg", iconId));
            } catch (Exception e) {
                ConsoleLogger.warn(String.format("No icon found for preset '%s'.", id));
            }
        }
        JLabel label = new JLabel();
        label.setIcon(icon);
        T.t(this, label, false, id);
        return label;
    }

    private void setPresetComboboxFromId(String id) {
        if (id.startsWith("custom")) {
            presetComboBox.setSelectedItem(0, false);
        } else {
            int index = 0;
            for (QFHPreset p : QFHPreset.PRESETS) {
                index++;
                if (p.id().equals(id)) {
                    presetComboBox.setSelectedItem(index, false);
                    return;
                }
            }
        }
    }

    private String getPresetId() {
        return getPresetId(presetComboBox.getSelectedIndex());
    }

    private String getPresetId(int index) {
        if (index < 0 || index > QFHPreset.PRESETS.size()) {
            return null;
        }
        return index == 0 ? "custom_qfh" : QFHPreset.PRESETS.get(index - 1).id();
    }

    public boolean isModelDefinitionValid() {
        if (currentEquationPanel == null) {
            return false;
        }
        return currentEquationPanel.isQFHEquationValid();
    }

    private List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public String getModelId() {
        return "TextFile";
    }

    @Override
    public int getNumberOfParameters() {
        if (currentEquationPanel == null) {
            return 0;
        }
        return currentEquationPanel.getParameterNames().size();
    }

    @Override
    public String[] getInputNames() {
        return new String[] { currentEquationPanel.getStageVariableName() };
    }

    @Override
    public String[] getOutputNames() {
        return new String[] { "Q" };
    }

    @Override
    public String getXtra(String workspace) {
        ConfigFile cf = new ConfigFile();
        cf.addItem(1, "number of input variables");
        cf.addItem(getInputNames(), "list of input variables, comma-separated");
        cf.addItem(getNumberOfParameters(), "number of parameters");
        String[] parameterNames = currentEquationPanel.getParameterNames().toArray(new String[0]);
        cf.addItem(parameterNames, "list of parameters, comma-separated ");
        cf.addItem(1, "number of output variables");
        cf.addItem(currentEquationPanel.getEquation(), "formula");
        String[] xTraLines = cf.createFileLines();
        return String.join("\n", xTraLines);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        // preset id
        json.put("presetId", getPresetId());
        // equation string and associated priors for all presets
        JSONArray eqConfigsAndPriors = new JSONArray();
        for (String presetId : equationPanels.keySet()) {
            JSONObject eqJson = new JSONObject();
            QFHTextFileEquation eq = equationPanels.get(presetId);
            eqJson.put("presetId", presetId);
            eqJson.put("eqConfigsAndPriors", eq.toJSON());
            eqConfigsAndPriors.put(eqJson);
        }
        json.put("eqConfigsAndPriors", eqConfigsAndPriors);
        // equation string and associated priors for selected preset
        // (currently used only for out of sync comparison purposes)
        json.put("selectedEqConfigAndPriors", equationPanels.get(getPresetId()).toJSON());
        return json;
    }

    public void fromJSON(JSONObject json) {
        String presetId = json.optString("presetId", "custom_qfh");
        setPresetComboboxFromId(presetId);
        JSONArray eqConfigAndPriorsConfig = json.optJSONArray("eqConfigsAndPriors");
        for (int k = 0; k < eqConfigAndPriorsConfig.length(); k++) {
            JSONObject eqJson = eqConfigAndPriorsConfig.getJSONObject(k);
            String pId = eqJson.optString("presetId", "");
            if (!equationPanels.containsKey(pId)) {
                continue;
            }
            QFHTextFileEquation eq = equationPanels.get(pId);
            eq.fromJSON(eqJson.optJSONObject("eqConfigsAndPriors"));
        }
    }
}
