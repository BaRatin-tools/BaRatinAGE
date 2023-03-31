package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class HydraulicControls extends RowColPanel {

    // JList listOfControls;
    // JListMo

    int numberOfControls;

    // List<ExplorerItem> controls;
    // Explorer controlExplorer;
    // List<ExplorerItem> controls;
    private record ControlItem(String id, String label, String icon) {
        public String toString() {
            return label();
        }
    }

    JList<ControlItem> controlSelector;
    DefaultListModel<ControlItem> controlSelectorModel;

    // A<String> controlSelector;
    List<HydraulicControl> controls;
    RowColPanel currentControl;

    JSplitPane splitPaneContainer;

    public HydraulicControls() {
        super(AXIS.COL);

        // NumberField field = new NumberField();
        // this.appendChild(field);

        controls = new ArrayList<>();

        // controlExplorer = new Explorer("");
        controlSelector = new JList<>();
        controlSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        controlSelector.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = controlSelector.getSelectedIndex();
                System.out.println("Selection is " + selectedIndex);
                currentControl.clear();
                if (selectedIndex >= 0 && selectedIndex < controls.size()) {
                    currentControl.appendChild(controls.get(selectedIndex));

                }
                // splitPaneContainer.updateUI();
            }
        });
        controlSelectorModel = new DefaultListModel<>();
        controlSelector.setModel(controlSelectorModel);

        // controlExplorer.appendItem(new ExplorerItem(TOOL_TIP_TEXT_KEY,
        // TOOL_TIP_TEXT_KEY, TOOL_TIP_TEXT_KEY))

        // EmptyBorder eb = new EmptyBorder(new Insets(0, 0, 0, 0));
        // controlSelector.setBorder(eb);
        currentControl = new RowColPanel();

        RowColPanel listPanel = new RowColPanel(AXIS.COL);

        JScrollPane listScrollContainer = new JScrollPane(listPanel);
        listScrollContainer.setBorder(BorderFactory.createEmptyBorder());

        listPanel.appendChild(controlSelector);
        JSplitPane splitPaneContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPaneContainer.setBorder(BorderFactory.createEmptyBorder());
        // splitPaneContainer = new SplitPanel(SplitPanel.DIR.H);
        // splitPaneContainer.setLeftComponent(controlExplorer);
        // splitPaneContainer.setLeftComponent(controlSelector);
        splitPaneContainer.setLeftComponent(listScrollContainer);
        splitPaneContainer.setRightComponent(currentControl);

        // splitPaneContainer.setResizeWeight(0.5);

        // controls = new ArrayList<>();

        // for (int k = 0; k < 30; k++) {
        // // controls.add(new ExplorerItem(
        // // "control_" + (k + 1),
        // // "control_" + (k + 1),
        // // null));
        // controlIdLabel ctrl = new controlIdLabel(UUID.randomUUID().toString(),
        // "Contrôle #" + (k + 1), null);
        // controlSelectorModel.addElement(ctrl);
        // controls.add(new HydraulicControl(ctrl.id(), ctrl.label()));
        // }

        // for (ExplorerItem control : controls) {
        // controlExplorer.appendItem(control);
        // }

        this.appendChild(splitPaneContainer);

    }

    public void setCurrentControl(int index) {
        // if (index <w)

    }

    public void setNumberOfControls(int n) {

        controlSelectorModel.clear();
        controls.clear();
        for (int k = 0; k < n; k++) {
            ControlItem ctrl = new ControlItem(UUID.randomUUID().toString(),
                    "Contrôle #" + (k + 1), null);
            controlSelectorModel.addElement(ctrl);
            controls.add(new HydraulicControl(ctrl.id(), ctrl.label()));
        }
    }

    private class HydraulicControl extends GridPanel {
        public final String id;
        private String label;

        public HydraulicControl(String id, String label) {
            this.id = id;
            this.label = label;
            this.insertChild(new JLabel(label), 0, 0);
        }
    }

}
