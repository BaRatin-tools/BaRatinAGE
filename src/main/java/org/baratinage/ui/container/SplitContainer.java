package org.baratinage.ui.container;

import java.awt.Component;

import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

public class SplitContainer extends JSplitPane {

    public static SplitContainer build2Left1RightSplitContainer(
            Component topLeftComponent,
            Component bottomLeftComponent,
            Component rightComponent) {
        return new SplitContainer(
                new SplitContainer(topLeftComponent, bottomLeftComponent, false),
                rightComponent, true);
    }

    public static SplitContainer build1Left2RightSplitContainer(
            Component leftComponent,
            Component topRightComponent,
            Component bottomRightComponent) {
        return new SplitContainer(
                leftComponent,
                new SplitContainer(topRightComponent, bottomRightComponent, false),
                true);
    }

    public SplitContainer(Component first, Component second, boolean horizontalSplit) {
        super(horizontalSplit ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);
        if (horizontalSplit) {
            setLeftComponent(first);
            setRightComponent(second);
        } else {
            setTopComponent(first);
            setBottomComponent(second);
        }
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }
}
