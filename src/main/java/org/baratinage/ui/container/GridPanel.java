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

    static public enum ANCHOR {
        C(GridBagConstraints.CENTER),
        CNS(GridBagConstraints.CENTER),
        CWE(GridBagConstraints.CENTER),
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

        public ANCHOR opposite() {
            if (this == N)
                return S;
            if (this == E)
                return W;
            if (this == W)
                return E;
            if (this == S)
                return N;
            if (this == NW)
                return SE;
            if (this == NE)
                return SW;
            if (this == SW)
                return NE;
            if (this == SE)
                return NW;
            return C;
        }
    }

    static public enum FILL {
        NONE(GridBagConstraints.NONE),
        H(GridBagConstraints.HORIZONTAL),
        V(GridBagConstraints.VERTICAL),
        BOTH(GridBagConstraints.BOTH);

        private final int value;

        private FILL(int value) {
            this.value = value;
        }
    }

    protected record ChildComponent(Component component,
            int x, int y,
            int xSpan, int ySpan,
            ANCHOR anchor, FILL fill,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {
        public ChildComponent flipXY() {
            return new ChildComponent(
                    component,
                    y, x,
                    xSpan, ySpan,
                    anchor, fill,
                    topPadding,
                    rightPadding,
                    bottomPadding,
                    leftPadding);
        }
    }

    protected List<ChildComponent> childComponents;
    protected Map<Integer, Double> colWeights;
    protected Map<Integer, Double> rowWeights;

    private Insets padding;
    private int rowGap;
    private int colGap;

    protected int lastColIndex;
    protected int lastRowIndex;

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

    public void setPadding(int padding) {
        setPadding(padding, padding, padding, padding);
    }

    public void setGap(int rowGap, int colGap) {
        this.rowGap = rowGap;
        this.colGap = colGap;
        updateChildrenLayout();
    }

    public void setGap(int gap) {
        setGap(gap, gap);
    }

    public void setColWeight(int index, double weight) {
        index += 1; // to accound for extensor
        this.colWeights.put(index, weight);
        updateChildrenLayout();
    }

    public void setRowWeight(int index, double weight) {
        index += 1; // to accound for extensor
        this.rowWeights.put(index, weight);
        updateChildrenLayout();
    }

    private static final int EXTENSOR_INDEX_MIN = 0;
    private static final int EXTENSOR_INDEX_MAX = 10000;
    private static final double EXTENSOR_STRENGTH = 10000;
    private List<Component> extensors = new ArrayList<>();

    private void addExtensor(ANCHOR anchor) {
        int x = 0;
        int y = 0;
        double xWeight = 0;
        double yWeight = 0;
        if (anchor == ANCHOR.N || anchor == ANCHOR.NE || anchor == ANCHOR.NW) {
            y = EXTENSOR_INDEX_MIN;
            yWeight = EXTENSOR_STRENGTH;
        }
        if (anchor == ANCHOR.S || anchor == ANCHOR.SE || anchor == ANCHOR.SW) {
            y = EXTENSOR_INDEX_MAX;
            yWeight = EXTENSOR_STRENGTH;
        }

        if (anchor == ANCHOR.E || anchor == ANCHOR.SE || anchor == ANCHOR.NE) {
            x = EXTENSOR_INDEX_MAX;
            xWeight = EXTENSOR_STRENGTH;
        }
        if (anchor == ANCHOR.W || anchor == ANCHOR.SW || anchor == ANCHOR.NW) {
            x = EXTENSOR_INDEX_MIN;
            xWeight = EXTENSOR_STRENGTH;
        }

        Component extensor = new Component() {
        };
        extensors.add(extensor);
        innerInsertChild(extensor, x, y,
                1, 1,
                xWeight, yWeight,
                ANCHOR.C, FILL.BOTH,
                0, 0, 0, 0);
    }

    public void setAnchor(ANCHOR anchor) {
        for (Component extensor : extensors) {
            this.remove(extensor);
        }
        extensors.clear();
        if (anchor == ANCHOR.C) {
            addExtensor(ANCHOR.NE);
            addExtensor(ANCHOR.SW);
        } else if (anchor == ANCHOR.CNS) {
            addExtensor(ANCHOR.N);
            addExtensor(ANCHOR.S);
        } else if (anchor == ANCHOR.CWE) {
            addExtensor(ANCHOR.E);
            addExtensor(ANCHOR.W);
        } else {
            addExtensor(anchor.opposite());
        }

    }

    public void insertChild(Component component,
            int x, int y,
            int xSpan, int ySpan,
            ANCHOR anchor, FILL fill,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {

        x += 1;
        y += 1;

        this.childComponents.add(
                new ChildComponent(component,
                        x, y,
                        xSpan, ySpan,
                        anchor, fill,
                        topPadding, rightPadding,
                        bottomPadding, leftPadding));

        updateChildrenLayout();
    }

    public void insertChild(Component component, int x, int y) {
        insertChild(component, x, y, 1, 1,
                ANCHOR.C, FILL.BOTH,
                0, 0,
                0, 0);
    }

    public void insertChild(Component component,
            int x, int y,
            int xSpan, int ySpan,
            ANCHOR anchor, FILL fill) {

        insertChild(component, x, y,
                xSpan, ySpan,
                anchor, fill,
                0, 0, 0, 0);

    }

    public void insertChild(Component component,
            int x, int y,
            ANCHOR anchor, FILL fill) {

        insertChild(component, x, y, 1, 1, anchor, fill, 0, 0, 0, 0);
    }

    public void insertChild(Component component,
            int x, int y,
            int xSpan, int ySpan) {

        insertChild(component, x, y, xSpan, ySpan, ANCHOR.C, FILL.BOTH, 0, 0, 0, 0);
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
        gbc.weightx = xWeight;
        gbc.weighty = yWeight;
        gbc.anchor = anchor.value;
        gbc.fill = fill.value;

        int top = topPadding;
        int right = rightPadding;
        int bottom = bottomPadding;
        int left = leftPadding;

        this.updateCurrentGridSize();
        // if there's a row before, add gap to top, otherwise, add global padding.top
        if (y > 1) {
            top = top + this.rowGap / 2;
        } else {
            top = top + this.padding.top;
        }
        // if there's a row after, add gap to botom, otherwise, add global
        // padding.bottom
        if ((y + ySpan - 1) < this.lastRowIndex + 1) {
            bottom = bottom + this.rowGap / 2;
        } else {
            bottom = bottom + this.padding.bottom;
        }
        // if there's a column before, add gap to left, otherwise, add global
        // padding.left
        if (x > 1) {
            left = left + this.colGap / 2;
        } else {
            left = left + this.padding.left;
        }
        // if there's a column after, add gap to right, otherwise, add global
        // padding.right
        if ((x + xSpan - 1) < this.lastColIndex + 1) {
            right = right + this.colGap / 2;
        } else {
            right = right + this.padding.right;
        }

        gbc.insets = new Insets(top, left, bottom, right);

        super.add(component, gbc);
    }

    protected void updateCurrentGridSize() {
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
        // the minus 1 is to account for the extensors
        this.lastColIndex = lastColIndex - 1;
        this.lastRowIndex = lastRowIndex - 1;
    }

    public void clear() {
        for (ChildComponent child : this.childComponents) {
            super.remove(child.component);
        }
        this.childComponents.clear();
        revalidate();
    }

    protected void updateChildrenLayout() {
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
