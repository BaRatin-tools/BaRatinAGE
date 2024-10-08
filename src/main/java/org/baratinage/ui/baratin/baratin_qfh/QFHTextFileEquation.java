package org.baratinage.ui.baratin.baratin_qfh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.textfile.EquationEditor;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

public class QFHTextFileEquation extends GridPanel {

    private final SimpleComboBox hSelectionCombobox;

    private final EquationEditor eqEditor;
    private final List<String> eqItems;
    private final HashSet<String> eqParameterNames;
    private String hVariableName;

    public final QFHPriors priorsPanel;

    public QFHTextFileEquation() {
        this(true);
    }

    public QFHTextFileEquation(boolean editable) {
        super();
        setGap(5);

        priorsPanel = new QFHPriors();
        eqItems = new ArrayList<>();
        eqParameterNames = new HashSet<>();
        hVariableName = null;

        String id = String.format("%s_%s", "baratin_qfh_custom_eq", Misc.getTimeStampedId());

        JLabel eqLabel = new JLabel();
        eqLabel.setText("Equation de la courbe de tarage");

        eqEditor = new EquationEditor();
        eqEditor.setRows(3);
        eqEditor.setEditable(editable);

        JScrollPane eqScrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        eqScrollPane.setViewportView(eqEditor);

        eqEditor.addChangeListener((e) -> {
            TimedActions.debounce(id, 250, () -> {
                updateStageVariableCombobox(eqEditor.getVariables());
                updateEquationParameters();
                fireChangeListeners(); // equation has changed
            });
        });

        JLabel hSelectionLabel = new JLabel();
        hSelectionLabel.setText("Variable \"hauteur d'eau\"");

        hSelectionCombobox = new SimpleComboBox();
        hSelectionCombobox.setEnabled(editable);
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
            eqParameterNames.clear();
            for (String s : eqItems) {
                if (!s.equals(hVariableName)) {
                    eqParameterNames.add(s);
                }
            }
            updateEquationParameters();
            fireChangeListeners(); // h variable selection has changed
        });
        hSelectionCombobox.setValidityView(false);

        setColWeight(0, 1);
        setRowWeight(1, 1);
        insertChild(eqLabel, 0, 0);
        insertChild(eqScrollPane, 0, 1);
        insertChild(hSelectionLabel, 0, 2);
        insertChild(hSelectionCombobox, 0, 3);

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

    public HashSet<String> getParameterNames() {
        return eqParameterNames;
    }

    public void setFromPreset(QFHPreset preset) {
        eqEditor.setText(preset.formula());
        hVariableName = preset.stageSymbole();
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

}
