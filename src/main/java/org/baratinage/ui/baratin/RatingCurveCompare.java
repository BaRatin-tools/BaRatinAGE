package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;

import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPlotDataProvider;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotData;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotToolsPanel;
import org.baratinage.ui.component.SimpleList;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.Legend;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotUtils;
import org.baratinage.utils.Misc;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.json.JSONArray;
import org.json.JSONObject;

public class RatingCurveCompare extends BamItem {

    private static String[] episKeys = new String[] {
            RatingCurvePlotData.MAXPOST,
            RatingCurvePlotData.PARAM_U,
            RatingCurvePlotData.TOTAL_U,
            RatingCurvePlotData.STAGE_TRANSITION,
            RatingCurvePlotData.STAGE_TRANSITION_U,
    };

    private final SimpleTextField rcOneNameLabel;
    private final SimpleTextField rcTwoNameLabel;

    private final SimpleTextField xAxisLabelField;
    private final SimpleTextField yAxisLabelField;

    public final BamItemParent rcOne;
    public final BamItemParent rcTwo;
    public final PlotContainer plotContainer;
    private final RatingCurvePlotToolsPanel plotToolsPanel;

    private Plot plot;

    private final HashMap<BamItem, HashMap<String, EditablePlotItem>> knownEditablePlotItems;
    private final HashMap<BamItem, String> knownLabels;
    private final SimpleList<EPI> episList;

