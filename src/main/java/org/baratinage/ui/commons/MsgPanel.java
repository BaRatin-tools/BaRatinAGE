package org.baratinage.ui.commons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.baratinage.AppSetup;
import org.baratinage.ui.container.SimpleFlowPanel;

public class MsgPanel extends SimpleFlowPanel {

    public final JLabel message;
    private final List<JButton> buttons;
    private final SimpleFlowPanel actionButtonsPanel;

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
        super(!small);

        message = new JLabel();
        buttons = new ArrayList<>();
        actionButtonsPanel = new SimpleFlowPanel();
        actionButtonsPanel.setBackground(null);

        message.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        addChild(message, false);
        addChild(actionButtonsPanel, false);

        setPadding(2);
        setGap(2);

        actionButtonsPanel.setGap(2);

        setMessageType(type);
    }

    private Color getColor() {
        Color clr = AppSetup.COLORS.INFO;
        if (msgType == TYPE.WARNING) {
            clr = AppSetup.COLORS.WARNING;
        } else if (msgType == TYPE.ERROR) {
            clr = AppSetup.COLORS.ERROR;
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
            setBackground(AppSetup.COLORS.INVALID_BG);
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
            actionButtonsPanel.removeAll();
            for (JButton btn : buttons) {
                actionButtonsPanel.addChild(btn, false);
            }
            actionButtonsPanel.updateUI();
        }
        super.updateUI();
    }
}
