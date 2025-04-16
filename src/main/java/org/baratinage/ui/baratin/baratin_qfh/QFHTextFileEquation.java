package org.baratinage.ui.baratin.baratin_qfh;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.baratin.baratin_qfh.QFHPreset.QFHPresetParameter;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.textfile.EquationEditor;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONArray;
import org.json.JSONObject;

public class QFHTextFileEquation extends GridPanel {

    private final QFHPreset preset;
    private final SimpleComboBox hSelectionCombobox;
    private final JLabel hVariableLabel;

    private final EquationEditor eqEditor;
    private final List<String> eqItems;
    private final Set<String> eqParameterNames;

    private final MsgPanel inconsistencyWarning;
    private boolean isConfigInconsistent = false;

    private String hVariableName;

    public final QFHPriors priorsPanel;

    public QFHTextFileEquation() {
        this(null);
    }

    public QFHTextFileEquation(QFHPreset preset) {
        super();
        setGap(5);

        this.preset = preset;

        priorsPanel = new QFHPriors();
        eqItems = new ArrayList<>();
        eqParameterNames = new LinkedHashSet<>();
        hVariableName = null;

        String id = String.format("%s_%s", "baratin_qfh_custom_eq", Misc.getTimeStampedId());

        eqEditor = new EquationEditor();
        eqEditor.setRows(3);
        eqEditor.setEditable(preset == null);

        hSelectionCombobox = new SimpleComboBox();
        hVariableLabel = new JLabel();
        hVariableLabel.setFont(hVariableLabel.getFont().deriveFont(Font.BOLD));

        inconsistencyWarning = new MsgPanel(MsgPanel.TYPE.ERROR, true);

        if (preset == null) {
            eqEditor.addChangeListener((e) -> {
                TimedActions.debounce(id, 250, () -> {
                    updateStageVariableCombobox(eqEditor.getVariables());
                    updateEquationParameters();
                    fireChangeListeners(); // equation has changed
                });
            });

            hSelectionCombobox.addChangeListener(l -> {
                int index = hSelectionCombobox.getSelectedIndex();
                if (index == -1) {
                    hVariableName = null;
                    hSelectionCombobox.setValidityView(false);
                    fireChangeListeners(); // h variable selection has changed
                    return;
                } else if (index >= eqItems.size()) {
                    hVariableName = null; // Invalid: should not happend
                    hSelectionCombobox.setValidityView(false);
                    fireChangeListeners();
                    return;
                }
                hVariableName = eqItems.get(index);
                hSelectionCombobox.setValidityView(true);
                updateEquationParameters();
                fireChangeListeners(); // h variable selection has changed
            });
            hSelectionCombobox.setValidityView(false);
        } else {
            setFromPreset(preset);
        }

        buildUI(preset == null);

    }

    private void buildUI(boolean editable) {

        eqEditor.setEditable(editable);

        JLabel hSelectionLabel = new JLabel();

        JLabel eqLabel = new JLabel();

        JScrollPane eqScrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        eqScrollPane.setViewportView(eqEditor);

        clear();
        setColWeight(0, 1);
        setRowWeight(1, 1);
        int rowIndex = 0;
        insertChild(eqLabel, 0, rowIndex);
        rowIndex++;
        insertChild(eqScrollPane, 0, rowIndex);
        rowIndex++;
        if (editable) {
            insertChild(hSelectionLabel, 0, rowIndex);
            rowIndex++;
            insertChild(hSelectionCombobox, 0, rowIndex);
            rowIndex++;
        } else {
            RowColPanel p = new RowColPanel();
            p.setMainAxisAlign(RowColPanel.ALIGN.START);
            p.setGap(5);
            p.appendChild(hSelectionLabel, 0);
            p.appendChild(hVariableLabel, 1);
            insertChild(p, 0, rowIndex);
            rowIndex++;
        }
        if (isConfigInconsistent) {
            insertChild(inconsistencyWarning, 0, rowIndex);
            rowIndex++;
        }

        T.clear(this);
        T.t(this, eqLabel, false, "rc_equation");
        T.t(this, hSelectionLabel, false, "stage_variable_name");
        if (isConfigInconsistent && preset != null) {
            T.t(this, () -> {
                inconsistencyWarning.message.setText(T.text(
                        "inconsistent_qfh_preset_warning", T.text(preset.id())));
            });
        }

    }

    public boolean isQFHEquationValid() {
        return eqEditor.isEquationValid() && hVariableName != null;
    }

    private void updateStageVariableCombobox(HashSet<String> idEqItems) {
        eqItems.clear();
        eqItems.addAll(idEqItems);

        String[] items = eqItems.toArray(new String[0]);

        int hSelectedVariableIndex = -1;
        for (int k = 0; k < items.length; k++) {
            if (items[k].equals(hVariableName)) {
                hSelectedVariableIndex = k;
            }
        }

        hSelectionCombobox.setChangeListenersEnabled(false);
        hSelectionCombobox.clearItems();
        hSelectionCombobox.setItems(items);
        if (hSelectedVariableIndex != -1) {
            hSelectionCombobox.setSelectedItem(hSelectedVariableIndex);
        }
        hSelectionCombobox.setChangeListenersEnabled(true);

    }

    private void updateEquationParameters() {
        eqParameterNames.clear();
        for (String eqItem : eqItems) {
            if (!eqItem.equals(hVariableName)) {
                eqParameterNames.add(eqItem);
            }
        }
        priorsPanel.updateUsedParNames(eqParameterNames);

    }

    public String getEquation() {
        return eqEditor.getText();
    }

    public Set<String> getParameterNames() {
        return eqParameterNames;
    }

    public String getStageVariableName() {
        return hVariableName;
    }

    private void setFromPreset(QFHPreset preset) {
        eqEditor.setText(preset.formula());
        hVariableName = preset.stageSymbole();
        hVariableLabel.setText(hVariableName);
        eqParameterNames.clear();
        for (QFHPresetParameter QFHPar : preset.parameters()) {
            eqParameterNames.add(QFHPar.symbole());
        }
        priorsPanel.updateUsedParNames(eqParameterNames);
        priorsPanel.setFromPreset(preset);
    }

    private List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("equation", eqEditor.getText());
        json.put("stageVarName", hVariableName);
        json.put("priors", priorsPanel.toJSON());
        return json;
    }

    public void fromJSON(JSONObject json) {
        String eqString = json.optString("equation", "");
        String hVarName = json.optString("stageVarName", null);
        if (preset != null) {
            boolean isValid = true;
            if (!hVariableName.equals(hVarName)) {
                isValid = false;
                ConsoleLogger.error(String.format("Cannot configure QFH config '%s': mismatch in stage variable name",
                        preset.id()));
            }
            if (!eqString.equals(eqEditor.getText())) {
                isValid = false;
                ConsoleLogger.error(String.format("Cannot configure QFH config '%s': mismatch in equation",
                        preset.id()));
            }
            if (!isValid) {
                isConfigInconsistent = true;
                eqEditor.setText(eqString);
                updateStageVariableCombobox(eqEditor.getVariables());
                updateEquationParameters();
                hVariableName = hVarName;
                buildUI(true);
            }
        } else {
            eqEditor.setText(eqString);
            hVariableName = hVarName;
            updateStageVariableCombobox(eqEditor.getVariables());
            updateEquationParameters();
        }
        JSONArray priorsJson = json.optJSONArray("priors");
        if (priorsJson != null) {
            priorsPanel.fromJSON(priorsJson, preset == null || isConfigInconsistent);
        }
    }

}
