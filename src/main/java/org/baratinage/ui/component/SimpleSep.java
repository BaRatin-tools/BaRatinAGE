package org.baratinage.ui.component;

import java.awt.Dimension;

import javax.swing.JSeparator;

public class SimpleSep extends JSeparator {

    public SimpleSep() {
        this(false);
    }

    public SimpleSep(boolean vertical) {
        super(vertical ? JSeparator.VERTICAL : JSeparator.HORIZONTAL);
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getPreferredSize();
    }

}
