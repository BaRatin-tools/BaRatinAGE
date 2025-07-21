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
import org.baratinage.ui.plot.MultiPlotContainer;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotPoints;
import org.jfree.data.Range;

public class ShiftDetectionMainPlot extends SimpleFlowPanel {

  private final List<ResultShift> shifts;
  private final List<ResultPeriod> periods;

  private ColorPalette palette;
  private final SimpleFlowPanel plotArea;
  private final SimpleFlowPanel plotPanel;
  private final RatingCurvePlotToolsPanel toolsPanel;
  private final SimpleRadioButtons<String> radioDischargeOrStage;

  public final PlotEditor plotEditor;
  private final JToggleButton plotEditorToggleBtn;

  private MultiPlotContainer mainPlot;

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
    plotEditor.saveAsDefault(true);
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
    if (mainPlot != null) {
      range = mainPlot.getCurrentDomainRange();
    }

    mainPlot = new MultiPlotContainer();

    if (range != null) {
      mainPlot.setDomainRange(range);
    }

    plotPanel.removeAll();
    boolean isDischargePlot = radioDischargeOrStage.getSelectedId().equals("q");

    if (!isDischargePlot) {
      mainPlot.addPlot(stagePlot, 3);
    } else {
      mainPlot.addPlot(dischargePlot, 3);
    }

    mainPlot.addPlot(shiftsPlot, 1);
    mainPlot.topLeftPanel.addChild(plotEditorToggleBtn, false);
    mainPlot.setDomainRange(mainPlot.getCurrentDomainRange());

    plotPanel.addChild(mainPlot, true);

    toolsPanel.updatePlotAxis(dischargePlot);

    plotEditor.addPlot("mainPlot", isDischargePlot ? dischargePlot : stagePlot);
    plotEditor.addPlot("shiftPlot", shiftsPlot);

    plotEditor.getEditablePlot("mainPlot").setYAxisLabel(
        isDischargePlot ? String.format("%s [m3/s]", T.text("discharge")) : String.format("%s [m]", T.text("stage")));

    for (int k = 0; k < shiftBars.size(); k++) {
      plotEditor.addEditablePlotItem("shift_bar_" + k, shiftBars.get(k).getLabel(),
          shiftBars.get(k));
    }
    for (int k = 0; k < shiftLines.size(); k++) {
      plotEditor.addEditablePlotItem("shift_line_" + k, shiftLines.get(k).getLabel(), shiftLines.get(k));
    }
    for (int k = 0; k < ht.size(); k++) {
      EditablePlotItem gaugingsEpi = new EditablePlotItem(ht.get(k));
      gaugingsEpi.addSibling(Qt.get(k));
      plotEditor.addEditablePlotItem("gaugings" + k, ht.get(k).getLabel(), gaugingsEpi);
    }
    plotEditor.updateEditor();

  }
}