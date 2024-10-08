package org.baratinage.ui.baratin.baratin_qfh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
            QFHTextFileEquation eq = getCurrentTextFileEquation();
            updateUI();
            if (eq == null) {
                return;
            }
            presetContentPanel.appendChild(eq, 1);
            priorsPanel = eq.priorsPanel;
            fireChangeListeners(); // changed equation type
        });

        presetComboBox.setSelectedItem(0);

    }

    public boolean isModelDefinitionValid() {
        QFHTextFileEquation currentEq = getCurrentTextFileEquation();
        if (currentEq == null) {
            return false;
        }
        return currentEq.isQFHEquationValid();
    }

    private QFHTextFileEquation getCurrentTextFileEquation() {
        int index = presetComboBox.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        String eqId = eqIds.get(index);
        return equationPanels.get(eqId);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelId'");
    }

    @Override
    public int getNumberOfParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberOfParameters'");
    }

    @Override
    public String[] getInputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInputNames'");
    }

    @Override
    public String[] getOutputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputNames'");
    }

    @Override
    public String getXtra(String workspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getXtra'");
    }

}
