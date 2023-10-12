package org.baratinage.ui.commons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;

public class MsgPanel extends RowColPanel {

    public final JLabel message;
    private final List<JButton> buttons;
    private final RowColPanel actionButtonsPanel;

    private TYPE msgType = TYPE.INFO;

    public static enum TYPE {
        INFO, WARNING, ERROR;
    };

    public MsgPanel() {
        this(TYPE.INFO);
    }

    public MsgPanel(TYPE type) {
        this(type, false);
    }

    public MsgPanel(TYPE type, boolean small) {
        super(small ? AXIS.ROW : AXIS.COL, ALIGN.START);

        message = new JLabel();
        buttons = new ArrayList<>();
        actionButtonsPanel = new RowColPanel();

        message.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        appendChild(message, 0);
        appendChild(actionButtonsPanel, 0);

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
        button.setHorizontalAlignment(SwingConstants.LEFT);
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
