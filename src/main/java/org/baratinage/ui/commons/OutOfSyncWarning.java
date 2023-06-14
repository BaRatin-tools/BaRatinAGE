package org.baratinage.ui.commons;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.App;
import org.baratinage.ui.container.RowColPanel;

public class OutOfSyncWarning extends RowColPanel {

    private JLabel messageLabel;
    private JButton cancelChangeBtn;

    public OutOfSyncWarning() {
        messageLabel = new JLabel();
        cancelChangeBtn = new JButton();

        appendChild(messageLabel, 1);
        appendChild(cancelChangeBtn, 0);

        setBackground(App.INVALID_COLLOR);

        setPadding(2);
        setGap(5);
    }

    public void setMessageText(String text) {
        messageLabel.setText(text);
    }

    public void setCancelButtonText(String text) {
        cancelChangeBtn.setText(text);
    }

    public void addActionListener(ActionListener l) {
        cancelChangeBtn.addActionListener(l);
    }

}
