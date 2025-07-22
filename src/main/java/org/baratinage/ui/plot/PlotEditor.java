package org.baratinage.ui.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleDialog;
import org.baratinage.ui.component.SimpleList;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.Misc;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlotEditor extends SimpleFlowPanel {

  private final Map<String, EditablePlotItem> editablePlotItems;

  private final Map<String, EditablePlot> editablePlots;

  private final SimpleFlowPanel globalEditionPanel;
  private final SimpleFlowPanel editionPanel;

  private final SimpleList<String> itemExplorer;

  private final JButton resetPlotButton;

  private JSONObject defaultPlotConfig = null;

  public PlotEditor() {
    super(true);
    setPadding(5);
    setGap(5);

    editablePlotItems = new HashMap<>();
    editablePlots = new HashMap<>();

    globalEditionPanel = new SimpleFlowPanel(true);
    globalEditionPanel.setGap(5);

    editionPanel = new SimpleFlowPanel(true);

    itemExplorer = new SimpleList<>();

    itemExplorer.addSelectionChangeListeners(l -> {
      updateEditionPanel();
    });

    itemExplorer.addOrderChangeListeners(l -> {
      updateItemsOrder();
    });

    resetPlotButton = new JButton();
    resetPlotButton.addActionListener((e) -> {
      if (defaultPlotConfig != null) {
        fromJSON(defaultPlotConfig);
      }
    });
    T.t(this, resetPlotButton, false, "reset_plot");

    addChild(globalEditionPanel, false);
    addChild(itemExplorer, true);
    addChild(editionPanel, false);
    addChild(resetPlotButton, false);

    Misc.setPreferredSize(itemExplorer, 300, null);
  }

  public void saveAsDefault(boolean override) {
    if (defaultPlotConfig == null) {
      saveAsDefault();
    } else {
      if (override) {
        saveAsDefault();
      }
    }
  }

  private void saveAsDefault() {
    JSONObject saved = toJSON();
    if (saved.has("default")) {
      saved.remove("default");
    }
    defaultPlotConfig = saved;
  }

  public void reset() {
    editablePlotItems.clear();
    editablePlots.clear();
    itemExplorer.clearList();
  }

  private void updateEditionPanel() {
    editionPanel.removeAll();
    String epiId = itemExplorer.getSelectedObject();
    if (!editablePlotItems.containsKey(epiId)) {
      return;
    }
    EditablePlotItem epi = editablePlotItems.get(epiId);

    SimpleFlowPanel panel = epi.getEditionPanel();
    panel.setPadding(5);
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    editionPanel.addChild(scrollPane, false);
  }

  private void updateItemsOrder() {
    List<String> epiId = itemExplorer.getAllObjects();

    List<PlotItem> plotItemsToReorder = new ArrayList<>();
    for (String id : epiId) {
      if (!editablePlotItems.containsKey(id)) {
        continue;
      }
      EditablePlotItem epi = editablePlotItems.get(id);
      plotItemsToReorder.add(epi.plotItem);
    }
    for (EditablePlot ep : editablePlots.values()) {
      ep.plot.reorderXYItems(plotItemsToReorder.reversed());
      ep.plot.update();
    }
  }

  public void addEditablePlot(Plot plot) {
    addEditablePlot("main", plot);
  }

  public void addEditablePlot(String id, Plot plot) {
    EditablePlot ep = new EditablePlot(plot);
    // ep.setConfig(editablePlotItems, itemExplorer.getAllObjects());
    ep.updateEditablePlotItems(editablePlotItems);
    if (editablePlots.containsKey(id)) {
      ep.applyState(editablePlots.get(id));
    }
    editablePlots.put(id, ep);
  }

  public EditablePlot getEditablePlot() {
    return getEditablePlot("main");
  }

  public EditablePlot getEditablePlot(String id) {
    return editablePlots.containsKey(id) ? editablePlots.get(id) : null;
  }

  public void addEditablePlotItem(String id, String title, EditablePlotItem editablePlotItem) {
    editablePlotItem.addChangeListener(l -> {
      for (EditablePlot ep : editablePlots.values()) {
        ep.plot.update();
        ep.update();
      }
      for (String epiId : editablePlotItems.keySet()) {
        updateItemExplorer(epiId);
      }
    });

    int index = -1;
    if (editablePlotItems.containsKey(id)) {
      editablePlotItem.applyState(editablePlotItems.get(id));
      index = itemExplorer.getFirstIndex(id);
    }
    Icon icon = PlotUtils.getPlotItemIcon(editablePlotItem.plotItem, 30, 30);
    if (index < 0) {
      itemExplorer.addItem(title, icon, id);
    } else {
      itemExplorer.modifyItemLabel(index, title, icon);
    }
    editablePlotItems.put(id, editablePlotItem);

    for (EditablePlot ep : editablePlots.values()) {
      // ep.setConfig(editablePlotItems, itemExplorer.getAllObjects());
      ep.updateEditablePlotItems(editablePlotItems);
    }
  }

  public void addEditablePlotItem(String id, String title, PlotItem plotItem) {
    EditablePlotItem editablePlotItem = new EditablePlotItem(plotItem);
    addEditablePlotItem(id, title, editablePlotItem);
  }

  public EditablePlotItem getEditablePlotItem(String id) {
    return editablePlotItems.containsKey(id) ? editablePlotItems.get(id) : null;
  }

  private void updateItemExplorer(String epiId) {
    if (!editablePlotItems.containsKey(epiId)) {
      return;
    }
    EditablePlotItem epi = editablePlotItems.get(epiId);
    String label = epi.getLabel();
    Icon icon = PlotUtils.getPlotItemIcon(epi.plotItem, 30, 30);
    int index = itemExplorer.getFirstIndex(epiId);
    if (index >= 0) {
      itemExplorer.modifyItemLabel(index, label, icon);
    }
  }

  public void updateEditor() {

    updateEditionPanel();
    updateItemsOrder();
    for (String id : editablePlotItems.keySet()) {
      updateItemExplorer(id);
    }
    for (EditablePlot ep : editablePlots.values()) {
      ep.update();
    }

    globalEditionPanel.removeAll();

    int k = 0;
    for (String id : editablePlots.keySet()) {
      k++;

      EditablePlot ep = editablePlots.get(id);

      SimpleFlowPanel plotPanel = new SimpleFlowPanel(true);

      JButton openPlotEditorBtn = new JButton();
      String btnLbl = T.text("plot_axis_and_legend");
      openPlotEditorBtn.setText(editablePlots.size() > 1 ? String.format("%s (%d)", btnLbl, k) : btnLbl);
      openPlotEditorBtn.addActionListener(l -> {
        SimpleDialog d = SimpleDialog.buildInfoDialog(
            this,
            T.text("plot_axis_and_legend"),
            new JScrollPane(ep.getEditionPanel()));
        d.setSize(400, 500);
        d.openDialog();
      });
      plotPanel.addChild(openPlotEditorBtn, false);

      globalEditionPanel.addChild(plotPanel, false);

    }

  }

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();

    JSONArray episJson = new JSONArray();
    for (String epiId : editablePlotItems.keySet()) {
      JSONObject epiJson = new JSONObject();
      epiJson.put("id", epiId);
      epiJson.put("config", editablePlotItems.get(epiId).toJSON());
      episJson.put(epiJson);
    }
    json.put("editablePlotItems", episJson);

    JSONArray epsJson = new JSONArray();
    for (String epId : editablePlots.keySet()) {
      JSONObject epJson = new JSONObject();
      epJson.put("id", epId);
      epJson.put("config", editablePlots.get(epId).toJSON());
      epsJson.put(epJson);
    }
    json.put("editablePlots", epsJson);

    json.put("itemsOrder", itemExplorer.getAllObjects());

    if (defaultPlotConfig != null) {
      json.put("default", defaultPlotConfig);
    }

    return json;
  }

  public void fromJSON(JSONObject json) {

    JSONArray episJson = json.optJSONArray("editablePlotItems");
    JSONArray epsJson = json.optJSONArray("editablePlots");
    JSONArray itemsOrderJson = json.optJSONArray("itemsOrder");

    if (episJson == null || epsJson == null || itemsOrderJson == null) {
      System.out.println("Invalid configuration");
      return;
    }

    for (int k = 0; k < episJson.length(); k++) {
      JSONObject epiJson = episJson.getJSONObject(k);
      String epiId = epiJson.getString("id");
      if (!editablePlotItems.containsKey(epiId)) {
        continue;
      }
      EditablePlotItem epi = editablePlotItems.get(epiId);
      epi.fromJSON(epiJson.getJSONObject("config"));
    }

    for (int k = 0; k < epsJson.length(); k++) {
      JSONObject epJson = epsJson.getJSONObject(k);
      String epId = epJson.getString("id");
      if (!editablePlots.containsKey(epId)) {
        continue;
      }
      EditablePlot ep = editablePlots.get(epId);
      ep.updateEditablePlotItems(editablePlotItems);
      if (!epJson.has("config")) {
        JSONArray idsOrTitlesArr = epJson.getJSONArray("idsOrTitles");
        List<String> idsOrTitles = new ArrayList<>();
        for (int i = 0; i < idsOrTitlesArr.length(); i++) {
          idsOrTitles.add(idsOrTitlesArr.getString(i));
        }
        ep.updateLegendItems(idsOrTitles);

        continue;
      }
      ep.fromJSON(epJson.getJSONObject("config"));
    }

    List<String> itemsOrder = new ArrayList<>();
    for (int i = 0; i < itemsOrderJson.length(); i++) {
      itemsOrder.add(itemsOrderJson.getString(i));
    }
    itemExplorer.reorderItems(itemsOrder);

    if (json.has("default")) {
      defaultPlotConfig = json.getJSONObject("default");
    }

    updateEditor();

  }
}
