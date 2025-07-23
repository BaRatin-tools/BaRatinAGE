package org.baratinage.ui.plot;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleCheckbox;
import org.baratinage.ui.component.SimpleFrame;
import org.baratinage.ui.component.SimpleList;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.json.JSONArray;
import org.json.JSONObject;

public class EditablePlot implements LegendItemSource {

  public final Plot plot;

  // this holds both editable plot items ids or title value
  private final SimpleList<String> legendItemsOrder = new SimpleList<>();
  private final Map<String, EditablePlotItem> editablePlotItems = new HashMap<>();

  private final SimpleFlowPanel legendItemEditorPanel = new SimpleFlowPanel(true);

  private boolean showLegend = true;
  // private boolean showPlot = true;
  private String xAxisLabel = "x axis";
  // private boolean xAxisLog = false;
  // private boolean xAxisTimeseries = false;
  private String yAxisLabel = "y axis";
  // private boolean yAxisLog = false;

  public EditablePlot(Plot plot) {
    this.plot = plot;

    ValueAxis xAxis = plot.plot.getDomainAxis();
    xAxisLabel = xAxis.getLabel();
    // xAxisLog = xAxis.equals(plot.axisXlog);
    // xAxisTimeseries = xAxis.equals(plot.axisXdate);
    ValueAxis yAxis = plot.plot.getRangeAxis();
    yAxisLabel = yAxis.getLabel();
    // yAxisLog = yAxis.equals(plot.axisYlog);

    legendItemsOrder.addOrderChangeListeners(l -> {
      updatePlotLegend();
    });

    legendItemsOrder.addSelectionChangeListeners(l -> {

      legendItemEditorPanel.removeAll();

      String id = legendItemsOrder.getSelectedObject();
      if (id == null) {
        return;
      }
      EditablePlotItem epi = editablePlotItems.containsKey(id) ? editablePlotItems.get(id) : null;
      if (epi == null) {

        int index = legendItemsOrder.getFirstIndex(id);

        JButton remItemBtn = new JButton();
        remItemBtn.setText("supprimer");
        remItemBtn.addActionListener(e -> {
          legendItemsOrder.removeItem(id);
          legendItemsOrder.updateUI();

        });

        JLabel titleLbl = new JLabel();
        titleLbl.setText(T.text("legend_text"));
        SimpleTextField titleField = new SimpleTextField();
        titleField.setText(
            id);
        titleField.addChangeListener(e -> {
          legendItemsOrder.modifyItemLabel(index, titleField.getText());
          legendItemsOrder.modifyItemValue(index, titleField.getText());
          legendItemsOrder.updateUI(); // ???
          update();
        });

        legendItemEditorPanel.addChild(remItemBtn, false);
        legendItemEditorPanel.addChild(titleLbl, false);
        legendItemEditorPanel.addChild(titleField, false);

      }

    });
  }

  public void updateEditablePlotItems(Map<String, EditablePlotItem> epis) {
    editablePlotItems.clear();
    for (String id : epis.keySet()) {
      EditablePlotItem epi = epis.get(id);
      editablePlotItems.put(id, epi);
    }
    updateEditablePlotItems();
  }

  public void updateLegendItems(String... idsOrTitles) {
    List<String> idsOrTitleArr = new ArrayList<>();
    for (String idOrTitle : idsOrTitles) {
      if (idOrTitle != null) {
        idsOrTitleArr.add(idOrTitle);
      }
    }
    updateLegendItems(idsOrTitleArr);
  }

  public void updateLegendItems(List<String> idsOrTitles) {
    legendItemsOrder.clearList();
    for (String idOrTitle : idsOrTitles) {
      if (editablePlotItems.containsKey(idOrTitle)) {
        EditablePlotItem epi = editablePlotItems.get(idOrTitle);
        Icon icon = PlotUtils.getPlotItemIcon(epi.plotItem, 30, 30);
        legendItemsOrder.addItem(epi.getLabel(), icon, idOrTitle);
      } else {
        legendItemsOrder.addItem(idOrTitle, AppSetup.ICONS.EDIT, idOrTitle);
      }
    }
    updateEditablePlotItems();
  }

  public List<String> getIdsOrTitles() {
    return legendItemsOrder.getAllObjects();
  }

  public void update() {
    updateEditablePlotItems();
    plot.setXAxisLabel(xAxisLabel);
    plot.setYAxisLabel(yAxisLabel);
  }

  private void updateEditablePlotItems() {
    for (String id : editablePlotItems.keySet()) {
      EditablePlotItem epi = editablePlotItems.get(id);
      if (!plot.items.contains(epi.plotItem)) {
        continue;
      }
      if (legendItemsOrder.containsObject(id)) {
        if (!epi.showLegend()) {
          legendItemsOrder.removeItem(id);
        } else {
          Icon icon = PlotUtils.getPlotItemIcon(epi.plotItem, 30, 30);
          int index = legendItemsOrder.getFirstIndex(id);
          legendItemsOrder.modifyItemLabel(index, epi.getLabel(), icon);
        }
      } else {
        if (epi.showLegend()) {
          Icon icon = PlotUtils.getPlotItemIcon(epi.plotItem, 30, 30);
          legendItemsOrder.addItem(epi.getLabel(), icon, id);
        }
      }
    }
    updatePlotLegend();
  }

  private void updatePlotLegend() {
    plot.setLegend(this, RectangleEdge.RIGHT);
  }

