package org.baratinage.ui.commons;

import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.App;
import org.baratinage.ui.container.RowColPanel;

public class WarningAndActions extends RowColPanel {

    private JLabel warningMessage = new JLabel();
    private HashMap<String, JButton> actionButtons = new HashMap<>();
    private RowColPanel actionButtonsPanel = new RowColPanel(AXIS.ROW);

    public WarningAndActions() {
        super(AXIS.COL);

        setBackground(App.INVALID_COLLOR);
        appendChild(warningMessage);
        appendChild(actionButtonsPanel);

        setPadding(2);
        setGap(5);

        actionButtonsPanel.setBackground(App.INVALID_COLLOR);
        actionButtonsPanel.setGap(5);
    }

    public void setWarningMessage(String text) {
        warningMessage.setText(text);
    }

    public void addActionButton(
            String id,
            String label,
            boolean enable,
            ActionListener action) {
        JButton btn = new JButton();
        btn.setText(label);
        btn.addActionListener(action);
        actionButtons.put(id, btn);
        updateActionButtonsPanel();

    }

    public void removeActionButton(String id) {
        actionButtons.remove(id);
        updateActionButtonsPanel();
    }

    private void updateActionButtonsPanel() {
        actionButtonsPanel.clear();
        for (String id : actionButtons.keySet()) {
            JButton btn = actionButtons.get(id);
            actionButtonsPanel.appendChild(btn);
        }
        actionButtonsPanel.updateUI();
        updateUI();
    }
}
