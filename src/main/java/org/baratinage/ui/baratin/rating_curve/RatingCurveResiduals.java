package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JToggleButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.CalibrationDataResiduals;
import org.baratinage.jbam.CalibrationDataResiduals.InputDataResiduals;
import org.baratinage.jbam.CalibrationDataResiduals.OutputDataResiduals;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.EditablePlot;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.Arr;
import org.baratinage.utils.DateTime;

public class RatingCurveResiduals extends SimpleFlowPanel {

  public final PlotContainer plotContainer;
  public final PlotEditor plotEditor;
  private final JToggleButton plotEditorToggleBtn;
  private final SimpleComboBox xAxisCombobox;

  private boolean gaugingsHaveTime = false;

  private static record PlotRecord(Plot plot, PlotPoints points) {
  };

  private final HashMap<String, PlotRecord> plots = new HashMap<>();

  public RatingCurveResiduals() {
    super();

    plotContainer = new PlotContainer();
    plotEditor = new PlotEditor();

    plotEditor.setVisible(false);
    plotEditorToggleBtn = new JToggleButton();
    plotEditorToggleBtn.setIcon(AppSetup.ICONS.EDIT);
    plotEditorToggleBtn.addActionListener(l -> {
      plotEditor.setVisible(plotEditorToggleBtn.isSelected());
    });
    plotContainer.toolsPanel.add(plotEditorToggleBtn, false);

    xAxisCombobox = new SimpleComboBox();
    xAxisCombobox.setEnabled(false);
    xAxisCombobox.setEmptyItem(null);
    xAxisCombobox.addChangeListener(l -> {
      int selectedIndex = xAxisCombobox.getSelectedIndex();
      PlotRecord pr = switch (selectedIndex) {
        case 0 -> plots.get("index");
        case 1 -> gaugingsHaveTime ? plots.get("time") : plots.get("obsQ");
        case 2 -> gaugingsHaveTime ? plots.get("obsQ") : plots.get("simQ");
        case 3 -> gaugingsHaveTime ? plots.get("simQ") : plots.get("stage");
        case 4 -> plots.get("stage");
        default -> plots.get("simQ");
      };
      setPlot(pr);
    });
    JLabel xAxisLabel = new JLabel();

    T.t(this, () -> {
      xAxisLabel.setText(T.text("x_axis"));
      setCombobox(gaugingsHaveTime);
    });

    SimpleFlowPanel bottomToolbar = new SimpleFlowPanel();
    bottomToolbar.setGap(5);
    bottomToolbar.addChild(xAxisLabel, false);
    bottomToolbar.addChild(xAxisCombobox, false);

    SimpleFlowPanel rightPanel = new SimpleFlowPanel(true);
    rightPanel.setPadding(0, 0, 5, 0);
    rightPanel.setGap(5);
    rightPanel.addChild(plotContainer, true);
    rightPanel.addChild(bottomToolbar, false);
    addChild(plotEditor, false);
    addChild(rightPanel, true);
  }

  private boolean isFirstDraw = true;

