package org.baratinage.ui.baratin.gaugings;

import javax.swing.JToggleButton;

import java.awt.Cursor;
import java.awt.Point;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotToolsPanel;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.ui.plot.PlotUtils;
import org.baratinage.ui.plot.PointHighlight;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;

public class GaugingsPlot extends SimpleFlowPanel {

  private final SimpleFlowPanel plotArea;
  private final RatingCurvePlotToolsPanel toolsPanel;
  private final DataTable table;
  private GaugingsDataset dataset;

  public final PlotEditor plotEditor;
  private final JToggleButton plotEditorToggleBtn;

  public final PlotContainer plotContainer;

  public GaugingsPlot(DataTable table) {
    super();
    setGap(5);

    this.table = table;

    toolsPanel = new RatingCurvePlotToolsPanel();
    toolsPanel.configure(true, true, false, false);
    toolsPanel.switchAxisCheckbox.addChangeListener(l -> {
      currentPlot = null; // disable keeping the axis bounds
    });
    toolsPanel.addChangeListener(l -> {
      setPlot();
    });

    plotArea = new SimpleFlowPanel(true);
    plotArea.setGap(5);

    plotEditor = new PlotEditor();
    plotEditorToggleBtn = new JToggleButton();
    plotEditorToggleBtn.setIcon(AppSetup.ICONS.EDIT);
    plotEditorToggleBtn.addActionListener(l -> {
      removeAll();
      if (plotEditorToggleBtn.isSelected()) {
        addChild(plotEditor, false);
      }
      addChild(plotArea, true);
    });

    addChild(plotArea, true);

    plotContainer = new PlotContainer();
    plotContainer.toolsPanel.addChild(plotEditorToggleBtn, false);

    T.updateHierarchy(this, plotEditor);
  }

  private void updatePlotEditor(Plot plot, PlotItem activeGaugings, PlotItem inactiveGaugings) {

    plotEditor.addEditablePlotItem(
        "inactive_gaugings",
        inactiveGaugings.getLabel(),
        inactiveGaugings);
    plotEditor.addEditablePlotItem(
        "active_gaugings",
        activeGaugings.getLabel(),
        activeGaugings);

    if (plotEditor.getEditablePlot() == null) {
      plotEditor.saveAsDefault(true); // first draw
    }

    plotEditor.addEditablePlot(plot);
    plotEditor.updateEditor();

    plot.update();
  }

  public void setGaugingsDataset(GaugingsDataset dataset) {
    this.dataset = dataset;
    setPlot();
  }

  private Plot currentPlot = null;

  public void setPlot() {
    if (dataset == null) {
      return;
    }

    PlotPoints activeGaugings = dataset.getPlotPoints(
        toolsPanel.axisFlipped() ? GaugingsDataset.PlotType.hQ : GaugingsDataset.PlotType.Qh, true);
    PlotPoints inactiveGaugings = dataset.getPlotPoints(
        toolsPanel.axisFlipped() ? GaugingsDataset.PlotType.hQ : GaugingsDataset.PlotType.Qh, false);

    PointHighlight highlight = new PointHighlight(2, 20, AppSetup.COLORS.PLOT_HIGHLIGHT);

    Plot plot = new Plot(true);

    toolsPanel.updatePlotAxis(plot);
    activeGaugings.setLabel(T.text("lgd_active_gaugings"));
    inactiveGaugings.setLabel(T.text("lgd_inactive_gaugings"));

    plot.addXYItem(highlight);
    plot.addXYItem(activeGaugings);
    plot.addXYItem(inactiveGaugings);

    plotContainer.setPlot(plot);

    ChartPanel chartPanel = plotContainer.getChartPanel();

    double[] stage = dataset.getStageValues();
    double[] discharge = dataset.getDischargeValues();

    chartPanel.addChartMouseListener(new ChartMouseListener() {
      @Override
      public void chartMouseMoved(ChartMouseEvent event) {
        updateHighlight(event.getTrigger().getPoint(), false);
      }

      @Override
      public void chartMouseClicked(ChartMouseEvent event) {
        updateHighlight(event.getTrigger().getPoint(), true);
      }

      private void updateHighlight(Point screenPoint, boolean includeTable) {
        double[] distances = PlotUtils.getDistancesFromPoint(
            plotContainer,
            toolsPanel.axisFlipped() ? discharge : stage,
            toolsPanel.axisFlipped() ? stage : discharge,
            screenPoint);
        int minIndex = -1;
        double minValue = Double.POSITIVE_INFINITY;
        for (int k = 0; k < stage.length; k++) {
          if (distances[k] < minValue) {
            minValue = distances[k];
            minIndex = k;
          }
        }
        if (minIndex >= 0 && minValue < 20) {
          if (toolsPanel.axisFlipped()) {
            highlight.setPosition(discharge[minIndex], stage[minIndex]);
          } else {
            highlight.setPosition(stage[minIndex], discharge[minIndex]);
          }
          highlight.setVisible(true);
          chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          if (includeTable) {
            table.selectRow(minIndex);
          }
          plot.update();
        } else {
          highlight.setVisible(false);
          chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          plot.update();
        }
      }
    });

    T.updateHierarchy(this, plotContainer);

    plotArea.removeAll();
    plotArea.addChild(plotContainer, true);
    plotArea.addChild(toolsPanel, 0, 5);

    if (currentPlot != null) {
      plot.setDomainZoom(currentPlot.getDomainZoom());
      plot.setRangeZoom(currentPlot.getRangeZoom());
    }
    currentPlot = plot;

    updatePlotEditor(plot, activeGaugings, inactiveGaugings);
  }

}