  public SimpleFlowPanel getEditionPanel() {

    SimpleFlowPanel editionPanel = new SimpleFlowPanel(true);
    editionPanel.setGap(5);
    editionPanel.setPadding(5);

    JLabel legendItemsLabel = new JLabel();
    legendItemsLabel.setText(T.text("legend_items"));
    JButton addTitleInLegendBtn = new JButton();
    addTitleInLegendBtn.setText(T.text("add_title"));
    addTitleInLegendBtn.addActionListener(l -> {
      String defaultTitle = "";
      legendItemsOrder.addItem(defaultTitle, AppSetup.ICONS.EDIT, defaultTitle);
    });

    JButton legendPlotBtn = new JButton();
    legendPlotBtn.setText(T.text("plot_legend"));
    legendPlotBtn.addActionListener(l -> {
      LegendItemCollection legendItems = getLegendItems();
      Legend legend = new Legend(legendItems);
      LegendTitle legendTitle = legend.getLegendTitle();
      Dimension dim = Legend.getSize(legendTitle);
      Plot plot = Legend.getLegendPlot(legendTitle);
      PlotContainer pc = new PlotContainer(plot, true);
      SimpleFrame sf = new SimpleFrame(legendPlotBtn, dim.width, dim.height + 150);
      sf.showContent(pc);
    });

    SimpleCheckbox showLegendCb = new SimpleCheckbox();
    showLegendCb.setText(T.text("show_legend"));
    showLegendCb.setSelected(showLegend);
    showLegendCb.addChangeListener(l -> {
      showLegend = showLegendCb.isSelected();
      updatePlot();
    });

    SimpleFlowPanel xAxisPanel = new SimpleFlowPanel();
    xAxisPanel.setGap(5);
    JLabel xAxisLabelFieldLabel = new JLabel();
    xAxisLabelFieldLabel.setText(T.text("x_axis_label"));
    SimpleTextField xAxisLabelField = new SimpleTextField();
    xAxisLabelField.setText(xAxisLabel);
    xAxisLabelField.addChangeListener(l -> {
      xAxisLabel = xAxisLabelField.getText();
      updatePlot();
    });
    xAxisPanel.addChild(xAxisLabelFieldLabel, false);
    xAxisPanel.addChild(xAxisLabelField, true);

    SimpleFlowPanel yAxisPanel = new SimpleFlowPanel();
    yAxisPanel.setGap(5);
    JLabel yAxisLabelFieldLabel = new JLabel();
    yAxisLabelFieldLabel.setText(T.text("y_axis_label"));
    SimpleTextField yAxisLabelField = new SimpleTextField();
    yAxisLabelField.setText(yAxisLabel);
    yAxisLabelField.addChangeListener(l -> {
      yAxisLabel = yAxisLabelField.getText();
      updatePlot();
    });
    yAxisPanel.addChild(yAxisLabelFieldLabel, false);
    yAxisPanel.addChild(yAxisLabelField, true);

    editionPanel.addChild(legendItemsLabel, false);
    editionPanel.addChild(legendItemsOrder, false);
    editionPanel.addChild(addTitleInLegendBtn, false);
    editionPanel.addChild(legendPlotBtn, false);
    editionPanel.addChild(legendItemEditorPanel, false);
    editionPanel.addChild(new SimpleSep(), false);
    editionPanel.addChild(showLegendCb, false);
    editionPanel.addChild(xAxisPanel, false);
    editionPanel.addChild(yAxisPanel, false);

    return editionPanel;
  }

  private void updatePlot() {
    plot.setIncludeLegend(showLegend);
    plot.axisX.setLabel(xAxisLabel);
    plot.axisXlog.setLabel(xAxisLabel);
    plot.axisXdate.setLabel(xAxisLabel);
    plot.axisY.setLabel(yAxisLabel);
    plot.axisYlog.setLabel(yAxisLabel);
    plot.update();
  }

  public boolean showLegend() {
    return showLegend;
  }

  public void setShowLegend(boolean show) {
    showLegend = show;
    update();
  }

  public String getXAxisLabel() {
    return xAxisLabel;
  }

  public void setXAxisLabel(String label) {
    xAxisLabel = label;
    update();
  }

  public String getYAxisLabel() {
    return yAxisLabel;
  }

  public void setYAxisLabel(String label) {
    yAxisLabel = label;
    update();
  }

  public void applyState(EditablePlot other) {
    this.showLegend = other.showLegend;
    this.xAxisLabel = other.xAxisLabel;
    this.yAxisLabel = other.yAxisLabel;
    this.legendItemsOrder.clearList();
    for (String id : other.legendItemsOrder.getAllObjects()) {
      JLabel lbl = other.legendItemsOrder.getLabel(id);
      this.legendItemsOrder.addItem(lbl.getText(), lbl.getIcon(), id);
    }
    updatePlot();
    updatePlotLegend();
  }

  @Override
  public LegendItemCollection getLegendItems() {
    Legend legend = new Legend();

    for (String id : legendItemsOrder.getAllObjects()) {
      if (editablePlotItems.containsKey(id)) {
        LegendItem legendItem = editablePlotItems.get(id).plotItem.getLegendItem();
        legend.addLegendItem(legendItem);
      } else {
        legend.addLegendTitleItem(id);
      }
    }
    return legend.getLegendItems();
  }

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();

    json.put("idsOrTitles", legendItemsOrder.getAllObjects());

    json.put("showLegend", showLegend);
    json.put("xAxis", xAxisLabel);
    json.put("yAxis", yAxisLabel);

    return json;
  }

  public void fromJSON(JSONObject json) {

    List<String> idsOrTitles = new ArrayList<>();
    JSONArray idsOrTitlesArr = json.getJSONArray("idsOrTitles");
    for (int k = 0; k < idsOrTitlesArr.length(); k++) {
      idsOrTitles.add(idsOrTitlesArr.getString(k));
    }
    updateLegendItems(idsOrTitles);

    showLegend = json.getBoolean("showLegend");
    xAxisLabel = json.optString("xAxis", null);
    yAxisLabel = json.optString("yAxis", null);

    updatePlot();
  }
}
