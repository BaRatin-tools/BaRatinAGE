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
    private final RowColPanel actionButtonsPanel = new RowColPanel();

    private TYPE msgType = TYPE.INFO;

    public static enum TYPE {
        INFO, WARNING, ERROR;
    };

    public MsgPanel() {
        this(TYPE.INFO);
    }

    public MsgPanel(TYPE type) {
        super(AXIS.COL);

        appendChild(message, 1);
        appendChild(actionButtonsPanel, 1);

        setPadding(2);
        setGap(2);

        actionButtonsPanel.setGap(2);

        setMessageType(type);
    }

    private Color getColor() {
        Color clr = AppConfig.AC.INFO_COLOR;
        if (msgType == TYPE.WARNING) {
            clr = AppConfig.AC.WARNING_COLOR;
        } else if (msgType == TYPE.ERROR) {
            clr = AppConfig.AC.INVALID_COLOR_FG;
        }
        return clr;
    }

    public void setMessageType(TYPE type) {
        msgType = type;
        Color clr = getColor();
        message.setForeground(clr);
        // setBorder(BorderFactory.createLineBorder(clr, 2));
        setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, clr));
        if (msgType == TYPE.ERROR) {
            setBackground(AppConfig.AC.INVALID_COLOR_BG);
        }
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
