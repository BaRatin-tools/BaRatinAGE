package org.baratinage.ui.baratin.ControlMatrix;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.baratinage.ui.baratin.ControlMatrix.ControlMatrixColumn.HasChangeListener;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class ControlMatrix extends RowColPanel implements HasChangeListener {

    private List<ControlMatrixColumn> controls;
    private GridPanel controlCheckBoxPanel;
    private JButton addControlButton;
    private JButton removeControlButton;
    private JCheckBox revservedOrderCheckBox;
    private boolean reversed;

    public ControlMatrix() {
        super(AXIS.COL);

        RowColPanel buttonsPanel = new RowColPanel(AXIS.ROW, ALIGN.STRETCH);

        addControlButton = new JButton("Ajouter un contrôle");
        addControlButton.addActionListener((e) -> {
            System.out.println("Add control");
            addControl();
            hasChange();
        });

        buttonsPanel.appendChild(addControlButton);

        removeControlButton = new JButton("Supprimer le dernier contrôle");
        removeControlButton.addActionListener((e) -> {
            System.out.println("Remove control");
            removeControl();
            hasChange();
        });
        buttonsPanel.appendChild(removeControlButton);

        controlCheckBoxPanel = new GridPanel();
        controlCheckBoxPanel.setGap(5);

        appendChild(buttonsPanel, 0);
        appendChild(controlCheckBoxPanel, 1);

        revservedOrderCheckBox = new JCheckBox("Inverser l'ordre des segments");
        revservedOrderCheckBox.setSelected(true);
        reversed = true;
        revservedOrderCheckBox.addItemListener((e) -> {
            reversed = revservedOrderCheckBox.isSelected();
            updateControlMatrixView();
        });
        appendChild(revservedOrderCheckBox, 0);

        controls = new ArrayList<>();
        controls.add(
                new ControlMatrixColumn(this, 1));

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
            System.err.println("Empty controlMatrix not supported");
            return;
        }
        int nCtrl = controlMatrix[0].length;
        if (nSeg != nCtrl) {
            System.err.println("Non-square controlMatrix not supported");
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
                controls.add(new ControlMatrixColumn(this, nSeg));
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

        // boolean revers = false;
        for (int k = 0; k < nCtrl; k++) {
            String labelPostfix = "";
            if (k == 0 && k == nCtrl - 1) {
                labelPostfix = "";
            } else if (k == 0) {
                labelPostfix = " (bas)";
            } else if (k == nCtrl - 1) {
                labelPostfix = " (haut)";
            }
            int index = reversed ? k + 1 : nCtrl - k;
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
                        i + 1, reversed ? j + 1 : nCtrl - j);
            }
        }
        controlCheckBoxPanel.updateUI();
        addControlButton.setText("Ajouter un contrôle  (" + "Contrôle #" + (nCtrl + 1) + ")");
        removeControlButton.setText("Supprimer le dernier contrôle (" + "Contrôle #" + nCtrl + ")");
        removeControlButton.setEnabled(nCtrl > 1);
    }

    @Override
    public void hasChange() {
        firePropertyChange("controlMatrix", null, null);
    }

}
