package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.awt.Color;
import java.util.List;

import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotToolsPanel;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults.ResultPeriod;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults.ResultShift;
import org.baratinage.ui.component.SimpleRadioButtons;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.ColorPalette;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.ui.plot.PlotInfiniteBand;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.ui.plot.StackedPlot;

public class ShiftDetectionMainPlot extends SimpleFlowPanel {

  private final List<ResultShift> shifts;
  private final List<ResultPeriod> periods;

  private ColorPalette palette;
  private final SimpleFlowPanel plotArea;
  private final SimpleFlowPanel plotPanel;
  private final RatingCurvePlotToolsPanel toolsPanel;
  public final SimpleRadioButtons<String> radioDischargeOrStage;

  public final PlotEditor plotEditor;
  private final JToggleButton plotEditorToggleBtn;

  public PlotContainer mainPlot;

  public ShiftDetectionMainPlot(
      List<ResultShift> shifts,
      List<ResultPeriod> periods) {
    super(false);

    this.palette = ColorPalette.VIRIDIS;
    this.shifts = shifts;
    this.periods = periods;

    plotArea = new SimpleFlowPanel(true);

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

    plotPanel = new SimpleFlowPanel();

    toolsPanel = new RatingCurvePlotToolsPanel();
    toolsPanel.configure(true, false, false, false);

    radioDischargeOrStage = new SimpleRadioButtons<>();
    JRadioButton stageBtn = radioDischargeOrStage.addOption("h", T.text("stage"), "h");
    JRadioButton dischargeBtn = radioDischargeOrStage.addOption("q", T.text("discharge"), "q");

    T.t(this, () -> {
      stageBtn.setText(T.text("stage"));
      dischargeBtn.setText(T.text("discharge"));
    });

    toolsPanel.add(stageBtn);
    toolsPanel.add(dischargeBtn);

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

    plotArea.addChild(plotPanel, true);
    plotArea.addChild(toolsPanel, false);
    addChild(plotArea, true);

    stageBtn.setText(T.text("stage"));
    dischargeBtn.setText(T.text("discharge"));

    updatePlot();
  }

  public void setPalette(ColorPalette palette) {
    this.palette = palette;
    plotEditor.reset();
    updatePlot();
  }

  public void updatePlot() {

    List<PlotBar> shiftBars = shifts.stream().map(s -> s.distribution()).toList();
    List<PlotInfiniteLine> shiftLines = shifts.stream().map(s -> s.line()).toList();
    List<PlotInfiniteBand> shiftBands = shifts.stream().map(s -> s.band()).toList();
    List<PlotPoints> ht = periods.stream().map(p -> p.htPoints()).toList();
    List<PlotPoints> Qt = periods.stream().map(p -> p.QtPoints()).toList();

    int n = shiftBars.size() + 1;
    Color[] colors = palette.getColors(n);
    for (int k = 0; k < n - 1; k++) {
      shiftBars.get(k).setPaint(colors[k]);
      shiftLines.get(k).setPaint(colors[k]);
      shiftBands.get(k).setFillPaint(colors[k]);
      shiftBands.get(k).setAlpha(0.5f);
      ht.get(k).setPaint(colors[k]);
      Qt.get(k).setPaint(colors[k]);
    }
    ht.get(n - 1).setPaint(colors[n - 1]);
    Qt.get(n - 1).setPaint(colors[n - 1]);

    Plot dischargePlot = new Plot(true, true);
    dischargePlot.addXYItems(shiftBands);
    dischargePlot.addXYItems(shiftLines);
    dischargePlot.addXYItems(Qt);

    Plot shiftsPlot = new Plot(true, true);
    shiftsPlot.addXYItems(shiftBars);
    shiftsPlot.addXYItems(shiftLines);
    shiftsPlot.addXYItems(shiftBands);
    shiftsPlot.plot.getRangeAxis().setVisible(false);

    Plot stagePlot = new Plot(true, true);
    stagePlot.addXYItems(ht);
    stagePlot.addXYItems(shiftLines);
    stagePlot.addXYItems(shiftBands);

    mainPlot = new PlotContainer();
    StackedPlot stackedPlot = null;

    plotPanel.removeAll();
    boolean isDischargePlot = radioDischargeOrStage.getSelectedId().equals("q");

    if (!isDischargePlot) {
      stackedPlot = new StackedPlot(stagePlot, 3);
    } else {
      stackedPlot = new StackedPlot(dischargePlot, 3);
    }

    stackedPlot.addSubplot(shiftsPlot, 1);
    // mainPlot.toolsPanel.addChild(plotEditorToggleBtn, false);
    mainPlot.toolsPanel.add(plotEditorToggleBtn);
    mainPlot.setPlot(stackedPlot);

    plotPanel.addChild(mainPlot, true);

    toolsPanel.updatePlotAxis(dischargePlot, plotEditor.getEditablePlot("mainPlot"));

    plotEditor.addEditablePlot("mainPlot", isDischargePlot ? dischargePlot : stagePlot);
    plotEditor.addEditablePlot("shiftPlot", shiftsPlot);

    plotEditor.getEditablePlot("mainPlot").setYAxisLabel(
        isDischargePlot
            ? "%s [m3/s]".formatted(T.text("discharge"))
            : "%s [m]".formatted(T.text("stage")));

    for (int k = 0; k < shiftBars.size(); k++) {
      EditablePlotItem epi = plotEditor.addEditablePlotItem("shift_bar_" + k,
          shiftBars.get(k).getLabel(),
          shiftBars.get(k));
      epi.setShowLegend(false);
    }
    for (int k = 0; k < shiftLines.size(); k++) {
      EditablePlotItem epi = plotEditor.addEditablePlotItem("shift_line_" + k,
          shiftLines.get(k).getLabel(),
          shiftLines.get(k));
      epi.setShowLegend(false);
    }
    for (int k = 0; k < shiftBands.size(); k++) {
      EditablePlotItem epi = plotEditor.addEditablePlotItem("shift_band_" + k,
          shiftBands.get(k).getLabel(),
          shiftBands.get(k));
      epi.setShowLegend(true);
    }
    for (int k = 0; k < ht.size(); k++) {
      EditablePlotItem gaugingsEpi = new EditablePlotItem(ht.get(k));
      gaugingsEpi.addSibling(Qt.get(k));
      gaugingsEpi.setShowLegend(false);
      EditablePlotItem epi = plotEditor.addEditablePlotItem("gaugings" + k,
          ht.get(k).getLabel(),
          gaugingsEpi);
      epi.setShowLegend(false);
    }
    plotEditor.updateEditor();
    stackedPlot.updatePlots();

  }
}