package ui.container;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class FlexPanel extends JPanel {

        public enum ALIGN {
                START(0),
                END(1),
                CENTER(2),
                EXPAND(3);

                private final int value;

                private ALIGN(int value) {
                        this.value = value;
                }
        }

        public enum AXIS {
                COL(0), ROW(1);

                private final int value;

                private AXIS(int value) {
                        this.value = value;
                }
        }

        private final static int NW = GridBagConstraints.NORTHWEST;
        private final static int N = GridBagConstraints.NORTH;
        private final static int NE = GridBagConstraints.NORTHEAST;
        private final static int W = GridBagConstraints.WEST;
        private final static int C = GridBagConstraints.CENTER;
        private final static int E = GridBagConstraints.EAST;
        private final static int SW = GridBagConstraints.SOUTHWEST;
        private final static int S = GridBagConstraints.SOUTH;
        private final static int SE = GridBagConstraints.SOUTHEAST;
        private final static int NONE = GridBagConstraints.NONE;
        private final static int V = GridBagConstraints.VERTICAL;
        private final static int H = GridBagConstraints.HORIZONTAL;
        private final static int B = GridBagConstraints.BOTH;

        private final static int[][][][] LOOK_UP_TABLE = new int[][][][] {
                        // column wise
                        {
                                        // axis start
                                        {
                                                        { NW, NONE }, // cross axis start
                                                        { NE, NONE }, // cross axis end
                                                        { N, NONE }, // cross axis center
                                                        { N, H }, // cross axis expand
                                        },
                                        // axis end
                                        {
                                                        { SW, NONE }, // cross axis start
                                                        { SE, NONE }, // cross axis end
                                                        { S, NONE }, // cross axis center
                                                        { S, H }, // cross axis expand
                                        },
                                        // axis center
                                        {
                                                        { W, NONE }, // cross axis start
                                                        { E, NONE }, // cross axis end
                                                        { C, NONE }, // cross axis center
                                                        { C, H }, // cross axis expand
                                        },
                                        // axis expand
                                        {
                                                        { W, V }, // cross axis start
                                                        { E, V }, // cross axis end
                                                        { C, V }, // cross axis center
                                                        { C, B }, // cross axis expand
                                        },
                        },
                        // row wise
                        {
                                        // axis start
                                        {
                                                        { NW, NONE }, // cross axis start
                                                        { SW, NONE }, // cross axis end
                                                        { W, NONE }, // cross axis center
                                                        { W, V }, // cross axis expand
                                        },
                                        // axis end
                                        {
                                                        { NE, NONE }, // cross axis start
                                                        { SE, NONE }, // cross axis end
                                                        { E, NONE }, // cross axis center
                                                        { E, V }, // cross axis expand
                                        },
                                        // axis center
                                        {
                                                        { N, NONE }, // cross axis start
                                                        { S, NONE }, // cross axis end
                                                        { C, NONE }, // cross axis center
                                                        { C, V }, // cross axis expand
                                        },
                                        // axis expand
                                        {
                                                        { N, H }, // cross axis start
                                                        { S, H }, // cross axis end
                                                        { C, H }, // cross axis center
                                                        { C, B }, // cross axis expand
                                        },
                        }
        };

        private record Item(
                        Component component,
                        GridBagConstraints constraints,
                        double mainAxisWeight,
                        ALIGN crossAxisAlign,
                        Insets insets) {
        };

        private GridBagLayout layout;
        // private ALIGN align;
        private AXIS axis;
        private int gap;
        private Insets padding;
        private List<Item> items;

        // private boolean debug = false;
        // public void setDebug(boolean debug) {
        // this.debug = debug;
        // if (debug) {
        // this.setBackground(new Color(
        // (int) (Math.random() * 255),
        // (int) (Math.random() * 255),
        // (int) (Math.random() * 255), 150));
        // }
        // }

        public FlexPanel(AXIS axis, ALIGN align) {
                this.layout = new GridBagLayout();
                this.items = new ArrayList<>();
                this.setLayout(this.layout);
                this.gap = 0;
                this.padding = new Insets(0, 0, 0, 0);
                this.axis = axis;
                // this.align = align;
                if (align == ALIGN.START) {
                        addEndExtensor();
                } else if (align == ALIGN.END) {
                        addStartExtensor();
                }
        }

        public FlexPanel(AXIS axis) {
                this(axis, ALIGN.EXPAND);
        }

        public FlexPanel(ALIGN align) {
                this(AXIS.COL, align);
        }

        public FlexPanel() {
                this(AXIS.COL, ALIGN.EXPAND);
        }

        public void setGap(int gap) {
                this.gap = gap;
        }

        public void setPadding(int top, int right, int bottom, int left) {
                this.padding = new Insets(top, left, bottom, right);
        }

        public void setPadding(int vPadding, int hPadding) {
                this.padding = new Insets(vPadding, hPadding, vPadding, hPadding);
        }

        public void setPadding(int padding) {
                this.padding = new Insets(padding, padding, padding, padding);
        }

        public void appendChild(Component component) {
                this.appendChild(component, 0F, ALIGN.EXPAND,
                                0, 0, 0, 0);
        }

        public void appendChild(Component component, int vPadding, int hPadding) {
                this.appendChild(component, 0F, ALIGN.EXPAND,
                                vPadding, hPadding, vPadding, hPadding);
        }

        public void appendChild(Component component,
                        int top, int right, int bottom, int left) {
                this.appendChild(component, 0F, ALIGN.EXPAND,
                                top, right, bottom, left);
        }

        public void appendChild(Component component, ALIGN crossAxisAlign) {
                this.appendChild(component, 0, crossAxisAlign,
                                0, 0, 0, 0);
        }

        public void appendChild(Component component, double mainAxisWeight) {
                this.appendChild(component, mainAxisWeight, ALIGN.EXPAND,
                                0, 0, 0, 0);
        }

        public void appendChild(Component component, double mainAxisWeight, ALIGN crossAxisAlign) {
                this.appendChild(component, mainAxisWeight, crossAxisAlign,
                                0, 0, 0, 0);
        }

        public void appendChild(Component component, double mainAxisWeight, ALIGN crossAxisAlign,
                        int top, int right, int bottom, int left) {
                GridBagConstraints gbc = new GridBagConstraints();
                // int index = this.getComponentCount();
                int index = this.items.size() + 1; // always an offset of one in case there's an extensor
                if (this.axis.value == 0) {

                        gbc.gridx = 0;
                        gbc.gridy = index;
                        gbc.weightx = 1;
                        gbc.weighty = mainAxisWeight;

                        // if (index != 0) {
                        // top = top + this.gap;
                        // }
                } else {
                        gbc.gridx = index;
                        gbc.gridy = 0;
                        gbc.weightx = mainAxisWeight;
                        gbc.weighty = 1;

                        // if (index != 0) {
                        // left = left + this.gap;
                        // }
                }

                Insets insets = new Insets(
                                top,
                                left,
                                bottom,
                                right);

                if (this.axis == AXIS.COL) {
                        left = left + this.padding.left;
                        right = right + this.padding.right;
                } else {
                        top = top + this.padding.top;
                        bottom = bottom + this.padding.bottom;
                }

                if (index == 1) { // first element to be appended
                        if (this.axis == AXIS.COL) {
                                top = top + this.padding.top;
                                bottom = bottom + this.padding.bottom;
                        } else {
                                left = left + this.padding.left;
                                right = right + this.padding.right;
                        }

                        this.layout.setConstraints(component, this.layout.getConstraints(component));
                } else if (index > 1) { // next elements to be appended
                        for (int k = 1; k < index; k++) {

                                Item item = this.items.get(k - 1);
                                GridBagConstraints constraints = item.constraints();

                                if (this.axis == AXIS.COL) {
                                        constraints.insets.bottom = item.insets().bottom + this.gap;
                                } else {
                                        constraints.insets.right = item.insets().right + this.gap;
                                }

                                this.layout.setConstraints(item.component(), item.constraints());
                        }
                        if (this.axis == AXIS.COL) {
                                bottom = bottom + this.padding.bottom;
                        } else {
                                right = right + this.padding.right;
                        }
                }

                // gbc.insets = new Insets(
                // top + this.padding.top,
                // left + this.padding.left,
                // bottom + this.padding.bottom,
                // right + this.padding.right);

                gbc.insets = new Insets(
                                top,
                                left,
                                bottom,
                                right);

                // if (this.debug) {
                // System.out.println("--------------------------");
                // System.out.println("main-axis=" + this.axis.value);
                // System.out.println("main-align=" + this.align.value);
                // System.out.println("cross-align=" + crossAxisAlign.value);
                // }

                int[] anchor_fill = LOOK_UP_TABLE[this.axis.value][ALIGN.EXPAND.value][crossAxisAlign.value];
                gbc.anchor = anchor_fill[0];
                gbc.fill = anchor_fill[1];

                items.add(new Item(component, gbc, mainAxisWeight, crossAxisAlign, insets));
                super.add(component, gbc);
        }

        private void addEndExtensor() {
                super.add(new JPanel(),
                                new GridBagConstraints(
                                                this.axis.value == 0 ? 0 : 10000,
                                                this.axis.value == 0 ? 10000 : 0,
                                                1,
                                                1,
                                                1,
                                                1,
                                                C,
                                                B,
                                                new Insets(0, 0, 0, 0),
                                                0,
                                                0));
        }

        private void addStartExtensor() {
                super.add(new JPanel(),
                                new GridBagConstraints(
                                                0,
                                                0,
                                                1,
                                                1,
                                                1,
                                                1,
                                                C,
                                                B,
                                                new Insets(0, 0, 0, 0),
                                                0,
                                                0));
        }

        @Override
        public Component add(Component component) {
                System.err.println("Please use appendChild method instead!");
                this.appendChild(component);
                return component;
        }

        @Override
        public void add(Component component, Object constraints) {
                System.err.println("Please use appendChild method instead!");
                this.appendChild(component);
        }
}
