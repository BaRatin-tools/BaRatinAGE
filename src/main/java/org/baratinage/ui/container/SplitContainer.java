package org.baratinage.ui.container;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

public class SplitContainer extends JSplitPane {

    public SplitContainer(JComponent first, JComponent second, boolean isHorizontal) {
        this(first, second, isHorizontal, -1.0f);
    }

    public SplitContainer(JComponent first, JComponent second, boolean isHorizontal, float dividerLocation) {
        super(isHorizontal ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);
        if (isHorizontal) {
            setLeftComponent(first);
            setRightComponent(second);
        } else {
            setTopComponent(first);
            setBottomComponent(second);
        }
        setBorder(new EmptyBorder(0, 0, 0, 0));

        if (dividerLocation < 0.0f || dividerLocation > 1.0f) {
            return;
        }

        setResizeWeight(dividerLocation);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setDividerLocation(dividerLocation);
                removeComponentListener(this); // only do it once
            }
        });
    }
}
