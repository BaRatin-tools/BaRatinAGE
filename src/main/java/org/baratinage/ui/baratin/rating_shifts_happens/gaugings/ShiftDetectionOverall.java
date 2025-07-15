package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.jbam.Model;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
import org.baratinage.ui.baratin.rating_shifts_happens.BamSegmentation;
import org.baratinage.ui.commons.DensityPlotGrid;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.utils.DateTime;

public class ShiftDetectionOverall {

  public record OverallProgress(
      int doneSteps,
      int plannedSteps,
      int numberOfStepsEstimated,
      float stepProgress,
      float overallProgress) {
  }

  private final List<ShiftDetectionIteration> ratingShiftDoneWithChildren;
  private final List<ShiftDetectionIteration> ratingShiftDone;
  private ShiftDetectionIteration rootRatingShiftDetection;
  private ShiftDetectionIteration currentRatingShiftDetection = null;
  private Runnable onCancel = null;

  public ShiftDetectionOverall(
      double[] time,
      double[] stage,
      double[] stageStd,
      double[] discharge,
      double[] dischargeStd,
      Model model,
      int nSegmentMax,
      int nObsMin) {

    rootRatingShiftDetection = new ShiftDetectionIteration(
        time,
        stage, stageStd,
        discharge, dischargeStd,
        model,
        nSegmentMax, nObsMin);

    ratingShiftDone = new ArrayList<>();
    ratingShiftDoneWithChildren = new ArrayList<>();
  }

  private ShiftDetectionOverall(
      ShiftDetectionIteration rootRatingShiftDetection) {

    this.rootRatingShiftDetection = rootRatingShiftDetection;

    ratingShiftDone = new ArrayList<>();
    ratingShiftDoneWithChildren = new ArrayList<>();

  }

  public ShiftDetectionConfig save(boolean writeToFile) {
    ShiftDetectionConfig config = rootRatingShiftDetection.save(writeToFile);
    return config;
  }

  public static ShiftDetectionOverall load(ShiftDetectionConfig config) {
    ShiftDetectionIteration rootRatingShiftDetection = ShiftDetectionIteration.load(config);
    return new ShiftDetectionOverall(rootRatingShiftDetection);
  }

  public ShiftDetectionIteration getChildRatingShiftDetection(String id) {
    return rootRatingShiftDetection.getRatingShiftDetection(id);
  }

  public ShiftDetectionIteration getRootRatingShiftDetection() {
    return rootRatingShiftDetection;
  }

  private void runRecursiveRatingCurveShiftDetection(Consumer<OverallProgress> progress) {
    ratingShiftDone.clear();
    ArrayDeque<ShiftDetectionIteration> ratingShiftToDo = new ArrayDeque<>();

    ratingShiftToDo.add(rootRatingShiftDetection);

    int nMaxIterSafeguard = 100;
    int nMaxIter = estimateRemainingSteps(ratingShiftToDo, ratingShiftDone);

    int currentIter = 0;

    while (currentIter < nMaxIterSafeguard && ratingShiftToDo.size() > 0) {
      currentIter++;
      int currentIterFinal = currentIter;
      int nMaxIterFinal = nMaxIter;

      currentRatingShiftDetection = ratingShiftToDo.pop();
      currentRatingShiftDetection.runShiftDetection((p) -> {
        progress.accept(
            new OverallProgress(
                ratingShiftDone.size(),
                ratingShiftToDo.size() + 1,
                nMaxIterFinal,
                p,
                computeProgress(currentIterFinal, nMaxIterFinal, p)));
      });
      List<ShiftDetectionIteration> children = currentRatingShiftDetection.getChildren();
      ratingShiftToDo.addAll(children);
      ratingShiftDone.add(currentRatingShiftDetection);
      ratingShiftDoneWithChildren.add(currentRatingShiftDetection);
      ratingShiftDoneWithChildren.addAll(children);
      currentRatingShiftDetection = null;

      int nMaxIterNew = estimateRemainingSteps(ratingShiftToDo, ratingShiftDone);
      nMaxIter = nMaxIterNew;

      progress.accept(
          new OverallProgress(
              ratingShiftDone.size(),
              ratingShiftToDo.size(),
              nMaxIter,
              1f,
              computeProgress(currentIter, nMaxIter, 1f)));
    }

  }

  public void cancel() {
    if (currentRatingShiftDetection != null) {
      currentRatingShiftDetection.cancel();
      if (onCancel != null) {
        onCancel.run();
      }
    }

  }

