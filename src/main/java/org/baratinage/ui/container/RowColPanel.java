package org.baratinage.ui.container;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RowColPanel extends GridPanel {

    static public enum ALIGN {
        START,
        END,
        CENTER,
        STRETCH;
    }

    static public enum AXIS {
        COL, ROW
    }

    public AXIS mainAxis;

    protected ALIGN mainAxisAlign;
    protected ALIGN crossAxisAlign;
    protected double defaultMainAxisWeight = 0;

    public RowColPanel() {
        this(AXIS.ROW, ALIGN.STRETCH, ALIGN.STRETCH);
    }

    public RowColPanel(AXIS mainAxis) {
        this(mainAxis, ALIGN.STRETCH, ALIGN.STRETCH);
    }

    public RowColPanel(AXIS mainAxis, ALIGN mainAxisAlign) {
        this(mainAxis, mainAxisAlign, ALIGN.STRETCH);
    }

    public RowColPanel(AXIS mainAxis, ALIGN mainAxisAlign, ALIGN crossAxisAlign) {
        super();
        this.mainAxis = mainAxis;
        setMainAxisAlign(mainAxisAlign);
        setCrossAxisAlign(crossAxisAlign);
    }

    public void setMainAxis(AXIS mainAxis) {

        if (this.mainAxis == mainAxis) {
            return;
        }

        this.mainAxis = mainAxis;

        // transpose content
        List<ChildComponent> newChildComponents = new ArrayList<>();
        for (ChildComponent cc : this.childComponents) {
            newChildComponents.add(cc.flipXY());
        }
        this.childComponents = newChildComponents;

        Map<Integer, Double> tmp = this.colWeights;
        this.colWeights = this.rowWeights;
        this.rowWeights = tmp;

        updateCurrentGridSize();
        updateChildrenLayout();
    }

    public void setMainAxisAlign(ALIGN align) {
        this.mainAxisAlign = align;
        if (mainAxis == AXIS.COL) {
            if (align == ALIGN.END) {
                setAnchor(ANCHOR.S);
                defaultMainAxisWeight = 0;
            } else if (align == ALIGN.START) {
                setAnchor(ANCHOR.N);
                defaultMainAxisWeight = 0;
            } else if (align == ALIGN.CENTER) {
                setAnchor(ANCHOR.CNS);
                defaultMainAxisWeight = 0;
            } else {
                defaultMainAxisWeight = 1;
            }
        } else {
            if (align == ALIGN.END) {
                setAnchor(ANCHOR.E);
                defaultMainAxisWeight = 0;
            } else if (align == ALIGN.START) {
                setAnchor(ANCHOR.W);
                defaultMainAxisWeight = 0;
            } else if (align == ALIGN.CENTER) {
                setAnchor(ANCHOR.CWE);
                defaultMainAxisWeight = 0;
            } else {
                defaultMainAxisWeight = 1;
            }

        }
        updateChildrenLayout();
    }

    public void setCrossAxisAlign(ALIGN align) {
        this.crossAxisAlign = align;
        if (mainAxis == AXIS.COL) {
            double weight = 0;
            if (align == ALIGN.START) {
                if (this.mainAxisAlign == ALIGN.END) {
                    setAnchor(ANCHOR.SW);
                } else if (this.mainAxisAlign == ALIGN.START) {
                    setAnchor(ANCHOR.NW);
                } else {
                    setAnchor(ANCHOR.W);
                }
            } else if (align == ALIGN.END) {
                if (this.mainAxisAlign == ALIGN.END) {
                    setAnchor(ANCHOR.SE);
                } else if (this.mainAxisAlign == ALIGN.START) {
                    setAnchor(ANCHOR.NE);
                } else {
                    setAnchor(ANCHOR.E);
                }
            } else if (align == ALIGN.STRETCH) {
                weight = 1;
            }
            setColWeight(0, weight);
        } else {
            this.crossAxisAlign = align;
            double weight = 0;
            if (align == ALIGN.START) {
                if (this.mainAxisAlign == ALIGN.END) {
                    setAnchor(ANCHOR.NW);
                } else if (this.mainAxisAlign == ALIGN.START) {
                    setAnchor(ANCHOR.NE);
                } else {
                    setAnchor(ANCHOR.N);
                }
            } else if (align == ALIGN.END) {
                if (this.mainAxisAlign == ALIGN.END) {
                    ;
                    setAnchor(ANCHOR.SE);
                } else if (this.mainAxisAlign == ALIGN.START) {
                    setAnchor(ANCHOR.SW);
                } else {
                    setAnchor(ANCHOR.S);
                }
            } else if (align == ALIGN.STRETCH) {
                weight = 1;
            }
            setRowWeight(0, weight);
        }
    }

    public void appendChild(Component component, double weight,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {
        updateCurrentGridSize();
        int x = lastColIndex;
        int y = lastRowIndex;
        if (mainAxis == AXIS.ROW) {
            x++;
            setColWeight(x, weight);
        } else {
            y++;
            setRowWeight(y, weight);
        }
        if (x == -1)
            x = 0;
        if (y == -1)
            y = 0;
        super.insertChild(component, x, y,
                1, 1,
                ANCHOR.C, FILL.BOTH,
                topPadding, rightPadding,
                bottomPadding, leftPadding);
    }

    public void appendChild(Component component, double weight, int vPadding, int hPadding) {
        appendChild(component, weight, vPadding, hPadding, vPadding, hPadding);
    }

    public void appendChild(Component component, double weight, int padding) {
        appendChild(component, weight, padding, padding, padding, padding);
    }

    public void appendChild(Component component, double weight) {
        appendChild(component, weight, 0, 0, 0, 0);
    }

    public void appendChild(Component component) {
        appendChild(component, defaultMainAxisWeight);
    }

    @Override
    public void insertChild(Component component, int x, int y) {
        throw new UnsupportedOperationException("Only appendChild valid! insertChild is disabled for RowColPanel!");
    }

    @Override
    public void insertChild(Component component,
            int x, int y,
            int xSpan, int ySpan,
            ANCHOR anchor, FILL fill,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {
        throw new UnsupportedOperationException("Only appendChild valid! insertChild is disabled for RowColPanel!");
    }

    @Override
    public void insertChild(Component component,
            int x, int y,
            int xSpan, int ySpan,
            ANCHOR anchor, FILL fill) {
        throw new UnsupportedOperationException("Only appendChild valid! insertChild is disabled for RowColPanel!");
    }

    @Override
    public void insertChild(Component component,
            int x, int y,
            ANCHOR anchor, FILL fill) {
        throw new UnsupportedOperationException("Only appendChild valid! insertChild is disabled for RowColPanel!");
    }

    @Override
    public void insertChild(Component component,
            int x, int y,
            int xSpan, int ySpan) {
        throw new UnsupportedOperationException("Only appendChild valid! insertChild is disabled for RowColPanel!");
    }

}
