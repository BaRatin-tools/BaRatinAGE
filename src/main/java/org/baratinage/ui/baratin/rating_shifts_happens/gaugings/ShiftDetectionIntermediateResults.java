package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.util.HashMap;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.plot.ColorPalette;

public class ShiftDetectionIntermediateResults extends SimpleFlowPanel {

  private ColorPalette palette;
  private final HashMap<String, ShiftDetectionResults> results;

  public ShiftDetectionIntermediateResults(ShiftDetectionIteration ratingShiftDetection) {

    results = new HashMap<>();
    palette = ColorPalette.VIRIDIS;

    SimpleFlowPanel currentPanel = new SimpleFlowPanel();

    Explorer explorer = new Explorer();
    buildTree(ratingShiftDetection, explorer);
    explorer.addTreeSelectionListener(l -> {
      ExplorerItem item = explorer.getLastSelectedPathComponent();
      if (item == null) {
        return;
      }
      currentPanel.removeAll();
      if (results.containsKey(item.id)) {
        currentPanel.addChild(results.get(item.id).mainPlot);
      } else {
        ShiftDetectionIteration selectedRsd = ratingShiftDetection.getRatingShiftDetection(item.id);
        if (selectedRsd != null) {
          ShiftDetectionResults localRes = new ShiftDetectionResults(selectedRsd, true);
          results.put(item.id, localRes);
          localRes.mainPlot.setPalette(palette);
          localRes.mainPlot.updatePlot();
          currentPanel.addChild(localRes.mainPlot);
        }
      }
    });

    SplitContainer container = new SplitContainer(explorer, currentPanel, true);

    addChild(container, true);
  }

  public void setPalette(ColorPalette palette) {
    this.palette = palette;
    for (ShiftDetectionResults res : results.values()) {
      res.mainPlot.setPalette(palette);
    }
  }

  public void updatePlots() {
    for (ShiftDetectionResults res : results.values()) {
      res.mainPlot.updatePlot();
    }
  }

  private static void buildTree(
      ShiftDetectionIteration ratingShiftDetection,
      Explorer explorer) {
    buildTree(ratingShiftDetection, null, explorer);
  }

  private static void buildTree(
      ShiftDetectionIteration ratingShiftDetection,
      ExplorerItem parent,
      Explorer explorer) {
    ExplorerItem node = new ExplorerItem(
        ratingShiftDetection.ID,
        ratingShiftDetection.getName(),
        AppSetup.ICONS.getCustomAppImageIcon(BamItemType.RATING_SHIFT_HAPPENS.id + ".svg"),
        parent);
    explorer.appendItem(node);
    List<ShiftDetectionIteration> children = ratingShiftDetection.getChildren();

    for (ShiftDetectionIteration child : children) {
      if (child.getChildren().size() == 0) {
        continue;
      }
      buildTree(child, node, explorer);
    }
  }
}
