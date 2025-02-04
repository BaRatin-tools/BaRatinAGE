package org.baratinage.ui.baratin.rc_compare;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;

import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.EditablePlotItemSet;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.component.SimpleList;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.Legend;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotUtils;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;

public class RatingCurveCompare extends BamItem {

    private final BamItemParent rcOne;
    private final BamItemParent rcTwo;
    private final PlotContainer plotContainer;

    private final SimpleList<EditablePlotItem> plotItemsList;
    private final EditablePlotItemSet editablePlotItemsSet;

    private Plot plot;

    public RatingCurveCompare(String uuid, BaratinProject project) {
        super(BamItemType.COMPARING_RATING_CURVES, uuid, project);

        /**
         * this item should be fed groups of plotItem
         * 
         * SimpleList
         * SimpleListItem (uuid, jlabel, object)
         * EditablePlotItem
         * 
         */

        // rating curves chooser
        rcOne = new BamItemParent(this, BamItemType.RATING_CURVE, BamItemType.HYDRAULIC_CONFIG);
        rcTwo = new BamItemParent(this, BamItemType.RATING_CURVE, BamItemType.HYDRAULIC_CONFIG);
        JLabel rcOneLabel = new JLabel();
        T.t(this, rcOneLabel, false, "rc_n", 1);
        JLabel rcTwoLabel = new JLabel();
        T.t(this, rcTwoLabel, false, "rc_n", 2);
        RowColPanel rcChooserPanel = new RowColPanel(RowColPanel.AXIS.COL);
        rcChooserPanel.setGap(5);
        rcChooserPanel.appendChild(rcOneLabel);
        rcChooserPanel.appendChild(rcOne.cb);
        rcChooserPanel.appendChild(rcTwoLabel);
        rcChooserPanel.appendChild(rcTwo.cb);

        // plot items management
        RowColPanel plotItemManagementPanel = new RowColPanel();
        plotItemsList = new SimpleList<EditablePlotItem>();

        plotItemManagementPanel.appendChild(plotItemsList);

        Dimension dim = new Dimension(300, 0);
        plotItemsList.setMinimumSize(dim);
        plotItemsList.setPreferredSize(dim);

        // plot item edition
        RowColPanel plotItemEditionPanel = new RowColPanel();

        // plot configuration
        RowColPanel plotConfigPanel = new RowColPanel();

        // main plot
        plotContainer = new PlotContainer(true);

        // final layout

        RowColPanel plotItemsConfigPanel = new RowColPanel(RowColPanel.AXIS.COL);
        plotItemsConfigPanel.setGap(5);
        plotItemsConfigPanel.appendChild(rcChooserPanel, 0);
        plotItemsConfigPanel.appendChild(plotItemManagementPanel, 1);
        plotItemsConfigPanel.appendChild(plotItemEditionPanel, 0);
        RowColPanel plotPanel = new RowColPanel(RowColPanel.AXIS.COL);
        plotPanel.appendChild(plotConfigPanel, 0);
        plotPanel.appendChild(plotContainer, 1);
        SplitContainer mainContainer = new SplitContainer(plotItemsConfigPanel, plotPanel, true);
        setContent(mainContainer);

        // behavior

        rcOne.addChangeListener(l -> {
            handleRatingCurveSelectionChange();
        });

        rcTwo.addChangeListener(l -> {
            handleRatingCurveSelectionChange();
        });

        plotItemsList.addOrderChangeListeners(l -> {
            System.out.println("ORDER HAS CHANGED");
            resetPlot();
        });

        plotItemsList.addSelectionChangeListeners(l -> {
            System.out.println("SELECTION HAS CHANGED");
            System.out.println(Locale.getDefault());
            plotItemEditionPanel.clear();
            Object o = plotItemsList.getSelectedObject();
            if (o != null && o instanceof EditablePlotItem) {
                EditablePlotItem e = (EditablePlotItem) o;
                plotItemEditionPanel.appendChild(e.getEditionPanel());
            }
        });

        editablePlotItemsSet = new EditablePlotItemSet();

        // settings = new HashMap<>();
        // plotItems = new HashMap<>();

        /**
         * make a default order of component with total > param > mp
         * do not change legend order based on component order
         */
    }