  public void runShiftDetection(
      Consumer<OverallProgress> progress,
      Runnable onDone,
      Runnable onCancel) {
    SwingWorker<Void, String> worker = new SwingWorker<>() {

      @Override
      protected Void doInBackground() throws Exception {
        runRecursiveRatingCurveShiftDetection(progress);
        return null;
      }

      @Override
      protected void process(List<String> progress) {

      }

      @Override
      protected void done() {
        onDone.run();
      }

    };

    worker.execute();
  }

  private static EstimatedParameterWrapper buildTauParameters(EstimatedParameter parameter, int mainIndex,
      int secIndex) {
    EstimatedParameter p = new EstimatedParameter(
        parameter.name,
        DateTime.dateTimeDoubleToSecondsDouble(parameter.mcmc),
        parameter.summary,
        parameter.maxpostIndex,
        parameter.parameterConfig);
    EstimatedParameterWrapper epw = new EstimatedParameterWrapper(
        p,
        String.format("Tau_%d_%d", mainIndex, secIndex),
        String.format("<html>&tau;<sub>%d, %d</sub></html>", mainIndex, secIndex),
        EstimatedParameterWrapper.TYPE.MODEL);
    return epw;
  }

  public SimpleFlowPanel getShiftsDensitiesPlots() {

    DensityPlotGrid densityPlotGrid = new DensityPlotGrid();
    densityPlotGrid.isTimeSeries = true;
    List<BamSegmentation> segmentations = rootRatingShiftDetection.getAllBestSegmentation();

    int i = 0;
    for (BamSegmentation se : segmentations) {
      List<EstimatedParameter> parameters = se.getTauParameters();
      i++;
      int j = 0;
      for (EstimatedParameter p : parameters) {
        j++;
        EstimatedParameterWrapper epw = buildTauParameters(p, i, j);
        densityPlotGrid.addPlot(epw);
      }
    }
    densityPlotGrid.updatePlots();
    return densityPlotGrid;
  }

  public PlotContainer getShiftsPlot() {

    List<BamSegmentation> segmentations = rootRatingShiftDetection.getAllBestSegmentation();

    List<double[]> allData = new ArrayList<>();
    for (BamSegmentation se : segmentations) {

      List<EstimatedParameter> tauParameters = se.getTauParameters();

      for (EstimatedParameter ep : tauParameters) {
        allData.add(ep.mcmc);
      }
    }

    double optimalBinWidth = PlotBar.computeOptimalBinwidth(allData, 30);

    PlotContainer container = new PlotContainer();

    Plot plot = new Plot(true, true);

    int k = 0;
    for (BamSegmentation se : segmentations) {

      List<EstimatedParameter> tauParameters = se.getTauParameters();

      for (EstimatedParameter ep : tauParameters) {

        // double[][] density = densityEstimate(ep.mcmc, 30);
        double[][] density = PlotBar.densityEstimate(ep.mcmc, optimalBinWidth);

        PlotLine line = new PlotLine("",
            DateTime.dateTimeDoubleToSecondsDouble(density[0]),
            density[1],
            getColor(k));
        line.setSplineRenderer(30);

        PlotInfiniteLine vLine = new PlotInfiniteLine("", ep.getMaxpost() * 1000, getColor(k), 4);

        PlotBar bar = new PlotBar(
            "",
            DateTime.dateTimeDoubleToSecondsDouble(density[0]),
            density[1],
            getColor(k),
            0.7f);

        bar.setLegendVisible(false);
        vLine.setLegendVisible(false);
        plot.addXYItem(bar);
        plot.addXYItem(vLine);
        k++;
      }
    }

    container.setPlot(plot);
    return container;
  }

  private static final Color[] COLORS = {
      Color.decode("#E69F00"), Color.decode("#56B4E9"),
      Color.decode("#009E73"), Color.decode("#F0E442"),
      Color.decode("#0072B2"), Color.decode("#D55E00"),
      Color.decode("#CC79A7"), Color.decode("#999999"),
      Color.decode("#000000"), Color.decode("#FFFFFF")
  };

  public static Color getColor(int index) {
    int safeIndex = Math.floorMod(index, COLORS.length);
    return COLORS[safeIndex];
  }

  public static float computeProgress(int currentStep, int totalSteps, float stepProgress) {
    float stepPart = 1f / (float) totalSteps;
    return stepPart * ((float) currentStep - 1f) + stepPart * stepProgress;
  }

  private static int estimateRemainingSteps(
      ArrayDeque<ShiftDetectionIteration> todo,
      List<ShiftDetectionIteration> done) {
    double s = 0;
    for (ShiftDetectionIteration rsd : todo) {
      double x = (double) rsd.ratingCurveEstimation.time.length / (double) rsd.nSegmentMax;
      s += x;
      s -= 1;
    }
    return ((int) Math.ceil(s)) + done.size();
  }

}
