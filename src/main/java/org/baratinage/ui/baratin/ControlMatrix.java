package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.baratinage.ui.container.ChangingRowColPanel;
import org.baratinage.ui.container.GridPanel;

public class ControlMatrix extends ChangingRowColPanel {

    private record ControlCheckBox(int segment, int control, CheckBox checkbox) {
    }

    private class CheckBox extends JCheckBox {

        private int grayRGBvalue = 225;
        private Color uncheckedColor = new Color(grayRGBvalue, grayRGBvalue, grayRGBvalue);
        private Color checkedColor = new Color(50, 200, 50);

        public CheckBox(String text) {
            super(text);
            this.setOpaque(true);
            this.setBackground(uncheckedColor);
            this.setPreferredSize(new Dimension(50, 30));
            this.setHorizontalAlignment(CENTER);
            this.addItemListener(e -> {
                CheckBox ccb = (CheckBox) e.getItem();
                if (ccb.isSelected()) {
                    ccb.setBackground(checkedColor);
                } else {
                    ccb.setBackground(uncheckedColor);
                }
            });
        }
    }

    private int nControls;
    private GridPanel matrixContainer;
    private List<ControlCheckBox> controlCheckboxes;

    public ControlMatrix() {
        super(AXIS.COL);
        nControls = 1;
        controlCheckboxes = new ArrayList<>();
        createMatrixPanel();
        getControlMatrix();
        updateEditability();
    }

    private void createMatrixPanel() {
        controlCheckboxes.clear();
        matrixContainer = new GridPanel();
        matrixContainer.setGap(5);
        matrixContainer.setPadding(10);
        matrixContainer.setAnchor(GridPanel.ANCHOR.N);

        for (int i = 0; i < nControls; i++) {
            JLabel label = new JLabel("Contrôle #" + (i + 1));
            label.setBorder(new EmptyBorder(new Insets(0, 5, 0, 5)));
            label.setVerticalAlignment(SwingConstants.BOTTOM);
            Component c;
            if (i != 0 && i == nControls - 1) {
                GridPanel gp = new GridPanel();
                JButton deleteButton = new JButton("-");
                deleteButton.addActionListener(e -> {
                    deleteLastControl();
                });
                gp.insertChild(deleteButton, 0, 0);
                gp.insertChild(label, 0, 1);
                c = gp;
            } else {
                c = label;
            }
            matrixContainer.insertChild(c, i + 1, 0);
        }

        for (int i = 0; i < nControls; i++) {
            JLabel label = new JLabel("Segment #" + (i + 1));
            matrixContainer.insertChild(label, 0, nControls - i);
        }

        for (int i = 0; i < nControls; i++) {
            for (int j = i; j < nControls; j++) {
                // CheckBox checkBox = new CheckBox("(" + i + ", " + j + ")");
                CheckBox checkBox = new CheckBox("");

                controlCheckboxes.add(new ControlCheckBox(j, i, checkBox));
                if (i == j) {
                    checkBox.setSelected(true);
                    checkBox.setEnabled(false);
                }

                checkBox.addItemListener(e -> {
                    updateEditability();
                    notifyFollowers();
                });
                if (i == 1 && j == 2) {

                    matrixContainer.insertChild(checkBox, i + 1, nControls - j);
                } else {

                    matrixContainer.insertChild(checkBox, i + 1, nControls - j);
                }

            }
        }

        JButton addControl = new JButton("+");
        addControl.addActionListener(e -> {
            addNewControl();
        });
        matrixContainer.insertChild(addControl, nControls + 1, 0);

        JScrollPane scrollPane = new JScrollPane(matrixContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        clear();
        appendChild(scrollPane);

    }

    private void deleteLastControl() {
        boolean[][] oldControlMatrix = getControlMatrix();
        nControls--;
        createMatrixPanel();
        updateStateFromBooleanMatrix(oldControlMatrix);
        revalidate();
        updateEditability();
        notifyFollowers();
    }

    private void addNewControl() {
        boolean[][] oldControlMatrix = getControlMatrix();
        nControls++;
        createMatrixPanel();
        updateStateFromBooleanMatrix(oldControlMatrix);
        revalidate();
        updateEditability();
        notifyFollowers();
    }

    // for quick debugging purposes...
    public static void printMatrix(boolean[][] matrix) {
        for (int i = 0; i < matrix.length; i++) { // this equals to the row in our matrix.
            for (int j = 0; j < matrix[i].length; j++) { // this equals to the column in each row.
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println(); // change line on console as row comes to end in the matrix.
        }
    }

    private void updateEditability() {
        int[] editableSegmentPerControl = new int[nControls];
        for (int k = 0; k < nControls; k++) {
            editableSegmentPerControl[k] = k;
        }
        for (ControlCheckBox ccb : controlCheckboxes) {
            if (ccb.checkbox.isSelected()) {
                int c = ccb.control;
                int s = editableSegmentPerControl[c];
                if (ccb.segment >= s) {
                    editableSegmentPerControl[c] = ccb.segment + 1;
                }
            }
        }
        for (ControlCheckBox ccb : controlCheckboxes) {
            boolean nextLevelCondition = editableSegmentPerControl[ccb.control] == ccb.segment;
            boolean prevLevelCondition = editableSegmentPerControl[ccb.control] - 1 == ccb.segment;
            ccb.checkbox
                    .setEnabled(ccb.control != ccb.segment && (nextLevelCondition || prevLevelCondition));
        }
    }

    private void updateStateFromBooleanMatrix(boolean[][] matrix) {
        for (ControlCheckBox ccb : controlCheckboxes) {
            if (matrix.length > ccb.segment) {
                if (matrix[ccb.segment].length > ccb.control) {

                    ccb.checkbox.setSelected(matrix[ccb.segment][ccb.control]);
                }
            }

        }
    }

    public boolean[][] getControlMatrix() {
        boolean[][] matrix = new boolean[nControls][nControls];
        for (ControlCheckBox ccb : controlCheckboxes) {
            matrix[ccb.segment][ccb.control] = ccb.checkbox.isSelected();
        }
        return matrix;
    }

    public void setFromBooleanMatrix(boolean[][] matrix) {
        int n = matrix.length;
        for (int k = 0; k < n; k++) {
            if (matrix[k].length != n) {
                System.err.println("ERROR: All rows of control matrix are not equal!");
                return;
            }
        }
        nControls = n;
        createMatrixPanel();
        updateStateFromBooleanMatrix(matrix);
        revalidate();
        updateEditability();
        notifyFollowers();
    }

}
