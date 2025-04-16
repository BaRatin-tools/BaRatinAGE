package org.baratinage.ui.baratin.baratin_qfh;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.jbam.utils.ConfigFile;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.baratin.HydraulicConfigurationQFH;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONArray;
import org.json.JSONObject;

public class QFHModelDefinition extends RowColPanel implements IModelDefinition {

    private final String id;
    private final SimpleComboBox configSelectionCombobox = new SimpleComboBox();

    private final RowColPanel configPanel = new RowColPanel(AXIS.COL);

    private final Map<String, QFHTextFileEquation> textFileEquations;
    private QFHTextFileEquation currentTextFileEquation;

    public QFHPriors priorsPanel;

    public QFHModelDefinition() {
        super(AXIS.COL);

        id = Misc.getTimeStampedId();
        setPadding(5);
        setGap(5);

        textFileEquations = new LinkedHashMap<>();

        addTextFileEquation("custom_qfh");
        for (QFHPreset preset : QFHPreset.PRESETS) {
            addTextFileEquation(preset);
        }

        appendChild(configSelectionCombobox, 0);
        appendChild(configPanel, 1);

        configSelectionCombobox.addChangeListener((e) -> {
            configPanel.clear();

            int index = configSelectionCombobox.getSelectedIndex();

            String configId = getConfigId(index);
            currentTextFileEquation = textFileEquations.get(configId);

            updateUI();
            if (currentTextFileEquation == null) {
                priorsPanel = null;
                fireChangeListeners(); // changed equation type
                return;
            }
            configPanel.appendChild(currentTextFileEquation, 1);
            priorsPanel = currentTextFileEquation.priorsPanel;
            fireChangeListeners(); // changed equation type
        });

        Misc.setMinimumSize(this, null, 200);

        buildConfigSelectionCombobox();
    }

    private JLabel buildComboboxLabel(String id, String iconId) {
        Icon icon = HydraulicConfigurationQFH.equationQFHIcon;
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

    private void buildConfigSelectionCombobox() {
        int selectedIndex = configSelectionCombobox.getSelectedIndex();

        JLabel[] labels = new JLabel[textFileEquations.size()];
        int k = 0;
        for (String pId : textFileEquations.keySet()) {
            QFHPreset p = QFHPreset.getPreset(pId);
            if (pId.startsWith("custom")) {
                labels[k] = buildComboboxLabel("custom_equation", null);
            } else if (p == null) {
                labels[k] = buildComboboxLabel(pId, null);
            } else {
                labels[k] = buildComboboxLabel(p.id(), p.iconId());
            }
            k++;
        }
        configSelectionCombobox.setItems(labels, true);

        if (selectedIndex >= 0 && selectedIndex < configSelectionCombobox.getItemCount()) {
            configSelectionCombobox.setSelectedItem(selectedIndex);
        }
    }

    private void setConfigSelectionFromId(String id) {
        if (id.startsWith("custom")) {
            configSelectionCombobox.setSelectedItem(0, false);
        } else {
            int index = 0;
            for (String pId : textFileEquations.keySet()) {
                if (pId.equals(id)) {
                    configSelectionCombobox.setSelectedItem(index, false);
                    return;
                }
                index++;
            }
        }
        ConsoleLogger.error(String.format("No config with id '%s' found!", id));
    }

    private void addTextFileEquation(String id) {
        addTextFileEquation(id, null);
    }

    private void addTextFileEquation(QFHPreset preset) {
        addTextFileEquation(preset.id(), preset);
    }

    private void addTextFileEquation(String id, QFHPreset preset) {
        QFHTextFileEquation txtFileEq = preset == null ? new QFHTextFileEquation() : new QFHTextFileEquation(preset);
        txtFileEq.addChangeListener(l -> {
            fireChangeListeners(); // changes in the equation
        });
        txtFileEq.priorsPanel.addChangeListener(l -> {
            fireChangeListeners(); // changes in parameters
        });
        textFileEquations.put(id, txtFileEq);
    }

    private String getConfigId() {
        return getConfigId(configSelectionCombobox.getSelectedIndex());
    }

    private String getConfigId(int index) {
        if (index < 0 || index > textFileEquations.size()) {
            return null;
        }

        String pId = new ArrayList<>(textFileEquations.keySet()).get(index);
        return pId;
    }

    public boolean isModelDefinitionValid() {
        if (currentTextFileEquation == null) {
            return false;
        }
        return currentTextFileEquation.isQFHEquationValid();
    }

    private List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        TimedActions.debounce("qfh_modeldef_" + id, 50, () -> {
            for (ChangeListener l : changeListeners) {
                l.stateChanged(new ChangeEvent(this));
            }
        });
    }

    @Override
    public String getModelId() {
        return "TextFile";
    }

    @Override
    public int getNumberOfParameters() {
        if (currentTextFileEquation == null) {
            return 0;
        }
        return currentTextFileEquation.getParameterNames().size();
    }

    @Override
    public String[] getInputNames() {
        return new String[] { currentTextFileEquation.getStageVariableName() };
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
        String[] parameterNames = currentTextFileEquation.getParameterNames().toArray(new String[0]);
        cf.addItem(parameterNames, "list of parameters, comma-separated ");
        cf.addItem(1, "number of output variables");
        cf.addItem(currentTextFileEquation.getEquation(), "formula");
        String[] xTraLines = cf.createFileLines();
        return String.join("\n", xTraLines);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        // preset id
        String pId = getConfigId();
        json.put("presetId", pId);
        // equation string and associated priors for all presets
        JSONArray eqConfigsAndPriors = new JSONArray();
        for (String presetId : textFileEquations.keySet()) {
            JSONObject eqJson = new JSONObject();
            QFHTextFileEquation eq = textFileEquations.get(presetId);
            eqJson.put("presetId", presetId);
            eqJson.put("eqConfigsAndPriors", eq.toJSON());
            eqConfigsAndPriors.put(eqJson);
        }
        json.put("eqConfigsAndPriors", eqConfigsAndPriors);
        // equation string and associated priors for selected preset
        // (currently used only for out of sync comparison purposes)
        if (pId != null) {
            QFHTextFileEquation eqPanel = textFileEquations.get(pId);
            if (eqPanel != null) {
                json.put("selectedEqConfigAndPriors", eqPanel.toJSON());
            }
        }
        return json;
    }

    public void fromJSON(JSONObject json) {
        JSONArray eqConfigAndPriorsConfig = json.optJSONArray("eqConfigsAndPriors");
        for (int k = 0; k < eqConfigAndPriorsConfig.length(); k++) {
            JSONObject eqJson = eqConfigAndPriorsConfig.getJSONObject(k);
            String pId = eqJson.optString("presetId", "");
            if (!textFileEquations.containsKey(pId)) {
                CommonDialog.warnDialog(T.text("missing_qfh_preset_warning", T.text(pId)));
                addTextFileEquation(pId);
                buildConfigSelectionCombobox();
            }
            QFHTextFileEquation eq = textFileEquations.get(pId);
            eq.fromJSON(eqJson.optJSONObject("eqConfigsAndPriors"));
        }

        String presetId = json.optString("presetId", "custom_qfh");
        setConfigSelectionFromId(presetId);
    }
}