  public void updateResults(CalibrationDataResiduals residuals, double[] gaugingsDateTime) {
    xAxisCombobox.setEnabled(true);

    gaugingsHaveTime = gaugingsDateTime != null;

    Plot plot = new Plot();

    if (residuals.outputResiduals.size() <= 0 || residuals.inputResiduals.size() <= 0) {
      plotContainer.setPlot(plot);
      return;
    }
    OutputDataResiduals res = residuals.outputResiduals.get(0);
    if (res == null) {
      plotContainer.setPlot(plot);
      return;
    }
    InputDataResiduals inRes = residuals.inputResiduals.get(0);
    if (inRes == null) {
      plotContainer.setPlot(plot);
      return;
    }
    double[] stage = inRes.obsValues();

    PlotInfiniteLine zeroLine = new PlotInfiniteLine("zero", 0, 0);

    PlotPoints indexPoints = new PlotPoints(
        "residuals", Arr.makeArray(1, stage.length, stage.length), res.resValues(),
        AppSetup.COLORS.GAUGING);
    Plot indexPlot = new Plot(true);
    indexPlot.addXYItem(zeroLine);
    indexPlot.addXYItem(indexPoints);

    Plot timePlot = null;
    PlotPoints datetimePoints = null;
    if (gaugingsHaveTime) {
      double[] dt = DateTime.dateTimeDoubleToSecondsDouble(gaugingsDateTime);
      datetimePoints = new PlotPoints(
          "residuals", dt, res.resValues(),
          AppSetup.COLORS.GAUGING);
      timePlot = new Plot(true, true);
      timePlot.addXYItem(zeroLine);
      timePlot.addXYItem(datetimePoints);
    }

    PlotPoints simDischargePoints = new PlotPoints(
        "residuals", res.simValues(), res.resValues(),
        AppSetup.COLORS.GAUGING);
    Plot simQPlot = new Plot();
    simQPlot.addXYItem(zeroLine);
    simQPlot.addXYItem(simDischargePoints);

    PlotPoints obsDischargePoints = new PlotPoints(
        "residuals", res.obsValues(), res.resValues(),
        AppSetup.COLORS.GAUGING);
    Plot obsQPlot = new Plot();
    obsQPlot.addXYItem(zeroLine);
    obsQPlot.addXYItem(obsDischargePoints);

    PlotPoints stagePoints = new PlotPoints(
        "residuals", stage, res.resValues(),
        AppSetup.COLORS.GAUGING);
    Plot stagePlot = new Plot();
    stagePlot.addXYItem(zeroLine);
    stagePlot.addXYItem(stagePoints);

    EditablePlot epiIndex = plotEditor.addEditablePlot("index", indexPlot);
    EditablePlot epiTime = null;
    if (gaugingsHaveTime) {
      epiTime = plotEditor.addEditablePlot("time", timePlot);
    }
    EditablePlot epiSimQ = plotEditor.addEditablePlot("simQ", simQPlot);
    EditablePlot epiObsQ = plotEditor.addEditablePlot("obsQ", obsQPlot);
    EditablePlot epiStage = plotEditor.addEditablePlot("stage", stagePlot);

    epiIndex.setPlotName(T.text("index"));
    if (epiTime != null) {
      epiTime.setPlotName(T.text("date_time"));
    }
    epiSimQ.setPlotName(T.text("sim_discharge"));
    epiObsQ.setPlotName(T.text("obs_discharge"));
    epiStage.setPlotName(T.text("stage"));

    plotEditor.updateEditor();

    plots.put("index", new PlotRecord(indexPlot, indexPoints));
    plots.put("time", new PlotRecord(timePlot, datetimePoints));
    plots.put("simQ", new PlotRecord(simQPlot, simDischargePoints));
    plots.put("obsQ", new PlotRecord(obsQPlot, obsDischargePoints));
    plots.put("stage", new PlotRecord(stagePlot, stagePoints));

    setCombobox(gaugingsHaveTime);

    if (isFirstDraw) {
      isFirstDraw = false;
      epiIndex.setShowLegend(false);
      epiIndex.setXAxisLabel(T.text("index"));
      epiIndex.setYAxisLabel(T.text("residuals"));
      if (gaugingsHaveTime) {
        epiTime.setShowLegend(false);
        epiTime.setXAxisLabel(T.text("date_time"));
        epiTime.setYAxisLabel(T.text("residuals"));
      }
      epiSimQ.setShowLegend(false);
      epiSimQ.setXAxisLabel(T.text("sim_discharge") + "[m3/s]");
      epiSimQ.setYAxisLabel(T.text("residuals"));
      epiObsQ.setShowLegend(false);
      epiObsQ.setXAxisLabel(T.text("obs_discharge") + "[m3/s]");
      epiObsQ.setYAxisLabel(T.text("residuals"));
      epiStage.setShowLegend(false);
      epiStage.setXAxisLabel(T.text("stage") + "[m]");
      epiStage.setYAxisLabel(T.text("residuals"));
    }

    setPlot(plots.get("obsQ"));
  }

  private void setPlot(PlotRecord pr) {
    EditablePlotItem epi = plotEditor.getEditablePlotItem("residuals");
    String epiLabel = epi != null ? epi.getLabel() : T.text("residuals");
    plotContainer.setPlot(pr.plot);
    epi = plotEditor.addEditablePlotItem("residuals", epiLabel, pr.points);
    epi.setLabel(epiLabel);
    plotEditor.updateEditor();
  }

  private void setCombobox(boolean withTime) {
    List<JLabel> xAxisOptions = new ArrayList<>();
    xAxisOptions.add(new JLabel(T.text("index")));
    if (withTime) {
      xAxisOptions.add(new JLabel(T.text("date_time")));
    }
    xAxisOptions.add(new JLabel(T.text("obs_discharge")));
    xAxisOptions.add(new JLabel(T.text("sim_discharge")));
    xAxisOptions.add(new JLabel(T.text("stage")));
    int selectedIndex = xAxisCombobox.getSelectedIndex();
    xAxisCombobox.setItems(xAxisOptions.toArray(new JLabel[xAxisOptions.size()]), true);
    if (selectedIndex == xAxisOptions.size() || selectedIndex < 0) {
      selectedIndex = xAxisOptions.size() - 2; // sim_discharge
    }
    xAxisCombobox.setSelectedItem(selectedIndex, true);
  }

}
