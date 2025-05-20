package org.baratinage.ui.baratin.rating_curve;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextArea;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
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

    public static String updateKACBEquation(List<EstimatedParameterWrapper> parameters, boolean[][] controlMatrix) {
        // here we assume KACB order for each control (indices 0, 1, 2 and 3)
        int nCtrlSeg = controlMatrix.length;
        String[] equationLines = new String[nCtrlSeg + 1];
        equationLines[0] = "h < " + parameters.get(0).parameter.getMaxpost() + ": Q = 0";
        for (int i = 0; i < nCtrlSeg; i++) { // for each segment (stage range)
            // retrieve control stage range and initialize equation line
            Double k = parameters.get(i * 4 + 0).parameter.getMaxpost();
            Double kNext = i < nCtrlSeg - 1 ? parameters.get((i + 1) * 4 + 0).parameter.getMaxpost() : null;
            String eqStr = kNext != null ? k + " < h < " + kNext : "h > " + k;
            eqStr = eqStr + ": Q = ";
            boolean first = true;
            for (int j = 0; j <= i; j++) { // for each possibly active control
                if (controlMatrix[i][j]) {
                    Double a = parameters.get(j * 4 + 1).parameter.getMaxpost();
                    Double b = parameters.get(i * 4 + 3).parameter.getMaxpost();
                    Double c = parameters.get(i * 4 + 2).parameter.getMaxpost();

                    eqStr = eqStr + (first ? a : processAdd(a));
                    eqStr = eqStr + " * (h" + processSub(b) + ") ^ " + c;
                    first = false;
                }
            }
            equationLines[i + 1] = eqStr;
        }
        // equationTextArea.setText(String.join("\n", equationLines));
        return String.join("\n", equationLines);
    }

    private static String processAdd(Double value) {
        return value < 0 ? " - " + (value * -1) : " + " + value;
    }

    private static String processSub(Double value) {
        return value < 0 ? " + " + (value * -1) : " - " + value;
    }
}
