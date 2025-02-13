package org.baratinage.ui.component;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.RowColPanel;

public class SimpleDialog {

    private final JDialog dialog;
    private final RowColPanel header;
    private final RowColPanel content;
    private final RowColPanel footer;

    public SimpleDialog(boolean modal) {
        dialog = new JDialog(AppSetup.MAIN_FRAME, modal);
        header = new RowColPanel();
        content = new RowColPanel();
        footer = new RowColPanel();

        RowColPanel panel = new RowColPanel(RowColPanel.AXIS.COL);
        panel.setGap(5);
        panel.setPadding(5);
        panel.appendChild(header, 0);
        panel.appendChild(content, 1);
        panel.appendChild(footer, 0);

        dialog.setContentPane(panel);
    }

    public void setSize(Integer width, Integer height) {
        Dimension dim = dialog.getPreferredSize();
        if (width != null) {
            dim.width = width;
        }
        if (height != null) {
            dim.height = height;
        }
        dialog.setPreferredSize(dim);
    }

    public void setTitle(String title) {
        dialog.setTitle(title);
    }

    public void setContent(JComponent newContent) {
        content.clear();
        content.appendChild(newContent, 1);
    }

    public void setHeader(JComponent newHeader) {
        header.clear();
        header.appendChild(newHeader, 1);
    }

    public void setFooter(JComponent newFooter) {
        footer.clear();
        footer.appendChild(newFooter, 1);
    }

    public void openDialog() {
        dialog.pack();
        dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        dialog.setVisible(true);
    }

    public void closeDialog() {
        dialog.dispose();
    }

    public static SimpleDialog buildOkCancelDialog(
            String title,
            JComponent content,
            ActionListener onOk,
            ActionListener onCancel) {

        SimpleDialog dialog = new SimpleDialog(true);

        dialog.setTitle(title);

        dialog.setContent(content);

        RowColPanel actionsPanel = new RowColPanel();
        actionsPanel.setGap(5);
        JButton cancelButton = new JButton();
        cancelButton.setText(T.text("cancel"));
        cancelButton.addActionListener(l -> {
            onCancel.actionPerformed(l);
            dialog.closeDialog();
        });
        JButton okButton = new JButton();
        okButton.setText(T.text("ok"));
        okButton.addActionListener(l -> {
            onOk.actionPerformed(l);
            dialog.closeDialog();
        });
        actionsPanel.appendChild(okButton, 1);
        actionsPanel.appendChild(cancelButton, 0);

        dialog.setFooter(actionsPanel);

        dialog.dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel.actionPerformed(null);
            }
        });

        return dialog;
    }

    public static SimpleDialog buildInfoDialog(String title, JComponent content) {

        SimpleDialog dialog = new SimpleDialog(false);

        dialog.setTitle(title);

        JButton closeButton = new JButton();
        closeButton.setText(T.text("close"));
        closeButton.addActionListener(l -> {
            dialog.closeDialog();
        });

        dialog.setContent(content);
        dialog.setFooter(closeButton);

        return dialog;
    }

}
