package org.baratinage.ui.baratin.rating_curve;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextArea;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.BamEstimatedParameter;
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

    public void updateKACBEquation(List<BamEstimatedParameter> parameters) {
        // here we assume KACB order for each control

        int nControls = parameters.size() / 4;

        String[] equationLines = new String[nControls + 1];

        double k0 = parameters.get(0).getMaxpost();

        equationLines[0] = "h < " + k0 + ": Q = 0";

        for (int k = 0; k < nControls; k++) {
            Double kLow = parameters.get(k * 4 + 0).getMaxpost();
            Double kHigh = k < nControls - 1 ? parameters.get((k + 1) * 4 + 0).getMaxpost() : null;
            Double a = parameters.get(k * 4 + 0).getMaxpost();
            Double b = parameters.get(k * 4 + 0).getMaxpost();
            Double c = parameters.get(k * 4 + 0).getMaxpost();

            String start = kHigh == null ? "h > " + kLow : kLow + " < h < " + kHigh;
            equationLines[k + 1] = start +
                    ": Q = " + a + " * (h - " + b + ") ^ " + c + "";
            equationLines[k + 1] = start + ": Q = " + a + " * (" +
                    processSubstraction("h", b) + ") ^ " + c + "";
        }

        equationTextArea.setText(String.join("\n", equationLines));
    }

    private static String processSubstraction(String first, Double second) {
        if (second < 0) {
            return first + " + " + (second * -1);
        } else {
            return first + " - " + second;
        }
    }

}
