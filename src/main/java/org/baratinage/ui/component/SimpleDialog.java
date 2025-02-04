package org.baratinage.ui.component;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.RowColPanel;

public class SimpleDialog extends JDialog {

    private final RowColPanel modifiableContentPanel;

    private SimpleDialog(RowColPanel modifiableContentPanel) {
        super(AppSetup.MAIN_FRAME, true);
        this.modifiableContentPanel = modifiableContentPanel;
    }

    public void setSize(Integer width, Integer height) {
        Dimension dim = getPreferredSize();
        if (width != null) {
            dim.width = width;
        }
        if (height != null) {
            dim.height = height;
        }
        setPreferredSize(dim);
    }

    public void updateContent(JPanel newContent) {
        modifiableContentPanel.clear();
        modifiableContentPanel.appendChild(newContent, 1);
    }

    public void openDialog() {
        pack();
        setLocationRelativeTo(AppSetup.MAIN_FRAME);
        setVisible(true);
    }

    private static RowColPanel buildActionPanel(JDialog dialog, String okText, ActionListener onOk,
            ActionListener onCancel) {

        RowColPanel actionsPanel = new RowColPanel();
        actionsPanel.setGap(5);
        JButton cancelButton = new JButton();
        cancelButton.setText(T.text("cancel"));
        cancelButton.addActionListener(l -> {
            onCancel.actionPerformed(l);
            dialog.dispose();
        });
        JButton okButton = new JButton();
        okButton.setText(okText);
        okButton.addActionListener(l -> {
            onOk.actionPerformed(l);
            dialog.dispose();
        });
        actionsPanel.appendChild(okButton, 1);
        actionsPanel.appendChild(cancelButton, 0);
        return actionsPanel;
    }

    public static SimpleDialog buildOkCancelDialog(String title, JComponent component, ActionListener onOk,
            ActionListener onCancel) {
        RowColPanel modifiableContentPanel = new RowColPanel();
        modifiableContentPanel.appendChild(component, 1);

        SimpleDialog dialog = new SimpleDialog(modifiableContentPanel);
        RowColPanel panel = new RowColPanel(RowColPanel.AXIS.COL);
        panel.setGap(5);
        panel.setPadding(5);

        RowColPanel actionPanel = buildActionPanel(dialog, T.text("ok"), onOk, onCancel);

        panel.appendChild(modifiableContentPanel, 1);
        panel.appendChild(actionPanel, 0);

        dialog.setContentPane(panel);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel.actionPerformed(null);
            }
        });

        dialog.setTitle(title);

        return dialog;
    }

}