    private void handleRatingCurveSelectionChange() {

        BamItem itemOne = rcOne.getCurrentBamItem();
        if (itemOne != null) {
            getEditablePlotItems(itemOne);
        }
        BamItem itemTwo = rcTwo.getCurrentBamItem();
        if (itemTwo != null) {
            getEditablePlotItems(itemTwo);
        }

        resetPlotItemList();
        resetPlot();

    }

    private void resetPlotItemList() {
        plotItemsList.clearList();
        BamItem itemOne = rcOne.getCurrentBamItem();
        if (itemOne != null) {
            for (EditablePlotItem ePltItem : editablePlotItemsSet.getEditablePlotItems(itemOne)) {
                plotItemsList.addItem(
                        itemOne.bamItemNameField.getText() + " > " + ePltItem.plotItem.getLabel(),
                        PlotUtils.getPlotItemIcon(ePltItem.plotItem, 30, 30),
                        ePltItem);
            }
        }
        BamItem itemTwo = rcTwo.getCurrentBamItem();
        if (itemTwo != null) {
            for (EditablePlotItem ePltItem : editablePlotItemsSet.getEditablePlotItems(itemTwo)) {
                plotItemsList.addItem(
                        itemTwo.bamItemNameField.getText() + " > " + ePltItem.plotItem.getLabel(),
                        PlotUtils.getPlotItemIcon(ePltItem.plotItem, 30, 30),
                        ePltItem);
            }
        }
    }

    private void resetPlot() {
        plot = new Plot();
        List<EditablePlotItem> items = plotItemsList.getValues();
        for (int k = items.size() - 1; k >= 0; k--) {
            plot.addXYItem(items.get(k).plotItem);
        }
        plot.chart.removeLegend();
        plot.chart.addLegend(getLegendTitle());
        plot.update();
        plotContainer.setPlot(plot);
    }

    private LegendTitle getLegendTitle() {
        Legend legend = new Legend();
        // here I add the elements by BamItems with a specific order
        BamItem itemOne = rcOne.getCurrentBamItem();
        if (itemOne != null) {
            legend.addLegendTitleItem(itemOne.bamItemNameField.getText());
            for (EditablePlotItem ePltItem : editablePlotItemsSet.getEditablePlotItems(itemOne)) {
                legend.addLegendItem(ePltItem.plotItem.getLegendItem());
            }
        }
        BamItem itemTwo = rcTwo.getCurrentBamItem();
        if (itemTwo != null) {
            legend.addLegendTitleItem(itemTwo.bamItemNameField.getText());
            for (EditablePlotItem ePltItem : editablePlotItemsSet.getEditablePlotItems(itemTwo)) {
                legend.addLegendItem(ePltItem.plotItem.getLegendItem());
            }
        }
        return legend.getLegendTitle(RectangleEdge.RIGHT, true);
    }

    private void getEditablePlotItems(BamItem item) {
        if (item instanceof IPlotDataProvider) {
            HashMap<String, PlotItem> bamItemPlotItems = ((IPlotDataProvider) item).getPlotItems();
            for (String key : bamItemPlotItems.keySet()) {
                PlotItem plotItem = bamItemPlotItems.get(key);
                plotItem.setLabel(T.text(plotItem.getLabel()));
                EditablePlotItem ePltItem = new EditablePlotItem(plotItem);
                ePltItem.addChangeListener(l -> {
                    resetPlot();
                    plotItemsList.modifyItemLabel(
                            plotItemsList.getFirstIndex(ePltItem),
                            item.bamItemNameField.getText() + " > " + ePltItem.plotItem.getLabel(),
                            PlotUtils.getPlotItemIcon(ePltItem.plotItem, 30, 30));
                    plotItemsList.repaint();
                });
                editablePlotItemsSet.addItem(key, item, ePltItem);
            }
        }
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        BamConfig config = new BamConfig(0);
        return config;
    }

    @Override
    public void load(BamConfig config) {
    }

}
