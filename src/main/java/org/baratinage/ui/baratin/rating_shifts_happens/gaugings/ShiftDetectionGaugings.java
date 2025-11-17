package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.baratin.Gaugings;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotToolsPanel;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults.ResultPeriod;
import org.baratinage.ui.component.SimpleColorField;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.ColorPalette;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.ui.plot.PlotPoints;

public class ShiftDetectionGaugings extends SimpleFlowPanel {

  public final PlotEditor plotEditor;
  private final JToggleButton plotEditorToggleBtn;

  private final List<ResultPeriodAndActions> periods;
  private final RatingCurvePlotToolsPanel toolsPanel;

  public final PlotContainer plotContainer;
  private ColorPalette palette;

  public ShiftDetectionGaugings(List<ResultPeriod> periods) {
    setGap(5);

    toolsPanel = new RatingCurvePlotToolsPanel();
    toolsPanel.configure(true, true, false, false);

    SimpleFlowPanel periodSelectionPanel = new SimpleFlowPanel(true);
    periodSelectionPanel.setPadding(5);
    periodSelectionPanel.setGap(5);
    SimpleFlowPanel plotPanel = new SimpleFlowPanel(true);
    plotPanel.setGap(5);

    plotContainer = new PlotContainer();

    toolsPanel.addChangeListener(l -> {
      updatePlot();
    });

    plotEditor = new PlotEditor();
    plotEditorToggleBtn = new JToggleButton();
    plotEditorToggleBtn.setIcon(AppSetup.ICONS.EDIT);
    plotEditorToggleBtn.addActionListener(l -> {
      removeAll();
      addChild(periodSelectionPanel, false);
      if (plotEditorToggleBtn.isSelected()) {
        addChild(plotEditor, false);
      }
      addChild(plotPanel, true);
    });
    plotContainer.toolsPanel.addChild(plotEditorToggleBtn, false);

    plotPanel.addChild(plotContainer, true);
    plotPanel.addChild(toolsPanel, false);

    addChild(periodSelectionPanel, false);
    addChild(plotPanel, true);

    this.periods = new ArrayList<>();
    for (ResultPeriod p : periods) {
      ResultPeriodAndActions rpaa = buildPeriodAction(p);
      this.periods.add(rpaa);
      periodSelectionPanel.addChild(rpaa.panel(), false);
    }

    JButton buildGaugingItemBtn = new JButton();
    buildGaugingItemBtn.setIcon(BamItemType.GAUGINGS.getAddIcon());
    buildGaugingItemBtn.setText(T.html("build_all_components", T.text(BamItemType.GAUGINGS.id)));
    buildGaugingItemBtn.addActionListener(l -> {
      for (ResultPeriod p : periods) {
        addGaugingBamItemFromPeriod(p);
      }
    });

    periodSelectionPanel.addChild(buildGaugingItemBtn, false);

    palette = ColorPalette.CIVIDIS;
    updatePlot();
  }

  public void setPalette(ColorPalette palette) {
    this.palette = palette;
    plotEditor.reset();
    updatePlot();
  }

  public void updatePlot() {

    int n = periods.size();
    Color[] colors = palette.getColors(n);

    List<PlotPoints> gaugingsPoints = new ArrayList<>();
    for (int k = 0; k < n; k++) {
      ResultPeriodAndActions p = periods.get(k);
      p.label.setIcon(SimpleColorField.buildColorIcon(colors[k], 20));
      PlotPoints points = toolsPanel.axisFlipped() ? p.period().hQPoints() : p.period().QhPoints();
      points.setPaint(colors[k]);
      gaugingsPoints.add(points);

    }
    // plotEditor.set

    Plot plot = new Plot();
    toolsPanel.updatePlotAxis(plot);

    plot.addXYItems(gaugingsPoints);
    plotContainer.setPlot(plot);

    plotEditor.addEditablePlot(plot);
    for (int k = 0; k < n; k++) {
      plotEditor.addEditablePlotItem("gaugings_" + k, "Gaugings - " + k, gaugingsPoints.get(k));
    }
    plotEditor.saveAsDefault(false);
    plotEditor.updateEditor();

  }

  private static record ResultPeriodAndActions(
      ResultPeriod period,
      SimpleFlowPanel panel,
      JLabel label,
      JButton createGaugingBamItemBtn) {
  }

  private static ResultPeriodAndActions buildPeriodAction(ResultPeriod period) {
    SimpleFlowPanel panel = new SimpleFlowPanel();
    panel.setGap(5);

    JLabel lbl = new JLabel(period.name());
    lbl.setIcon(SimpleColorField.buildColorIcon(period.color(), 20));

    JButton buildGaugingItemBtn = new JButton();
    buildGaugingItemBtn.setToolTipText(T.html("build_corresponding_component", T.text(BamItemType.GAUGINGS.id)));
    buildGaugingItemBtn.setIcon(BamItemType.GAUGINGS.getAddIcon());
    buildGaugingItemBtn.addActionListener(l -> {
      addGaugingBamItemFromPeriod(period);
    });
    panel.addChild(lbl, true);
    panel.addChild(buildGaugingItemBtn, false);

    return new ResultPeriodAndActions(period, panel, lbl, buildGaugingItemBtn);
  }

  private static Gaugings addGaugingBamItemFromPeriod(ResultPeriod period) {
    Gaugings item = (Gaugings) AppSetup.MAIN_FRAME.currentProject.addBamItem(BamItemType.GAUGINGS);
    item.setGaugingDataset(period.dataset(), T.text(BamItemType.RATING_SHIFT_HAPPENS.id));
    item.bamItemNameField.setText(period.name());
    return item;
  }

}
