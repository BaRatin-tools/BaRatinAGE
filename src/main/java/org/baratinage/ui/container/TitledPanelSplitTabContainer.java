package org.baratinage.ui.container;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Supplier;

import javax.swing.JComponent;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

public class TitledPanelSplitTabContainer extends RowColPanel {

    public static TitledPanelSplitTabContainer build2Left1Right(
            JComponent parentComponent,
            TitledPanel topLeft,
            TitledPanel bottomLeft,
            TitledPanel right) {

        TitledPanelSplitTabContainer container = new TitledPanelSplitTabContainer(
                parentComponent,
                () -> {
                    return SplitContainer.build2Left1RightSplitContainer(
                            topLeft.getTitledPanel(),
                            bottomLeft.getTitledPanel(),
                            right.getTitledPanel());
                }, () -> {
                    TabContainer tabContainer = new TabContainer();
                    tabContainer.addTab(topLeft);
                    tabContainer.addTab(right);
                    tabContainer.addTab(bottomLeft);
                    return tabContainer;
                });

        return container;

    }

    private final String ID = Misc.getTimeStampedId();
    private final Supplier<SplitContainer> splitContainerSupplier;
    private final Supplier<TabContainer> tabContainerSupplier;
    private boolean isInTabViewMode;
    private JComponent parentComponent;
    private int widthBreakpoint;
    private int heightBreakpoint;

    private TitledPanelSplitTabContainer(
            JComponent parentComponent,
            Supplier<SplitContainer> splitContainerSupplier,
            Supplier<TabContainer> tabContainerSupplier) {

        this.splitContainerSupplier = splitContainerSupplier;
        this.tabContainerSupplier = tabContainerSupplier;

        this.parentComponent = parentComponent;
        widthBreakpoint = 0;
        heightBreakpoint = 0;

        parentComponent.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                TimedActions.throttle(
                        "TitledPanelSplitTabContainer_" + ID,
                        250,
                        TitledPanelSplitTabContainer.this::updateView);
            }
        });

        appendChild(splitContainerSupplier.get(), 1);
        isInTabViewMode = false;
    }

    public void setBreakpoints(int width, int height) {
        widthBreakpoint = width;
        heightBreakpoint = height;
    }

    private void updateView() {
        int panelWidth = parentComponent.getWidth();
        int panelHeight = parentComponent.getHeight();
        ConsoleLogger.log("parent component size is : " + panelWidth + " x " + panelHeight);
        if (panelWidth == 0 || panelHeight == 0) {
            return;
        }
        if (panelWidth < widthBreakpoint || panelHeight < heightBreakpoint) {
            if (!isInTabViewMode) {
                clear();
                appendChild(tabContainerSupplier.get(), 1);
            }
            isInTabViewMode = true;
        } else {
            if (isInTabViewMode) {
                clear();
                appendChild(splitContainerSupplier.get(), 1);
            }
            isInTabViewMode = false;
        }
    }

}
