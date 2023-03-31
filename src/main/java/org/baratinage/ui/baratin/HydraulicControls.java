package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import org.baratinage.ui.component.NumberField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class HydraulicControls extends RowColPanel {

    private record ControlItem(String id, String label, String icon) {
        public String toString() {
            return label();
        }
    }

    JList<ControlItem> controlSelector;
    DefaultListModel<ControlItem> controlSelectorModel;

    List<HydraulicControl> controls;
    RowColPanel currentControl;

    JSplitPane splitPaneContainer;

    public HydraulicControls() {
        super(AXIS.COL);

        controls = new ArrayList<>();

        controlSelector = new JList<>();
        controlSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        controlSelector.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ControlItem controlItem = controlSelector.getSelectedValue();
                if (controlItem != null) {
                    controls.stream()
                            .filter(ctrl -> ctrl.id.equals(controlItem.id()))
                            .findFirst()
                            .ifPresentOrElse((ctrl) -> {
                                currentControl.clear();
                                currentControl.appendChild(ctrl);
                                currentControl.updateUI();
                            }, () -> {
                                currentControl.clear();
                                currentControl.updateUI();
                            });
                } else {
                    currentControl.clear();
                    currentControl.updateUI();
                }

            }
        });
        controlSelectorModel = new DefaultListModel<>();
        controlSelector.setModel(controlSelectorModel);

        currentControl = new RowColPanel();

        RowColPanel listPanel = new RowColPanel(AXIS.COL);

        JScrollPane listScrollContainer = new JScrollPane(listPanel);
        listScrollContainer.setBorder(BorderFactory.createEmptyBorder());

        listPanel.appendChild(controlSelector);
        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());

        splitPaneContainer.setLeftComponent(listScrollContainer);
        splitPaneContainer.setRightComponent(currentControl);

        this.appendChild(splitPaneContainer);

    }

    public void setNumberOfControls(int n) {
        int m = controlSelectorModel.size();
        if (m > n) {
            int d = m - n;
            System.out.println("Deleting the " + d + " controls no longer needed");
            for (int k = 0; k < d; k++) {
                controlSelectorModel.remove(m - k - 1);
            }
        } else if (m < n) {
            // int d = n - m;q
            for (int k = m; k < n; k++) {
                ControlItem ctrl = new ControlItem(UUID.randomUUID().toString(),
                        "Contrôle #" + (k + 1), null);
                controlSelectorModel.addElement(ctrl);
                controls.add(new HydraulicControl(ctrl.id(), ctrl.label()));
            }
        } else {
            System.out.println("Number of controls are already matching!");
        }
    }

    private class HydraulicControl extends RowColPanel {
        public final String id;

        private GridPanel parametersPanel;
        private NumberField activationStage;
        private NumberField activationStageUncertainty;
        private NumberField coefficient;
        private NumberField coefficientUncertainty;
        private NumberField exponent;
        private NumberField exponentUncertainty;

        public HydraulicControl(String id, String label) {
            super(AXIS.COL);
            this.id = id;

            this.appendChild(new JLabel(label), 0, 5);
            this.appendChild(new JSeparator(), 0);

            parametersPanel = new GridPanel();
            parametersPanel.setAnchor(ANCHOR.N);
            parametersPanel.setGap(5);
            parametersPanel.setPadding(5);
            parametersPanel.setColWeight(1, 1);
            parametersPanel.setColWeight(2, 1);

            parametersPanel.insertChild(new JLabel("<html>Valeur <i>a priori</i></html> "), 1, 0);
            parametersPanel.insertChild(new JLabel("+/- (Incertitude élargie)"), 2, 0);

            JLabel activationStageLabel = new JLabel("k - Hauteur d'activation");
            activationStage = new NumberField();
            activationStageUncertainty = new NumberField();
            parametersPanel.insertChild(activationStageLabel, 0, 1);
            parametersPanel.insertChild(activationStage, 1, 1);
            parametersPanel.insertChild(activationStageUncertainty, 2, 1);

            JLabel coefficientLabel = new JLabel("a - Coefficient");
            coefficient = new NumberField();
            coefficientUncertainty = new NumberField();
            parametersPanel.insertChild(coefficientLabel, 0, 2);
            parametersPanel.insertChild(coefficient, 1, 2);
            parametersPanel.insertChild(coefficientUncertainty, 2, 2);

            JLabel exponentLabel = new JLabel("c - Exposant");
            exponent = new NumberField();
            exponentUncertainty = new NumberField();
            parametersPanel.insertChild(exponentLabel, 0, 3);
            parametersPanel.insertChild(exponent, 1, 3);
            parametersPanel.insertChild(exponentUncertainty, 2, 3);

            this.appendChild(parametersPanel);

        }
    }

}