    private static record EPI(BamItem bamItem, String key) {
        public boolean isSame(EPI other) {
            return other.bamItem.ID.equals(bamItem.ID) && other.key.equals(key);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof EPI) {
                return (isSame((EPI) other));
            } else {
                return false;
            }
        }
    }

    public RatingCurveCompare(String uuid, BaratinProject project) {
        super(BamItemType.COMPARING_RATING_CURVES, uuid, project);

        // rating curves chooser

        JLabel rcOneLabel = new JLabel();
        T.t(this, rcOneLabel, false, "rc_n", 1);
        JLabel rcTwoLabel = new JLabel();
        T.t(this, rcTwoLabel, false, "rc_n", 2);
        SimpleFlowPanel rcChooserPanel = new SimpleFlowPanel(true);

        rcOne = new BamItemParent(this,
                BamItemType.RATING_CURVE,
                BamItemType.HYDRAULIC_CONFIG,
                BamItemType.HYDRAULIC_CONFIG_BAC,
                BamItemType.HYDRAULIC_CONFIG_QFH);
        rcTwo = new BamItemParent(this,
                BamItemType.RATING_CURVE,
                BamItemType.HYDRAULIC_CONFIG,
                BamItemType.HYDRAULIC_CONFIG_BAC,
                BamItemType.HYDRAULIC_CONFIG_QFH);

        rcOne.cb.setValidityView(true);
        rcTwo.cb.setValidityView(true);

        rcOneNameLabel = new SimpleTextField();
        rcTwoNameLabel = new SimpleTextField();

        rcChooserPanel.setGap(5);
        rcChooserPanel.addChild(rcOneLabel, false);
        rcChooserPanel.addChild(rcOne.cb, false);
        rcChooserPanel.addChild(rcOneNameLabel, false);
        rcChooserPanel.addChild(rcTwoLabel, false);
        rcChooserPanel.addChild(rcTwo.cb, false);
        rcChooserPanel.addChild(rcTwoNameLabel, false);

        // plot items management
        SimpleFlowPanel plotItemManagementPanel = new SimpleFlowPanel();
        episList = new SimpleList<>();
        plotItemManagementPanel.addChild(episList);
        Misc.setMinimumSize(episList, 300, null);

        // plot item edition
        SimpleFlowPanel plotItemEditionPanel = new SimpleFlowPanel(true);

        // plot configuration
        SimpleFlowPanel plotConfigPanel = new SimpleFlowPanel();
        plotConfigPanel.setGap(5);
        plotConfigPanel.setPadding(5);

        JLabel xAxisLabel = new JLabel();
        T.t(this, xAxisLabel, false, "stage_label");
        xAxisLabelField = new SimpleTextField();
        xAxisLabelField.setText(T.text("stage") + " [m]");
        xAxisLabelField.addChangeListener(l -> {
            resetPlot();
        });
        SimpleFlowPanel xAxisConfigPanel = new SimpleFlowPanel();
        xAxisConfigPanel.setGap(5);
        xAxisConfigPanel.addChild(xAxisLabel, false);
        xAxisConfigPanel.addChild(xAxisLabelField, true);
        plotConfigPanel.addChild(xAxisConfigPanel, true);

        JLabel yAxisLabel = new JLabel();
        T.t(this, yAxisLabel, false, "discharge_label");
        yAxisLabelField = new SimpleTextField();
        yAxisLabelField.setText(T.text("discharge") + " [m3/s]");
        yAxisLabelField.addChangeListener(l -> {
            resetPlot();
        });
        SimpleFlowPanel yAxisConfigPanel = new SimpleFlowPanel();
        yAxisConfigPanel.setGap(5);
        yAxisConfigPanel.addChild(yAxisLabel, false);
        yAxisConfigPanel.addChild(yAxisLabelField, true);
        plotConfigPanel.addChild(yAxisConfigPanel, true);

        // main plot
        plotContainer = new PlotContainer(true);
        plotToolsPanel = new RatingCurvePlotToolsPanel();
        plotToolsPanel.configure(true, true, true, true);

        SimpleFlowPanel plotArea = new SimpleFlowPanel(true);
        plotArea.setGap(5);
        plotArea.setPadding(5);
        plotArea.addChild(plotContainer, true);
        plotArea.addChild(plotToolsPanel, false);
        plotArea.addChild(plotConfigPanel, false);

        // final layout

        SimpleFlowPanel plotItemsConfigPanel = new SimpleFlowPanel(true);
        plotItemsConfigPanel.setGap(10);
        plotItemsConfigPanel.setPadding(5);
        plotItemsConfigPanel.addChild(rcChooserPanel, false);
        plotItemsConfigPanel.addChild(new SimpleSep(), false);
        plotItemsConfigPanel.addChild(plotItemManagementPanel, true);
        plotItemsConfigPanel.addChild(plotItemEditionPanel, false);

        SplitContainer mainContainer = new SplitContainer(
                plotItemsConfigPanel,
                plotArea,
                true,
                0.3f);
        setContent(mainContainer);

        // behavior

        knownEditablePlotItems = new HashMap<>();
        knownLabels = new HashMap<>();

        rcOne.addChangeListener(l -> {

            BamItem item = rcOne.getCurrentBamItem();
            updateKnownEditablePlotItems(item);

            if (item == null) {
                rcOneNameLabel.setText("");
            } else {
                String label = knownLabels.containsKey(item) ? knownLabels.get(item) : item.bamItemNameField.getText();
                rcOneNameLabel.setText(label);
                knownLabels.put(item, label);
            }

            resetPlotItemList();
            resetPlot();
        });

        rcTwo.addChangeListener(l -> {
            BamItem item = rcTwo.getCurrentBamItem();
            updateKnownEditablePlotItems(item);

            if (item == null) {
                rcTwoNameLabel.setText("");
            } else {
                String label = knownLabels.containsKey(item) ? knownLabels.get(item) : item.bamItemNameField.getText();
                rcTwoNameLabel.setText(label);
                knownLabels.put(item, label);
            }
            resetPlotItemList();
            resetPlot();
        });

        rcOneNameLabel.addChangeListener(l -> {
            BamItem item = rcOne.getCurrentBamItem();
            if (item != null) {
                knownLabels.put(item, rcOneNameLabel.getText());
            }
            resetPlot();
        });

        rcTwoNameLabel.addChangeListener(l -> {
            BamItem item = rcTwo.getCurrentBamItem();
            if (item != null) {
                knownLabels.put(item, rcTwoNameLabel.getText());
            }
            resetPlot();
        });

        episList.addOrderChangeListeners(l -> {
            resetPlot();
        });

        episList.addSelectionChangeListeners(l -> {
            plotItemEditionPanel.removeAll();
            plotItemEditionPanel.revalidate();
            List<EPI> epis = episList.getSelectedObjects();
            if (epis.size() == 0) {
                return;
            }
            EPI epi = episList.getSelectedObject();

            EditablePlotItem e = getEditablePlotItem(epi);
            if (e == null) {
                return;
            }

            List<EditablePlotItem> ePltItemList = getEditablePlotItems(epis);

            if (ePltItemList.size() > 1) {

            } else {
                SimpleFlowPanel ePanel = e.getEditionPanel();
                ePanel.setGap(5);
                ePanel.setPadding(5, 0, 5, 0);
                plotItemEditionPanel.addChild(ePanel, false);

            }
        });

        plotToolsPanel.addChangeListener(l -> {

            updateKnownEditablePlotItems(rcOne.getCurrentBamItem());
            updateKnownEditablePlotItems(rcTwo.getCurrentBamItem());

            resetPlotItemList();
            resetPlot();

        });

    }

    private EditablePlotItem getEditablePlotItem(EPI epi) {
        if (!knownEditablePlotItems.containsKey(epi.bamItem)) {
            return null;
        }
        if (!knownEditablePlotItems.get(epi.bamItem).containsKey(epi.key)) {
            return null;
        }
        return knownEditablePlotItems.get(epi.bamItem).get(epi.key);
    }

    private List<EditablePlotItem> getEditablePlotItems(List<EPI> epis) {
        return epis.stream().map(epi -> getEditablePlotItem(epi)).filter(e -> e != null).toList();
    }

    private void updateKnownEditablePlotItems(BamItem bamItem) {
        if (bamItem != null) {
            HashMap<String, EditablePlotItem> newEditablePlotItems = buildEditableRatingCurvePlotItems(bamItem);
            if (!knownEditablePlotItems.containsKey(bamItem)) {
                knownEditablePlotItems.put(bamItem, newEditablePlotItems);
            } else {
                for (String key : newEditablePlotItems.keySet()) {
                    if (!knownEditablePlotItems.get(bamItem).containsKey(key)) {
                        knownEditablePlotItems.get(bamItem).put(key, newEditablePlotItems.get(key));
                    } else {
                        EditablePlotItem existingEditablePlotItem = knownEditablePlotItems.get(bamItem).get(key);
                        EditablePlotItem newEditablePlotItem = newEditablePlotItems.get(key);
                        newEditablePlotItem.applyState(existingEditablePlotItem);
                        knownEditablePlotItems.get(bamItem).put(key, newEditablePlotItem);
                    }
                }
            }
        }
    }

    private void resetPlotItemList() {

        List<EPI> neededEPIs = new ArrayList<>();
        BamItem bamItemOne = rcOne.getCurrentBamItem();
        if (bamItemOne != null && knownEditablePlotItems.containsKey(bamItemOne)) {
            for (String key : episKeys) {
                if (knownEditablePlotItems.get(bamItemOne).containsKey(key)) {
                    neededEPIs.add(new EPI(bamItemOne, key));
                }
            }
        }
        BamItem bamItemTwo = rcTwo.getCurrentBamItem();
        if (bamItemTwo != null && knownEditablePlotItems.containsKey(bamItemTwo)) {
            for (String key : episKeys) {
                if (knownEditablePlotItems.get(bamItemTwo).containsKey(key)) {
                    neededEPIs.add(new EPI(bamItemTwo, key));
                }
            }
        }

        // remove no longer necessary items
        List<EPI> existingEPIs = episList.getAllObjects();
        for (EPI epi1 : existingEPIs) {
            boolean needed = false;
            for (EPI epi2 : neededEPIs) {
                if (epi1.equals(epi2)) {
                    needed = true;
                    break;
                }
            }
            if (!needed) {
                episList.removeItem(epi1);
            }
        }

        // add missing items
        existingEPIs = episList.getAllObjects();
        for (EPI epi1 : neededEPIs) {
            boolean missing = true;
            for (EPI epi2 : existingEPIs) {
                if (epi1.equals(epi2)) {
                    missing = false;
                    break;
                }
            }
            if (missing) {
                EditablePlotItem ePltItem = knownEditablePlotItems.get(epi1.bamItem).get(epi1.key);
                if (ePltItem == null) {
                    continue;
                }
                episList.addItem(
                        epi1.bamItem.bamItemNameField.getText() + " > " + ePltItem.plotItem.getLabel(),
                        PlotUtils.getPlotItemIcon(ePltItem.plotItem, 30, 30),
                        epi1);
            }
        }

    }

    private void resetPlot() {
        plot = new Plot();
        plotToolsPanel.updatePlotAxis(plot);
        List<EditablePlotItem> items = getEditablePlotItems(episList.getAllObjects());
        for (int k = items.size() - 1; k >= 0; k--) {
            if (items.get(k).plotItem.getVisible()) {
                plot.addXYItem(items.get(k).plotItem);
                for (PlotItem pi : items.get(k).siblings) {
                    plot.addXYItem(pi);
                }
            }
        }
        plot.chart.removeLegend();
        plot.chart.addLegend(getLegendTitle());
        plotContainer.setPlot(plot);
    }

    private LegendTitle getLegendTitle() {
        Legend legend = new Legend();
        // here I add the elements by BamItems with a specific order
        List<EPI> epis = episList.getAllObjects();
        BamItem itemOne = rcOne.getCurrentBamItem();
        if (itemOne != null && knownEditablePlotItems.containsKey(itemOne)) {
            legend.addLegendTitleItem(rcOneNameLabel.getText());
            for (EPI epi : epis) {
                if (epi.bamItem.ID.equals(itemOne.ID)) {
                    EditablePlotItem ePltItem = knownEditablePlotItems.get(itemOne).get(epi.key);
                    if (ePltItem != null && ePltItem.plotItem.getLegendVisible()) {
                        legend.addLegendItem(ePltItem.plotItem.getLegendItem());
                    }
                }
            }
        }
        BamItem itemTwo = rcTwo.getCurrentBamItem();
        if (itemTwo != null && knownEditablePlotItems.containsKey(itemTwo)) {
            legend.addLegendTitleItem(rcTwoNameLabel.getText());
            for (EPI epi : epis) {
                if (epi.bamItem.ID.equals(itemTwo.ID)) {
                    EditablePlotItem ePltItem = knownEditablePlotItems.get(itemTwo).get(epi.key);
                    if (ePltItem != null && ePltItem.plotItem.getLegendVisible()) {
                        legend.addLegendItem(ePltItem.plotItem.getLegendItem());
                    }
                }
            }
        }
        return legend.getLegendTitle(RectangleEdge.RIGHT, true);
    }

    private HashMap<String, EditablePlotItem> buildEditableRatingCurvePlotItems(BamItem item) {
        HashMap<String, EditablePlotItem> editableRatingCurvePlotItems = new HashMap<>();
        if (item == null) {
            return editableRatingCurvePlotItems;
        }

        HashMap<String, PlotItem> allPlotItems = new HashMap<>();
        if (item instanceof RatingCurve) {
            RatingCurvePlotData rcPlotData = ((RatingCurve) item).getRatingCurvePlotData();
            rcPlotData.smoothed = plotToolsPanel.totalEnvSmoothed();
            rcPlotData.axisFliped = plotToolsPanel.axisFlipped();
            allPlotItems = rcPlotData.getPlotItems();
        } else if (item instanceof HydraulicConfiguration || item instanceof HydraulicConfigurationBAC
                || item instanceof HydraulicConfigurationQFH) {
            RatingCurvePlotData rcPlotData = null;
            if (item instanceof HydraulicConfiguration)
                rcPlotData = ((HydraulicConfiguration) item).priorRatingCurve.getRatingCurvePlotData();
            if (item instanceof HydraulicConfigurationBAC)
                rcPlotData = ((HydraulicConfigurationBAC) item).priorRatingCurve.getRatingCurvePlotData();
            if (item instanceof HydraulicConfigurationQFH)
                rcPlotData = ((HydraulicConfigurationQFH) item).priorRatingCurve.getRatingCurvePlotData();
            if (rcPlotData != null) {
                rcPlotData.smoothed = plotToolsPanel.totalEnvSmoothed();
                rcPlotData.axisFliped = plotToolsPanel.axisFlipped();
                allPlotItems = rcPlotData.getPlotItems();
            }
        } else {
            allPlotItems = ((IPlotDataProvider) item).getPlotItems();
        }

        for (String key : episKeys) {
            List<PlotItem> plotItems = findMatchingPlotItems(allPlotItems, key);
            if (plotItems.size() > 0) {

                EditablePlotItem ePlotItem = buildEditableRatingCurvePlotItem(
                        plotItems.get(0), item, key);
                if (plotItems.size() > 1) {
                    for (int k = 1; k < plotItems.size(); k++) {
                        ePlotItem.addSibling(plotItems.get(k));
                    }
                }
                editableRatingCurvePlotItems.put(key, ePlotItem);
            }
        }
        return editableRatingCurvePlotItems;
    }

    private EditablePlotItem buildEditableRatingCurvePlotItem(PlotItem plotItem, BamItem bamItem, String key) {
        EditablePlotItem ePltItem = new EditablePlotItem(plotItem);
        ePltItem.addChangeListener(l -> {
            resetPlot();
            int index = episList.getFirstIndex(new EPI(bamItem, key));
            if (index >= 0) {
                episList.modifyItemLabel(
                        index,
                        bamItem.bamItemNameField.getText() + " > " + ePltItem.plotItem.getLabel(),
                        PlotUtils.getPlotItemIcon(ePltItem.plotItem, 30, 30));
            }
            episList.repaint();
        });
        return ePltItem;
    }

    private static List<PlotItem> findMatchingPlotItems(HashMap<String, PlotItem> plotItems, String keyPrefix) {
        List<PlotItem> result = plotItems.keySet().stream()
                .filter(k -> k.startsWith(keyPrefix))
                .map(k -> plotItems.get(k))
                .toList();
        return result;
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        BamConfig config = new BamConfig(0);

        JSONObject jsonKnownEditablePlotItems = new JSONObject();

        for (BamItem bamItem : knownEditablePlotItems.keySet()) {
            JSONObject jsonEPI = new JSONObject();
            for (String key : knownEditablePlotItems.get(bamItem).keySet()) {
                jsonEPI.put(key, knownEditablePlotItems.get(bamItem).get(key).toJSON());
            }
            jsonKnownEditablePlotItems.put(bamItem.ID, jsonEPI);
        }

        JSONObject jsonKnownLabels = new JSONObject();
        for (BamItem bamItem : knownLabels.keySet()) {
            jsonKnownLabels.put(bamItem.ID, knownLabels.get(bamItem));
        }

        JSONArray jsonEPIList = new JSONArray();
        for (EPI epi : episList.getAllObjects()) {
            JSONObject j = new JSONObject();
            j.put("bamItemId", epi.bamItem.ID);
            j.put("plotItemId", epi.key);
            jsonEPIList.put(j);
        }

        config.JSON.put("knownEditablePlotItems", jsonKnownEditablePlotItems);
        config.JSON.put("knownLabels", jsonKnownLabels);
        config.JSON.put("episList", jsonEPIList);
        config.JSON.put("rcOne", rcOne.saveConfig().JSON);
        config.JSON.put("rcTwo", rcTwo.saveConfig().JSON);
        config.JSON.put("rcOneNameLabel", rcOneNameLabel.getText());
        config.JSON.put("rcTwoNameLabel", rcTwoNameLabel.getText());
        config.JSON.put("logDischargeAxis", plotToolsPanel.logDischargeAxis());
        config.JSON.put("axisFlipped", plotToolsPanel.axisFlipped());
        config.JSON.put("totalEnvSmoothed", plotToolsPanel.totalEnvSmoothed());
        config.JSON.put("xAxisLabel", xAxisLabelField.getText());
        config.JSON.put("yAxisLabel", yAxisLabelField.getText());

        return config;
    }

    @Override
    public void load(BamConfig config) {

        JSONObject jsonKnownEditablePlotItems = config.JSON.optJSONObject("knownEditablePlotItems", null);
        if (jsonKnownEditablePlotItems != null) {
            for (String bamItemId : jsonKnownEditablePlotItems.keySet()) {
                BamItem bamItem = PROJECT.BAM_ITEMS.getBamItemWithId(bamItemId);
                if (bamItem != null) {
                    HashMap<String, EditablePlotItem> ePlotItems = buildEditableRatingCurvePlotItems(bamItem);
                    JSONObject jsonEPI = jsonKnownEditablePlotItems.getJSONObject(bamItemId);
                    for (String key : jsonEPI.keySet()) {
                        if (ePlotItems.containsKey(key)) {
                            ePlotItems.get(key).fromJSON(jsonEPI.getJSONObject(key));
                        }
                    }
                    knownEditablePlotItems.put(bamItem, ePlotItems);
                }
            }
        }

        JSONObject jsonKnownLabels = config.JSON.optJSONObject("knownLabels", null);
        if (jsonKnownLabels != null) {
            for (String bamItemId : jsonKnownLabels.keySet()) {
                BamItem bamItem = PROJECT.BAM_ITEMS.getBamItemWithId(bamItemId);
                if (bamItem != null) {
                    knownLabels.put(bamItem, jsonKnownLabels.getString(bamItemId));
                }
            }
        }

        JSONArray jsonEPIList = config.JSON.optJSONArray("episList", null);
        if (jsonEPIList != null) {
            for (int k = 0; k < jsonEPIList.length(); k++) {
                JSONObject j = jsonEPIList.optJSONObject(k, null);
                if (j != null) {
                    BamItem bamItem = PROJECT.BAM_ITEMS.getBamItemWithId(j.optString("bamItemId", ""));
                    String key = j.optString("plotItemId", "");
                    if (bamItem != null && !key.equals("")) {
                        EPI epi = new EPI(bamItem, key);
                        EditablePlotItem ePltItem = getEditablePlotItem(epi);
                        if (ePltItem != null) {
                            episList.addItem(
                                    epi.bamItem.bamItemNameField.getText() + " > " + ePltItem.plotItem.getLabel(),
                                    PlotUtils.getPlotItemIcon(ePltItem.plotItem, 30, 30),
                                    epi);
                        }
                    }
                }
            }
        }

        if (config.JSON.has("rcOneNameLabel")) {
            rcOneNameLabel.setText(config.JSON.getString("rcOneNameLabel"));
        }

        if (config.JSON.has("rcTwoNameLabel")) {
            rcTwoNameLabel.setText(config.JSON.getString("rcTwoNameLabel"));
        }

        if (config.JSON.has("logDischargeAxis")) {
            plotToolsPanel.setLogDischargeAxis(config.JSON.getBoolean("logDischargeAxis"));
        }

        if (config.JSON.has("axisFlipped")) {
            plotToolsPanel.setAxisFlipped(config.JSON.getBoolean("axisFlipped"));
        }

        if (config.JSON.has("totalEnvSmoothed")) {
            plotToolsPanel.setTotalEnvSmoothed(config.JSON.getBoolean("totalEnvSmoothed"));
        }

        if (config.JSON.has("xAxisLabel")) {
            xAxisLabelField.setText(config.JSON.getString("xAxisLabel"));
        }

        if (config.JSON.has("yAxisLabel")) {
            yAxisLabelField.setText(config.JSON.getString("yAxisLabel"));
        }

        if (config.JSON.has("rcOne")) {
            rcOne.fromJSON(config.JSON.getJSONObject("rcOne"), true);
        }

        if (config.JSON.has("rcTwo")) {
            rcTwo.fromJSON(config.JSON.getJSONObject("rcTwo"), true);
        }

        resetPlotItemList();
        resetPlot();
    }

}
