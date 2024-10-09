package org.baratinage.ui.baratin.baratin_qfh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.baratin.baratin_qfh.QFHPreset.RatingCurveEquationParameter;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class QFHModelDefinition extends RowColPanel implements IModelDefinition {

    static private final List<QFHPreset> eqPresets = new ArrayList<>();

    static {
        try {
            String jsonContent = ReadFile.readTextFile("resources/baratin_qfh_presets.json");
            JSONArray json = new JSONArray(jsonContent);
            for (int i = 0; i < json.length(); i++) {
                JSONObject presetJson = json.getJSONObject(i);
                JSONArray presetParametersJson = presetJson.getJSONArray("parameters");
                List<RatingCurveEquationParameter> presetParameters = new ArrayList<>();
                for (int j = 0; j < presetParametersJson.length(); j++) {
                    JSONObject presetParJson = presetParametersJson.getJSONObject(j);
                    presetParameters.add(new RatingCurveEquationParameter(presetParJson.getString("symbole"),
                            presetParJson.getString("type")));
                }
                eqPresets.add(new QFHPreset(
                        presetJson.getString("id"), presetJson.getString("formula"),
                        presetJson.getString("stage_symbole"), presetParameters));
            }
        } catch (IOException | JSONException e) {
            ConsoleLogger.error(e);
        }
    }

    private final SimpleComboBox presetComboBox = new SimpleComboBox();

    private final RowColPanel presetContentPanel = new RowColPanel(AXIS.COL);

    private final HashMap<String, QFHTextFileEquation> equationPanels;
    private QFHTextFileEquation currentEquationPanel;

    public QFHPriors priorsPanel;

    private final List<String> eqIds;

    public QFHModelDefinition() {
        super(AXIS.COL);

        setPadding(5);
        setGap(5);

        equationPanels = new HashMap<>();

        eqIds = new ArrayList<>();
        eqIds.add("custom");
        QFHTextFileEquation eqPanel = new QFHTextFileEquation(true);
        eqPanel.addChangeListener(l -> {
            fireChangeListeners(); // custom equation or stage variable selection has changed
        });
        equationPanels.put("custom", eqPanel);
        for (QFHPreset preset : eqPresets) {
            String eqId = preset.id();
            eqIds.add(eqId);
            eqPanel = new QFHTextFileEquation(false);
            eqPanel.setFromPreset(preset);
            equationPanels.put(eqId, eqPanel);
        }

        presetComboBox.setItems(eqIds.toArray(new String[0]));

        appendChild(presetComboBox, 0);
        appendChild(presetContentPanel, 1);

        presetComboBox.addChangeListener((e) -> {
            presetContentPanel.clear();

            int index = presetComboBox.getSelectedIndex();

            currentEquationPanel = index >= 0 && index < eqIds.size() ? equationPanels.get(eqIds.get(index)) : null;

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

        presetComboBox.setSelectedItem(0);

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
        int presetIndex = presetComboBox.getSelectedIndex();
        json.put("presetId", presetIndex >= 0 && presetIndex < eqIds.size() ? eqIds.get(presetIndex) : null);
        // equation string for all presets
        JSONArray eqConfigsAndPriors = new JSONArray();
        for (String presetId : equationPanels.keySet()) {
            JSONObject eqJson = new JSONObject();
            QFHTextFileEquation eq = equationPanels.get(presetId);
            eqJson.put("presetId", presetId);
            eqJson.put("eqConfigsAndPriors", eq.toJSON());
            eqConfigsAndPriors.put(eqJson);
        }
        json.put("eqConfigsAndPriors", eqConfigsAndPriors);
        return json;
    }

    public void fromJSON(JSONObject json) {
        String presetId = json.optString("presetId", "custom");
        int presetIndex = eqIds.indexOf(presetId);
        presetComboBox.setSelectedItem(presetIndex);
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
