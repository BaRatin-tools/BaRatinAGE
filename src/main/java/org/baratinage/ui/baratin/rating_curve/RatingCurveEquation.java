package org.baratinage.ui.baratin.rating_curve;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JButton;
import javax.swing.JTextArea;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.RowColPanel;

public class RatingCurveEquation extends RowColPanel {
    private final JTextArea equationTextArea;

    RatingCurveEquation() {
        super(AXIS.COL, ALIGN.START, ALIGN.STRETCH);
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
        RowColPanel actionPanel = new RowColPanel();
        actionPanel.setMainAxisAlign(ALIGN.START);
        actionPanel.appendChild(btnCopyToClipboard);
        appendChild(actionPanel, 0);
        appendChild(equationTextArea, 0);

        T.t(this, () -> {
            btnCopyToClipboard.setToolTipText(T.text("to_clipboard"));
        });
    }

    public void updateEquation(String equationString) {
        equationTextArea.setText(equationString);
    }
}
