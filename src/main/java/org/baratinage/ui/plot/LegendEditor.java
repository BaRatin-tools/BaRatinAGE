package org.baratinage.ui.plot;

import java.util.List;

import javax.swing.Icon;

import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleCheckbox;
import org.baratinage.ui.component.SimpleList;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.Misc;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;

public class LegendEditor extends SimpleFlowPanel implements LegendItemSource {

  private static class LegendItemConfig {
    public EditablePlotItem editablePlotItem;
    public boolean visible = true;

    public LegendItemConfig(EditablePlotItem editablePlotItem) {
      this.editablePlotItem = editablePlotItem;
    }
  };

  /**
   * input should be legendItems associated with ids?
   * 
   */

  // private final Plot plot;

  private final PlotContainer legendPlotContainer;
  private Plot legendPlot;
  private final SimpleFlowPanel editorPanel;
  private final SimpleList<LegendItemConfig> legendItemExporer;

  public LegendEditor() {
    super(false);
    setGap(5);
    /**
     * it takes a Plot -- DONE
     * generates a standalone legend plot -- DONE
     * allow reordering -- DONE
     * allow hiding/showing --
     * allow renaming
     * allow adding titles
     * allow changing the legend of the original plot
     */

    // this.plot = plot;

    legendPlotContainer = new PlotContainer();
    Misc.setMinimumSize(this, 300, 200);
    Misc.setPreferredSize(this, 300, 200);
    editorPanel = new SimpleFlowPanel();

    legendItemExporer = new SimpleList<>();
    legendItemExporer.addSelectionChangeListeners(l -> {
      editorPanel.removeAll();
      LegendItemConfig item = legendItemExporer.getSelectedObject();
      if (item == null) {
        return;
      }
      editorPanel.addChild(updateLegendItemEditionPanel(item));
    });

    legendItemExporer.addOrderChangeListeners(l -> {
      updatePlot();
    });

    SimpleFlowPanel sidePanel = new SimpleFlowPanel(true);
    sidePanel.addChild(legendItemExporer, true);
    sidePanel.addChild(editorPanel, false);

    // JButton addTitle

    addChild(sidePanel, false);
    addChild(legendPlotContainer, true);

    // resetPlotLegend();
    // updatePlot();
  }

  private SimpleFlowPanel updateLegendItemEditionPanel(LegendItemConfig item) {

    SimpleFlowPanel panel = new SimpleFlowPanel(true);

    SimpleCheckbox visibilityCb = new SimpleCheckbox();
    visibilityCb.setText(T.text("include_legend"));
    visibilityCb.setSelected(item.visible);
    visibilityCb.addChangeListener(e -> {
      item.visible = visibilityCb.isSelected();
      updatePlot();
    });
    panel.addChild(visibilityCb, false);

    SimpleTextField legendLabelTextField = new SimpleTextField();
    legendLabelTextField.setText(item.editablePlotItem.plotItem.getLabel());
    legendLabelTextField.addChangeListener(e -> {
      item.editablePlotItem.plotItem.setLabel(legendLabelTextField.getText());
      updatePlot();
      updateLegendItemExplorer();
    });
    panel.addChild(legendLabelTextField, false);

    return panel;
  }

  // public

  private void updatePlot() {
    Legend lgd = new Legend(getLegendItems());
    legendPlot = lgd.getLegendPlot();
    legendPlotContainer.setPlot(legendPlot);
  }

  private void updateLegendItemExplorer() {
    for (int k = 0; k < legendItemExporer.getItemCount(); k++) {
      legendItemExporer.modifyItemLabel(
          k,
          legendItemExporer.getObject(k).editablePlotItem.plotItem.getLabel());
    }
  }

  public void setEditablePlotItem(List<EditablePlotItem> editablePlotItems) {
    legendItemExporer.clearList();
    for (int k = 0; k < editablePlotItems.size(); k++) {
      EditablePlotItem item = editablePlotItems.get(k);
      // if (item.visible && item.legend) {
      Icon icon = PlotUtils.getPlotItemIcon(item.plotItem, 30, 30);
      legendItemExporer.addItem(
          item.plotItem.getLabel(),
          icon,
          new LegendItemConfig(item));
    }
    updatePlot();
  }

  // }
  // public void resetPlotLegend() {
  // List<PlotItem> plotItems = plot.items;
  // legendItemExporer.clearList();
  // for (int k = 0; k < plotItems.size(); k++) {
  // PlotItem item = plotItems.get(k);
  // if (item.visible && item.legend) {
  // Icon icon = PlotUtils.getPlotItemIcon(item, 30, 30);
  // legendItemExporer.addItem(
  // item.label,
  // icon,
  // new LegendItemConfig(item));
  // }
  // }
  // }

  @Override
  public LegendItemCollection getLegendItems() {
    LegendItemCollection lgdCollection = new LegendItemCollection();
    for (LegendItemConfig item : legendItemExporer.getAllObjects()) {
      if (item.visible) {
        lgdCollection.add(item.editablePlotItem.plotItem.getLegendItem());
      }
    }
    return lgdCollection;
  }

}
