package org.baratinage.ui.commons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;

public class MsgPanel extends RowColPanel {

    public final JLabel message = new JLabel();
    private final List<JButton> buttons = new ArrayList<>();
    private final RowColPanel actionButtonsPanel = new RowColPanel(AXIS.ROW);

    private TYPE msgType = TYPE.INFO;

    public static enum TYPE {
        INFO, WARNING, ERROR;
    };

    public MsgPanel() {
        this(TYPE.INFO);
    }

    public MsgPanel(TYPE type) {
        super(AXIS.COL);

        appendChild(message);
        appendChild(actionButtonsPanel);

        setPadding(2);
        setGap(5);

        actionButtonsPanel.setGap(5);

        setMessageType(type);
    }

    public void setMessageType(TYPE type) {
        msgType = type;
        Color clr = AppConfig.AC.INFO_COLOR;
        if (msgType == TYPE.WARNING) {
            clr = AppConfig.AC.WARNING_COLOR;
        } else if (msgType == TYPE.ERROR) {
            clr = AppConfig.AC.INVALID_COLOR;
        }
        message.setForeground(clr);
        setBorder(BorderFactory.createLineBorder(clr, 2));
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
