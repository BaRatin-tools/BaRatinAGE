package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;

public class WarningAndActions extends RowColPanel {

    public final JLabel message = new JLabel();
    private final List<JButton> buttons = new ArrayList<>();
    private final RowColPanel actionButtonsPanel = new RowColPanel(AXIS.ROW);

    public WarningAndActions() {
        super(AXIS.COL);

        message.setForeground(AppConfig.AC.INVALID_COLOR);
        setBorder(BorderFactory.createLineBorder(AppConfig.AC.INVALID_COLOR, 2));
        appendChild(message);
        appendChild(actionButtonsPanel);

        setPadding(2);
        setGap(5);

        actionButtonsPanel.setGap(5);
    }

    public void addButton(JButton button) {
        buttons.add(button);
        updateUI();
    }

    public void removeButton(JButton button) {
        buttons.remove(button);
        updateUI();
    }

    public void clearButtons() {
        buttons.clear();
        updateUI();
    }

    @Override
    public void updateUI() {
        if (actionButtonsPanel != null && buttons != null) {
            actionButtonsPanel.clear();
            for (JButton btn : buttons) {
                actionButtonsPanel.appendChild(btn);
            }
            actionButtonsPanel.updateUI();
        }
        super.updateUI();
    }
}
