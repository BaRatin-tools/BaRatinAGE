package org.baratinage.ui.baratin.hydraulic_control;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.translation.T;
import org.json.JSONObject;

public class ControlMatrix extends SimpleFlowPanel implements ChangeListener {

    private final List<ControlMatrixColumn> controls;
    private final GridPanel controlCheckBoxPanel;
    private final JButton addControlButton;
    private final JButton removeControlButton;
    private final JCheckBox reversedOrderCheckBox;

    public ControlMatrix() {
        super(true);

        Misc.setMinimumSize(this, null, 250);

        SimpleFlowPanel buttonsPanel = new SimpleFlowPanel(true);
        buttonsPanel.setPadding(5);
        buttonsPanel.setGap(5);

        addControlButton = new JButton(" > Ajouter un contrôle");
        addControlButton.addActionListener((e) -> {
            addControl();
        });

        buttonsPanel.addChild(addControlButton);

        removeControlButton = new JButton(" > Supprimer le dernier contrôle");
        removeControlButton.addActionListener((e) -> {
            removeControl();
        });
        buttonsPanel.addChild(removeControlButton);

        controlCheckBoxPanel = new GridPanel();
        controlCheckBoxPanel.setGap(5);

        addChild(buttonsPanel, false);
        JScrollPane controlGridScrollPane = new JScrollPane(controlCheckBoxPanel);
        controlGridScrollPane.setBorder(BorderFactory.createEmptyBorder());
        addChild(controlGridScrollPane, true);

        reversedOrderCheckBox = new JCheckBox(" > Inverser l'ordre des segments");
        // T.t(reversedOrderCheckBox, false, "invert_control_matrix");
        reversedOrderCheckBox.setSelected(true);
        reversedOrderCheckBox.addItemListener((e) -> {
            updateControlMatrixView();
        });
        addChild(reversedOrderCheckBox, false);

        controls = new ArrayList<>();
        ControlMatrixColumn cmc = new ControlMatrixColumn(1);
        cmc.addChangeListener(this);
        controls.add(cmc);

        updateControlMatrixView();

        T.t(this, reversedOrderCheckBox, false, "invert_control_matrix");
        T.t(this, () -> {
            updateLabelsAndButtons();
            updateControlMatrixView();
        });
    }

    private void updateLabelsAndButtons() {
        int nCtrl = controls.size();
        String nextCtrlText = T.text("control_nbr", nCtrl + 1);
        String currCtrlText = T.text("control_nbr", nCtrl);
        String addCtrlText = T.html("add_control", nextCtrlText);
        String delCtrlText = T.html("delete_last_control", currCtrlText);
        addControlButton.setText(addCtrlText);
        removeControlButton.setText(delCtrlText);
    }

    public boolean getIsReversed() {
        return reversedOrderCheckBox.isSelected();
    }

    public void setIsReversed(boolean isReversed) {
        reversedOrderCheckBox.setSelected(isReversed);
        updateControlMatrixView();
    }

    public boolean[][] getControlMatrix() {
        int nCtrl = controls.size();
        boolean[][] controlMatrix = new boolean[nCtrl][nCtrl];
        for (int i = 0; i < nCtrl; i++) {
            for (int j = 0; j < nCtrl; j++) {
                if (j >= i) {
                    controlMatrix[j][i] = controls.get(i).ctrlCheckBoxes.get(j - i).isSelected();
                } else {
                    controlMatrix[j][i] = false;
                }
            }
        }
        return controlMatrix;
    }

    public int getNumberOfControls() {
        return controls.size();
    }

    public static String toXtra(boolean[][] controlMatrix) {
        String xtra = "";
        for (int i = 0; i < controlMatrix.length; i++) {
            for (int j = 0; j < controlMatrix[i].length; j++) {
                xtra += controlMatrix[i][j] ? "1 " : "0 ";
            }
            if ((i + 1) != controlMatrix.length) {
                xtra += "\n";
            }
        }
        return xtra;
    }

    public static boolean[][] fromXtra(String xTra) {
        return fromXtra(xTra, false);
    }

    public static boolean[][] fromXtra(String xTra, boolean ignoreLastRow) {
        String[] rows = xTra.split("\n");
        int nCtrl = rows.length - (ignoreLastRow ? 1 : 0);
        boolean[][] controlMatrix = new boolean[nCtrl][nCtrl];
        for (int i = 0; i < nCtrl; i++) {
            String[] items = rows[i].split(" ");
            if (nCtrl != items.length) {
                ConsoleLogger.error("Should not happend! Malformed xTra data!");
                return controlMatrix;
            }
            for (int j = 0; j < nCtrl; j++) {
                if (items[j].equals("0")) {
                    controlMatrix[i][j] = false;
                } else if (items[j].equals("1")) {
                    controlMatrix[i][j] = true;
                } else {
                    ConsoleLogger.error("Should not happend! Malformed xTra data!");
                    return controlMatrix;
                }
            }
        }
        return controlMatrix;
    }

    /**
     * 
     * @param controlMatrix each row is a stage range segment and
     *                      each column is a hydraulic control
     */
    public void setControlMatrix(boolean[][] controlMatrix) {
        int nSeg = controlMatrix.length;
        if (nSeg <= 0) {
            ConsoleLogger.error("Empty controlMatrix not supported");
            return;
        }
        int nCtrl = controlMatrix[0].length;
        if (nSeg != nCtrl) {
            ConsoleLogger.error("Non-square controlMatrix not supported");
            return;
        }
        int nCtrlOld = controls.size();
        int nDiff = nCtrlOld - nCtrl;
        if (nDiff > 0) { // too many controls
            for (int k = 1; k <= nDiff; k++) {
                ControlMatrixColumn c = controls.get(nCtrlOld - k);
                c.destroyControl();
                controls.remove(nCtrlOld - k);
            }
        } else if (nDiff < 0) { // not enough controls
            for (int k = 1; k <= -nDiff; k++) {
                ControlMatrixColumn cmc = new ControlMatrixColumn(nSeg);
                cmc.addChangeListener(this);
                controls.add(cmc);
            }
        }

        for (int k = 0; k < nCtrl; k++) {
            int n = nCtrl - k;
            boolean[] selected = new boolean[n];
            for (int i = 0; i < n; i++) {
                selected[i] = controlMatrix[i + k][k];
            }
            controls.get(k).update(selected);
        }
        updateControlMatrixView();
        fireChangeListeners();
    }

