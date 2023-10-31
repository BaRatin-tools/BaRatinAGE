package org.baratinage.ui.component;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;

public class ProgressFrame extends JDialog {

    private final RowColPanel customContentPanel;
    private final JProgressBar progressBar;
    private final JLabel progressMsg;
    private final JButton cancelCloseButton;

    private final List<Runnable> onCancelActions = new ArrayList<>();
    private final List<Runnable> onDoneActions = new ArrayList<>();

    private Frame parentFrame;
    private boolean canceled = false;
    private boolean done = false;
    private boolean autoClose = false;

    public ProgressFrame() {
        super(AppConfig.AC.APP_MAIN_FRAME, false);
        RowColPanel contentPanel = new RowColPanel(RowColPanel.AXIS.COL);

        contentPanel.setGap(5);
        contentPanel.setPadding(10);

        customContentPanel = new RowColPanel();
        progressBar = new JProgressBar();
        progressMsg = new JLabel();
        cancelCloseButton = new JButton();

        cancelCloseButton.addActionListener(e -> {
            cancelOrClose();
        });

        contentPanel.appendChild(customContentPanel, 1);
        contentPanel.appendChild(progressBar, 0);
        contentPanel.appendChild(progressMsg, 0);
        contentPanel.appendChild(cancelCloseButton, 0);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });

        setContentPane(contentPanel);
    }

    public void openProgressFrame(
            Frame parentFrame,
            Container content,
            String titleString,
            int progressMin,
            int progressMax,
            boolean autoClose) {
        progressBar.setMinimum(progressMin);
        progressBar.setMaximum(progressMax);

        progressMsg.setText(" ".repeat(150));

        cancelCloseButton.setText(T.text("cancel"));

        customContentPanel.clear();
        customContentPanel.appendChild(content);

        this.autoClose = autoClose;
        canceled = false;
        done = false;

        setTitle(titleString);
        pack();
        setLocationRelativeTo(parentFrame);
        setVisible(true);

        this.parentFrame = parentFrame;
        if (parentFrame != null) {
            parentFrame.setEnabled(false);
        }
    }

    public void closeProgressFrame() {
        if (parentFrame != null) {
            parentFrame.setEnabled(true);
        }
        setVisible(false);
        dispose();
    }

    public void updateProgress(final String message, final int progress) {
        progressBar.setValue(progress);
        progressMsg.setText(message);
    }

    public void done() {
        progressBar.setValue(progressBar.getMaximum());
        done = true;
        fireOnDoneActions();
        cancelOrDone();
    }

    public void cancel() {
        canceled = true;
        fireOnCancelActions();
        cancelOrDone();
    }

    private void cancelOrDone() {
        cancelCloseButton.setText(T.text("close"));
        if (autoClose) {
            closeProgressFrame();
        }
    }

    private void cancelOrClose() {
        if (canceled || done) {
            closeProgressFrame();
        } else {
            cancel();
        }
    }

    public void addOnDoneAction(Runnable action) {
        onDoneActions.add(action);
    }

    public void removeOnDoneAction(Runnable action) {
        onDoneActions.remove(action);
    }

    public void clearOnDoneActions() {
        onDoneActions.clear();
    }

    private void fireOnDoneActions() {
        for (Runnable runnable : onDoneActions) {
            runnable.run();
        }
    }

    public void addOnCancelAction(Runnable action) {
        onCancelActions.add(action);
    }

    public void removeOnCancelAction(Runnable action) {
        onCancelActions.remove(action);
    }

    public void clearOnCancelActions() {
        onCancelActions.clear();
    }

    private void fireOnCancelActions() {
        for (Runnable runnable : onCancelActions) {
            runnable.run();
        }
    }
}
