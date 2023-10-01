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

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

public class ControlMatrix extends RowColPanel implements ChangeListener {

    private List<ControlMatrixColumn> controls;
    private GridPanel controlCheckBoxPanel;
    private JButton addControlButton;
    private JButton removeControlButton;
    private JCheckBox reversedOrderCheckBox;

    public ControlMatrix() {
        super(AXIS.COL);

        RowColPanel buttonsPanel = new RowColPanel(AXIS.COL, ALIGN.STRETCH);

        addControlButton = new JButton(" > Ajouter un contrôle");
        addControlButton.addActionListener((e) -> {
            addControl();
        });

        buttonsPanel.appendChild(addControlButton);

        removeControlButton = new JButton(" > Supprimer le dernier contrôle");
        removeControlButton.addActionListener((e) -> {
            removeControl();
        });
        buttonsPanel.appendChild(removeControlButton);

        controlCheckBoxPanel = new GridPanel();
        controlCheckBoxPanel.setGap(5);

        appendChild(buttonsPanel, 0);
        JScrollPane controlGridScrollPane = new JScrollPane(controlCheckBoxPanel);
        controlGridScrollPane.setBorder(BorderFactory.createEmptyBorder());
        appendChild(controlGridScrollPane, 1);

        reversedOrderCheckBox = new JCheckBox(" > Inverser l'ordre des segments");
        Lg.register(reversedOrderCheckBox, "invert_control_matrix");
        reversedOrderCheckBox.setSelected(true);
        reversedOrderCheckBox.addItemListener((e) -> {
            updateControlMatrixView();
        });
        appendChild(reversedOrderCheckBox, 0);

        controls = new ArrayList<>();
        ControlMatrixColumn cmc = new ControlMatrixColumn(1);
        cmc.addChangeListener(this);
        controls.add(cmc);

        updateControlMatrixView();
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

    /**
     * 
     * @param controlMatrix each row is a stage range segment and
     *                      each column is a hydraulic control
     */
    public void setControlMatrix(boolean[][] controlMatrix) {
        int nSeg = controlMatrix.length;
        if (nSeg <= 0) {
            System.err.println("ControlMatrix Error: Empty controlMatrix not supported");
            return;
        }
        int nCtrl = controlMatrix[0].length;
        if (nSeg != nCtrl) {
            System.err.println("ControlMatrix Error: Non-square controlMatrix not supported");
            return;
        }
        int nCtrlOld = controls.size();
        int nDiff = nCtrlOld - nCtrl;
        if (nDiff > 0) { // too many controls
            for (int k = 1; k <= nDiff; k++) {
                ControlMatrixColumn c = controls.get(nCtrlOld - k);
                c.destroyControl();
                controls.remove(k);
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
        boolean[][] oldMatrixControl = getControlMatrix();
        boolean[][] newMatrixControl = new boolean[nCtrl][nCtrl];
        for (int i = 0; i < nCtrl; i++) {
            for (int j = 0; j < nCtrl; j++) {
                newMatrixControl[i][j] = oldMatrixControl[i][j];
            }
        }
        setControlMatrix(newMatrixControl);
    }

    private void updateControlMatrixView() {
        controlCheckBoxPanel.clear();
        int nCtrl = controls.size();

        for (int k = 0; k < nCtrl; k++) {
            String labelPostfix = "";
            if (k == 0 && k == nCtrl - 1) {
                labelPostfix = "";
            } else if (k == 0) {
                labelPostfix = " (bas)";
            } else if (k == nCtrl - 1) {
                labelPostfix = " (haut)";
            }
            int index = reversedOrderCheckBox.isSelected() ? k + 1 : nCtrl - k;
            JLabel lbl = new JLabel("Segment #" + (k + 1) + labelPostfix);
            controlCheckBoxPanel.insertChild(lbl, 0, index);
        }
        for (int k = 0; k < nCtrl; k++) {
            JLabel lbl = new JLabel("Contrôle #" + (k + 1));
            controlCheckBoxPanel.insertChild(lbl, k + 1, 0, GridPanel.ANCHOR.C, GridPanel.FILL.NONE);
        }

        for (int i = 0; i < nCtrl; i++) {
            for (int j = i; j < nCtrl; j++) {
                controlCheckBoxPanel.insertChild(
                        controls.get(i).ctrlCheckBoxes.get(j - i),
                        i + 1, reversedOrderCheckBox.isSelected() ? j + 1 : nCtrl - j);
            }
        }
        controlCheckBoxPanel.updateUI();
        addControlButton.setText(" > Ajouter un contrôle  (" + "Contrôle #" + (nCtrl + 1) + ")");
        removeControlButton.setText(" > Supprimer le dernier contrôle (" + "Contrôle #" + nCtrl + ")");

        Lg.register(this, () -> {
            String nextCtrlText = Lg.text("control_number", nCtrl + 1);
            String currCtrlText = Lg.text("control_number", nCtrl);
            String addCtrlText = Lg.html("add_control", nextCtrlText);
            String delCtrlText = Lg.html("delete_last_control", currCtrlText);
            addControlButton.setText(addCtrlText);
            removeControlButton.setText(delCtrlText);
        });
        removeControlButton.setEnabled(nCtrl > 1);
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
}
