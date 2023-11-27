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

    public void updateEquation(List<EstimatedControlParameters> parameters) {
        // this.parameters = parameters;

        int nControls = parameters.size();

        String[] equationLines = new String[nControls + 1];

        double k0 = parameters.get(0).k().getMaxpost();

        equationLines[0] = "h < " + k0 + ": Q = 0";

        for (int k = 0; k < nControls; k++) {
            Double kLow = parameters.get(k).k().getMaxpost();
            Double kHigh = k < nControls - 1 ? parameters.get(k + 1).k().getMaxpost() : null;
            Double a = parameters.get(k).a().getMaxpost();
            Double b = parameters.get(k).b().getMaxpost();
            Double c = parameters.get(k).c().getMaxpost();

            String start = kHigh == null ? "h > " + kLow : kLow + " < h < " + kHigh;
            equationLines[k + 1] = start + ": Q = " + a + " * (h - " + b + ") ^ " + c + "";
        }

        equationTextArea.setText(String.join("\n", equationLines));
    }

}
