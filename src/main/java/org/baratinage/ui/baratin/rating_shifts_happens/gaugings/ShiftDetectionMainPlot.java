package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.awt.Color;
import java.util.List;

import javax.swing.JRadioButton;

import org.baratinage.translation.T;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotToolsPanel;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults.ResultPeriod;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults.ResultShift;
import org.baratinage.ui.component.SimpleRadioButtons;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.ColorPalette;
import org.baratinage.ui.plot.MultiPlotContainer;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotPoints;
import org.jfree.data.Range;

public class ShiftDetectionMainPlot extends SimpleFlowPanel {

  private final List<ResultShift> shifts;
  private final List<ResultPeriod> periods;

  private ColorPalette palette;
  private final SimpleFlowPanel plotPanel;
  private final RatingCurvePlotToolsPanel toolsPanel;
  private final SimpleRadioButtons<String> radioDischargeOrStage;

  private MultiPlotContainer mainStagePlot;
  private MultiPlotContainer mainDischargePlot;

  public ShiftDetectionMainPlot(
      List<ResultShift> shifts,
      List<ResultPeriod> periods) {
    super(true);

    this.palette = ColorPalette.VIRIDIS;
    this.shifts = shifts;
    this.periods = periods;

    plotPanel = new SimpleFlowPanel();

    toolsPanel = new RatingCurvePlotToolsPanel();
    toolsPanel.configure(true, false, false);

    radioDischargeOrStage = new SimpleRadioButtons<>();
    JRadioButton stageBtn = radioDischargeOrStage.addOption("h", T.text("stage"), "h");
    JRadioButton dischargeBtn = radioDischargeOrStage.addOption("q", T.text("discharge"), "q");

    toolsPanel.addChild(stageBtn, false);
    toolsPanel.addChild(dischargeBtn, false);

    radioDischargeOrStage.addChangeListener(l -> {
      boolean isDischargePlot = radioDischargeOrStage.getSelectedId().equals("q");
      toolsPanel.logScaleDischargeAxis.setEnabled(isDischargePlot);
      updatePlot();
    });

    toolsPanel.addChangeListener(l -> {
      updatePlot();
    });

    radioDischargeOrStage.setSelected("h");
    toolsPanel.logScaleDischargeAxis.setEnabled(false);

    addChild(plotPanel, true);
    addChild(toolsPanel, false);

    stageBtn.setText(T.text("stage"));
    dischargeBtn.setText(T.text("discharge"));

    updatePlot();
  }

  public void setPalette(ColorPalette palette) {
    this.palette = palette;
  }

  public void updatePlot() {

    List<PlotBar> shiftBars = shifts.stream().map(s -> s.distribution()).toList();
    List<PlotInfiniteLine> shiftLines = shifts.stream().map(s -> s.line()).toList();
    List<PlotPoints> ht = periods.stream().map(p -> p.htPoints()).toList();
    List<PlotPoints> Qt = periods.stream().map(p -> p.QtPoints()).toList();

    int n = shiftBars.size() + 1;
    Color[] colors = palette.getColors(n);
    for (int k = 0; k < n - 1; k++) {
      shiftBars.get(k).setPaint(colors[k]);
      shiftLines.get(k).setPaint(colors[k]);
      ht.get(k).setPaint(colors[k]);
      Qt.get(k).setPaint(colors[k]);
    }
    ht.get(n - 1).setPaint(colors[n - 1]);
    Qt.get(n - 1).setPaint(colors[n - 1]);

    Plot shiftsPlot = new Plot(false, true);
    shiftsPlot.addXYItems(shiftBars);
    shiftsPlot.addXYItems(shiftLines);
    shiftsPlot.plot.getRangeAxis().setVisible(false);

    Plot stagePlot = new Plot(true, true);
    stagePlot.addXYItems(ht);
    stagePlot.addXYItems(shiftLines);

    Plot dischargePlot = new Plot(true, true);
    dischargePlot.addXYItems(Qt);
    dischargePlot.addXYItems(shiftLines);

    Range range = null;
    if (mainStagePlot != null) {
      range = mainDischargePlot.getCurrentDomainRange();
    }

    mainStagePlot = new MultiPlotContainer();
    mainStagePlot.addPlot(stagePlot, 3);
    if (shiftBars.size() > 0) {
      mainStagePlot.addPlot(shiftsPlot, 1);
    }

    mainDischargePlot = new MultiPlotContainer();
    mainDischargePlot.addPlot(dischargePlot, 3);
    if (shiftBars.size() > 0) {
      mainDischargePlot.addPlot(shiftsPlot, 1);
    }

    if (range != null) {
      mainStagePlot.setDomainRange(range);
      mainDischargePlot.setDomainRange(range);
    }

    plotPanel.removeAll();
    boolean isDischargePlot = radioDischargeOrStage.getSelectedId().equals("q");

    // String id = radioDischargeOrStage.getSelectedId();
    if (!isDischargePlot) {
      plotPanel.addChild(mainStagePlot, true);
      mainStagePlot.setDomainRange(mainDischargePlot.getCurrentDomainRange());
    } else {
      plotPanel.addChild(mainDischargePlot, true);
      mainDischargePlot.setDomainRange(mainStagePlot.getCurrentDomainRange());
    }

    stagePlot.setYAxisLabel(String.format("%s [m]", T.text("stage")));
    dischargePlot.setYAxisLabel(String.format("%s [m3/s]", T.text("discharge")));

    toolsPanel.updatePlotAxis(dischargePlot);
  }
}