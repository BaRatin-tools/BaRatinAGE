package org.baratinage.ui.baratin.rating_curve;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.EquationLabel;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.SimpleFlowPanel;

public class RatingCurveEquation extends SimpleFlowPanel {
    private final JTextArea plainEqLbl;
    private final EquationLabel niceEqLbl;
    private final SimpleFlowPanel latexPanel;

    RatingCurveEquation() {
        super(true);
        setGap(5);
        setPadding(5);

        plainEqLbl = new JTextArea();
        plainEqLbl.setEditable(false);
        JButton plainEqCopyBtn = new JButton();
        plainEqCopyBtn.setIcon(AppSetup.ICONS.COPY);
        plainEqCopyBtn.addActionListener((e) -> {
            Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            systemClipboard.setContents(new StringSelection(plainEqLbl.getText()), null);
        });
        JToolBar plainEqToolbar = new JToolBar();
        plainEqToolbar.add(plainEqCopyBtn, false);

        niceEqLbl = new EquationLabel();
        JToolBar niceEqToolbar = niceEqLbl.getExportToolbar();

        latexPanel = new SimpleFlowPanel(true);
        latexPanel.addChild(new SimpleSep(), false);
        latexPanel.addChild(niceEqToolbar, false);
        latexPanel.addChild(niceEqLbl, false);

        addChild(plainEqToolbar, false);
        addChild(plainEqLbl, false);
        addChild(latexPanel, false);

        T.t(this, () -> {
            plainEqCopyBtn.setToolTipText(T.text("to_clipboard"));
        });
    }

    public void updateEquation(String equationString) {
        plainEqLbl.setText(equationString);
    }

    public void updateEquationLatex(String equationLatex) {
        if (equationLatex != null) {
            niceEqLbl.setLatexEquation(equationLatex);
            latexPanel.setVisible(true);
            return;
        } else {
            latexPanel.setVisible(false);
        }
    }

    public String getEquationString() {
        return plainEqLbl.getText();
    }
}
