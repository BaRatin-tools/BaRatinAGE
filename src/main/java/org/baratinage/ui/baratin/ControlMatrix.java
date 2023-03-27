package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.baratinage.ui.container.FlexPanel;
import org.baratinage.ui.container.GridPanel;

public class ControlMatrix extends FlexPanel {

    private int nControls;
    boolean[][] controlMatrixBySegment;
    GridPanel matrixContainer;

    public ControlMatrix() {

        this.nControls = 2;

        controlMatrixBySegment = new boolean[4][4];
        for (int i = 0; i < nControls; i++) {
            for (int j = i; j < nControls; j++) {
                controlMatrixBySegment[i][j] = i == j;
            }
        }

        matrixContainer = createMatrixPanel(4);
        JScrollPane scrollPane = new JScrollPane(matrixContainer);
        this.appendChild(scrollPane, 1);

    }

    private GridPanel createMatrixPanel(int nControls) {

        GridPanel matrixContainer = new GridPanel();
        matrixContainer.setGap(5, 5);
        matrixContainer.setPadding(0, 10, 0, 10);

        for (int i = 0; i < nControls; i++) {
            JLabel label = new JLabel("Contrôle #" + (i + 1));
            matrixContainer.insertChild(label, i + 1, 0,
                    1, 1,
                    GridPanel.ANCHOR.C, GridPanel.FILL.BOTH,
                    2, 5, 2, 5);
        }

        for (int i = 0; i < nControls; i++) {
            JLabel label = new JLabel("Segment #" + (i + 1));
            matrixContainer.insertChild(label, 0, nControls - i);
        }

        for (int i = 0; i < nControls; i++) {
            for (int j = i; j < nControls; j++) {
                ControlCheckBox checkBox = new ControlCheckBox("(" + i + ", " + j + ")");
                if (i == j) {
                    checkBox.setSelected(true);
                    checkBox.setEnabled(false);
                }

                checkBox.addItemListener(e -> {
                    updateMatrix();
                });
                // matrixContainer.insertChild(checkBox, i + 1, nControls - j);
                if (i == 1 && j == 2) {

                    matrixContainer.insertChild(checkBox, i + 1, nControls - j,
                            1, 1,
                            GridPanel.ANCHOR.C, GridPanel.FILL.BOTH,
                            0, 0, 0, 0);
                } else {

                    matrixContainer.insertChild(checkBox, i + 1, nControls - j,
                            1, 1,
                            GridPanel.ANCHOR.C, GridPanel.FILL.BOTH,
                            0, 0, 0, 0);
                }

            }
        }

        return matrixContainer;

    }

    // private void updateEditability() {

    // }

    private void updateMatrix() {

    }

    private class ControlCheckBox extends JCheckBox {

        // public ControlCheckBox() {
        // this("");
        // }

        public ControlCheckBox(String text) {
            super(text);
            this.setOpaque(true);
            this.setBackground(Color.GRAY);
            this.setPreferredSize(new Dimension(50, 30));
            this.setHorizontalAlignment(CENTER);
            this.addItemListener(e -> {
                ControlCheckBox ccb = (ControlCheckBox) e.getItem();
                if (ccb.isSelected()) {
                    ccb.setBackground(Color.GREEN);
                } else {
                    ccb.setBackground(Color.GRAY);
                }
            });
        }
    }

    // private class RotatedLabel extends JPanel {
    // JLabel label;
    // public RotatedLabel(String text) {
    // this.label = new JLabel(text);
    // }

    // @Override
    // protected void paintComponent(Graphics g) {

    // super.paintComponent(g2);
    // Graphics2D g2 = (Graphics2D) g;
    // // g2.rotate(Math.toRadians(-60), 0, 75);
    // }

    // // @Override
    // // public Dimension getPreferredSize() {
    // // return new Dimension(100, 100);
    // // }
    // }

}
