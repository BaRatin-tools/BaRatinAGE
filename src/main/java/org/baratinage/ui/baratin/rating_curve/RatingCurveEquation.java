package org.baratinage.ui.baratin.rating_curve;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JButton;
import javax.swing.JTextArea;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;

public class RatingCurveEquation extends SimpleFlowPanel {
    private final JTextArea equationTextArea;

    RatingCurveEquation() {
        super(true);
        setGap(5);
        setPadding(5);
        equationTextArea = new JTextArea();
        equationTextArea.setEditable(false);
        JButton btnCopyToClipboard = new JButton();
        btnCopyToClipboard.setIcon(AppSetup.ICONS.COPY);
        btnCopyToClipboard.addActionListener((e) -> {
            Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            systemClipboard.setContents(new StringSelection(equationTextArea.getText()), null);
        });
        SimpleFlowPanel actionPanel = new SimpleFlowPanel();
        actionPanel.addChild(btnCopyToClipboard, false);
        addChild(actionPanel, false);
        addChild(equationTextArea, false);

        T.t(this, () -> {
            btnCopyToClipboard.setToolTipText(T.text("to_clipboard"));
        });
    }

    public void updateEquation(String equationString) {
        equationTextArea.setText(equationString);
    }
}
