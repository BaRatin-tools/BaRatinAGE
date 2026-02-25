package org.baratinage.ui.component;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;

public class SimpleDialog {

    private final JDialog dialog;
    private final SimpleFlowPanel header;
    private final SimpleFlowPanel content;
    private final SimpleFlowPanel footer;

    public SimpleDialog(JComponent parent, boolean modal) {
        this(SwingUtilities.getWindowAncestor(parent), modal);
    }

    public SimpleDialog(Window parent, boolean modal) {
        dialog = new JDialog(parent);
        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        header = new SimpleFlowPanel();
        content = new SimpleFlowPanel();
        footer = new SimpleFlowPanel();

        SimpleFlowPanel panel = new SimpleFlowPanel(true);
        panel.setGap(5);
        panel.setPadding(5);
        panel.addChild(header, false);
        panel.addChild(content, true);
        panel.addChild(footer, false);

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
        content.removeAll();
        content.addChild(newContent, true);
    }

    public void setHeader(JComponent newHeader) {
        header.removeAll();
        header.addChild(newHeader, true);
    }

    public void setFooter(JComponent newFooter) {
        footer.removeAll();
        footer.addChild(newFooter, true);
    }

    public void openDialog() {
        dialog.pack();
        dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        dialog.setVisible(true);
    }

    public void closeDialog() {
        dialog.dispose();
    }

    public void update() {
        dialog.pack();
    }

    public static SimpleDialog buildOkCancelDialog(JComponent parent,
            String title,
            JComponent content,
            ActionListener onOk,
            ActionListener onCancel) {
        return buildOkCancelDialog(
                SwingUtilities.getWindowAncestor(parent),
                title, content, onOk, onCancel);
    }

    public static SimpleDialog buildOkCancelDialog(
            Window parent,
            String title,
            JComponent content,
            ActionListener onOk,
            ActionListener onCancel) {

        SimpleDialog dialog = new SimpleDialog(parent, true);

        dialog.setTitle(title);

        dialog.setContent(content);

        SimpleFlowPanel actionsPanel = new SimpleFlowPanel();
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
        actionsPanel.addChild(okButton, true);
        actionsPanel.addChild(cancelButton, false);

        dialog.setFooter(actionsPanel);

        dialog.dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel.actionPerformed(null);
            }
        });

        return dialog;
    }

    public static SimpleDialog buildInfoDialog(JComponent parent, String title, JComponent content) {
        return buildInfoDialog(SwingUtilities.getWindowAncestor(parent), title, content);
    }

    public static SimpleDialog buildInfoDialog(Window parent, String title, JComponent content) {

        SimpleDialog dialog = new SimpleDialog(parent, true);

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
