package org.baratinage.ui.baratin.rating_curve;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextArea;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.EstimatedControlParameters;
import org.baratinage.ui.container.RowColPanel;

public class RatingCurveEquation extends RowColPanel {
    private final JTextArea equationTextArea;

    // private List<EstimatedControlParameters> parameters;

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

    public void updateEquation(List<EstimatedControlParameters> parameters, boolean[][] controlMatrix) {
        int nCtrlSeg = parameters.size();
        String[] equationLines = new String[nCtrlSeg + 1];
        equationLines[0] = "h < " + parameters.get(0).k().getMaxpost() + ": Q = 0";
        for (int i = 0; i < nCtrlSeg; i++) { // for each segment (stage range)
            // retrieve control stage range and initialize equation line
            Double k = parameters.get(i).k().getMaxpost();
            Double kNext = i < nCtrlSeg - 1 ? parameters.get(i + 1).k().getMaxpost() : null;
            String eqStr = kNext != null ? k + " < h < " + kNext : "h > " + k;
            eqStr = eqStr + ": Q = ";
            boolean first = true;
            for (int j = 0; j <= i; j++) { // for each possibly active control
                if (controlMatrix[i][j]) {
                    Double a = parameters.get(j).a().getMaxpost();
                    Double b = parameters.get(j).b().getMaxpost();
                    Double c = parameters.get(j).c().getMaxpost();

                    eqStr = eqStr + (first ? a : processAdd(a));
                    eqStr = eqStr + " * (h" + processSub(b) + ") ^ " + c;
                    first = false;
                }
            }
            equationLines[i + 1] = eqStr;
        }
        equationTextArea.setText(String.join("\n", equationLines));
    }

    private static String processAdd(Double value) {
        return value < 0 ? " - " + (value * -1) : " + " + value;
    }

    private static String processSub(Double value) {
        return value < 0 ? " + " + (value * -1) : " - " + value;
    }
}
