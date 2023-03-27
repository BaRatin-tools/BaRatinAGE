package org.baratinage.ui.container;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

public class GridPanel extends JPanel {

    public enum ANCHOR {
        C(GridBagConstraints.CENTER),
        N(GridBagConstraints.NORTH),
        E(GridBagConstraints.EAST),
        S(GridBagConstraints.SOUTH),
        W(GridBagConstraints.WEST),
        NW(GridBagConstraints.NORTHWEST),
        NE(GridBagConstraints.NORTHEAST),
        SW(GridBagConstraints.SOUTHWEST),
        SE(GridBagConstraints.SOUTHEAST);

        private final int value;

        private ANCHOR(int value) {
            this.value = value;
        }
    }

    public enum FILL {
        NONE(GridBagConstraints.NONE),
        H(GridBagConstraints.HORIZONTAL),
        V(GridBagConstraints.VERTICAL),
        BOTH(GridBagConstraints.BOTH);

        private final int value;

        private FILL(int value) {
            this.value = value;
        }
    }

    private record ChildComponent(Component component,
            int x, int y,
            int xSpan, int ySpan,
            ANCHOR anchor, FILL fill,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {

    }

    List<ChildComponent> childComponents;
    Map<Integer, Double> colWeights;
    Map<Integer, Double> rowWeights;

    Insets padding;
    int rowGap;
    int colGap;

    int lastColIndex;
    int lastRowIndex;

    public GridPanel() {
        super();
        this.setLayout(new GridBagLayout());
        this.colWeights = new HashMap<>();
        this.rowWeights = new HashMap<>();
        this.padding = new Insets(0, 0, 0, 0);
        this.rowGap = 0;
        this.colGap = 0;
        this.childComponents = new ArrayList<>();
        // this.setDebug(true);
    }

    public void setDebug(boolean debug) {
        if (debug) {
            this.setBackground(new Color(
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255),
                    (int) (Math.random() * 255), 150));

        } else {
            this.setBackground(null);
        }
        this.updateUI();
    }

    public void setPadding(int top, int right, int bottom, int left) {
        this.padding = new Insets(top, left, bottom, right);
        updateChildrenLayout();
    }

    public void setGap(int rowGap, int colGap) {
        this.rowGap = rowGap;
        this.colGap = colGap;
        updateChildrenLayout();
    }

    public void setColWeight(int index, double weight) {
        this.colWeights.put(index, weight);
        updateChildrenLayout();
    }

    public void setRowWeight(int index, double weight) {
        this.rowWeights.put(index, weight);
        updateChildrenLayout();
    }

    public void insertChild(Component component, int x, int y) {
        this.insertChild(component, x, y, 1, 1,
                ANCHOR.C, FILL.BOTH,
                0, 0,
                0, 0);
    }

    public void insertChild(Component component,
            int x, int y,
            int xSpan, int ySpan,
            ANCHOR anchor, FILL fill,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {

        this.childComponents.add(
                new ChildComponent(component,
                        x, y,
                        xSpan, ySpan,
                        anchor, fill,
                        topPadding, rightPadding,
                        bottomPadding, leftPadding));

        updateChildrenLayout();
    }

    private void innerInsertChild(Component component,
            int x, int y,
            int xSpan, int ySpan,
            double xWeight, double yWeight,
            ANCHOR anchor, FILL fill,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = xSpan;
        gbc.gridheight = ySpan;
        gbc.weightx = xWeight; // FIXME: deal with weights
        gbc.weighty = yWeight; // FIXME: deal with weights
        gbc.anchor = anchor.value;
        gbc.fill = fill.value;

        int top = topPadding;
        int right = rightPadding;
        int bottom = bottomPadding;
        int left = leftPadding;

        this.updateCurrentGridSize();
        // if there's a row before, add gap to top, otherwise, add global padding.top
        if (y > 0) {
            top = top + this.rowGap / 2;
        } else {
            top = top + this.padding.top;
        }
        // if there's a row after, add gap to botom, otherwise, add global
        // padding.bottom
        if (y < this.lastRowIndex) {
            bottom = bottom + this.rowGap / 2;
        } else {
            bottom = bottom + this.padding.bottom;
        }
        // if there's a column before, add gap to left, otherwise, add global
        // padding.left
        if (x > 0) {
            left = left + this.colGap / 2;
        } else {
            left = left + this.padding.left;
        }
        // if there's a column after, add gap to right, otherwise, add global
        // padding.right
        if (x < this.lastColIndex) {
            right = right + this.colGap / 2;
        } else {
            right = right + this.padding.right;
        }

        gbc.insets = new Insets(top, left, bottom, right);

        super.add(component, gbc);
    }

    private void updateCurrentGridSize() {
        int lastColIndex = 0;
        int lastRowIndex = 0;
        for (ChildComponent child : this.childComponents) {
            if (child.x > lastColIndex) {
                lastColIndex = child.x;
            }
            if (child.y > lastRowIndex) {
                lastRowIndex = child.y;
            }
        }
        this.lastColIndex = lastColIndex;
        this.lastRowIndex = lastRowIndex;
    }

    private void updateChildrenLayout() {
        for (ChildComponent child : this.childComponents) {
            super.remove(child.component);
        }
        for (ChildComponent child : this.childComponents) {
            Double xWeight = this.colWeights.get(child.x);
            if (xWeight == null) {
                xWeight = 0.0;
            }
            Double yWeight = this.rowWeights.get(child.y);
            if (yWeight == null) {
                yWeight = 0.0;
            }
            innerInsertChild(child.component,
                    child.x, child.y,
                    child.xSpan, child.ySpan,
                    xWeight,
                    yWeight,
                    child.anchor, child.fill,
                    child.topPadding, child.rightPadding, child.bottomPadding,
                    child.leftPadding);
        }
        revalidate();
    }
}