    private void addControl() {
        int nCtrl = controls.size() + 1;
        boolean[][] oldMatrixControl = getControlMatrix();
        boolean[][] newMatrixControl = new boolean[nCtrl][nCtrl];
        for (int i = 0; i < nCtrl; i++) {
            for (int j = 0; j < nCtrl; j++) {
                newMatrixControl[i][j] = i == j;
            }
        }
        for (int i = 0; i < nCtrl - 1; i++) {
            for (int j = 0; j < nCtrl - 1; j++) {
                newMatrixControl[i][j] = oldMatrixControl[i][j];
            }
        }
        setControlMatrix(newMatrixControl);
    }

    private void removeControl() {
        int nCtrl = controls.size() - 1;
        if (nCtrl == 0) {
            return;
        }
        String areYouSure = T.text("are_you_sure");
        String ctrlConfigLoss = T.text("control_configuration_will_be_lost", nCtrl + 1);
        if (!CommonDialog.confirmDialog(
                String.format("<html>%s<br>%s</html>", areYouSure, ctrlConfigLoss),
                areYouSure)) {
            return;
        }
        boolean[][] oldMatrixControl = getControlMatrix();
        boolean[][] newMatrixControl = new boolean[nCtrl][nCtrl];
        for (int i = 0; i < nCtrl; i++) {
            for (int j = 0; j < nCtrl; j++) {
                newMatrixControl[i][j] = oldMatrixControl[i][j];
            }
        }
        setControlMatrix(newMatrixControl);
    }

    static private class CtrlMatPos {
        public final int index;
        public final boolean isStart;
        public final boolean isEnd;

        public CtrlMatPos(int index, int nCtrl) {
            this.index = index;
            isStart = nCtrl > 1 && index == 1;
            isEnd = nCtrl > 1 && index == nCtrl;
        }

        public String getSegmentLabel() {
            String postFix = "";
            if (isStart) {
                postFix = String.format(" (%s)", T.text("bottom"));
            }
            if (isEnd) {
                postFix = String.format(" (%s)", T.text("top"));
            }
            return String.format("<html>%s%s</html>", T.text("segment_nbr", index), postFix);
        }

        public String getControlLabel() {
            return T.html("control_nbr", index);
        }
    }

    private void updateControlMatrixView() {
        controlCheckBoxPanel.clear();
        int nCtrl = controls.size();

        ArrayList<JLabel> segLabel = new ArrayList<>();
        ArrayList<JLabel> ctrlLabel = new ArrayList<>();

        for (int k = 0; k < nCtrl; k++) {
            int index = reversedOrderCheckBox.isSelected() ? k + 1 : nCtrl - k;
            JLabel segLbl = new JLabel("seg " + index);
            controlCheckBoxPanel.insertChild(segLbl, 0, index);
            segLabel.add(segLbl);

            JLabel ctrlLbl = new JLabel("ctrl " + (k + 1));
            controlCheckBoxPanel.insertChild(ctrlLbl, k + 1, 0, GridPanel.ANCHOR.C, GridPanel.FILL.NONE);
            ctrlLabel.add(ctrlLbl);
        }

        for (int k = 0; k < nCtrl; k++) {
            CtrlMatPos ctrlMatPos = new CtrlMatPos(k + 1, nCtrl);
            segLabel.get(k).setText(ctrlMatPos.getSegmentLabel());
            ctrlLabel.get(k).setText(ctrlMatPos.getControlLabel());
        }

        for (int i = 0; i < nCtrl; i++) {
            for (int j = i; j < nCtrl; j++) {
                controlCheckBoxPanel.insertChild(
                        controls.get(i).ctrlCheckBoxes.get(j - i),
                        i + 1, reversedOrderCheckBox.isSelected() ? j + 1 : nCtrl - j);
            }
        }
        controlCheckBoxPanel.updateUI();
        removeControlButton.setEnabled(nCtrl > 1);
        updateLabelsAndButtons();
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        fireChangeListeners();
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    public static String toXtraJsonString(boolean[][] matrix) {
        String stringMatrix = "";
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                stringMatrix += matrix[i][j] ? "0" : "1";
            }
            stringMatrix += ";";
        }
        return stringMatrix;
    }

    public static boolean[][] fromXtraJsonString(String xTraJsonString) {
        String[] stringMatrixRow = xTraJsonString.split(";");
        int n = stringMatrixRow.length;
        boolean[][] matrix = new boolean[n][n];
        char one = "1".charAt(0);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = stringMatrixRow[i].charAt(j) != one;
            }
        }
        return matrix;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        boolean[][] matrix = getControlMatrix();
        String stringMatrix = toXtraJsonString(matrix);
        json.put("controlMatrixString", stringMatrix);
        json.put("isReversed", getIsReversed());
        return json;
    }

    public void fromJSON(JSONObject json) {
        String stringMatrix = json.getString("controlMatrixString");
        boolean[][] matrix = fromXtraJsonString(stringMatrix);
        setControlMatrix(matrix);
        setIsReversed(json.getBoolean("isReversed"));
    }
}
