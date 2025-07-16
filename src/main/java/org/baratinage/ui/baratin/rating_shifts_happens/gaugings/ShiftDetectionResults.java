package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.rating_shifts_happens.BamSegmentation;
import org.baratinage.ui.plot.ColorPalette;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.DateTime;

public class ShiftDetectionResults {

  private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final List<ResultPeriod> periods;
  private final List<ResultShift> shifts;

  public final ShiftDetectionMainPlot mainPlot;
  public final ShiftDetectionTable table;
  public final ShiftDetectionGaugings gaugings;
  public final ShiftDetectionIntermediateResults intermediateResults;

  public ShiftDetectionResults(
      ShiftDetectionIteration rcShiftsDetectionRun,
      boolean local) {

    // **************************************************************
    // get all shifts in the proper order
    List<BamSegmentation> segs = new ArrayList<>();
    if (local) {
      segs.add(rcShiftsDetectionRun.getBestSegmentation());
    } else {
      segs = new ArrayList<>();
      segs = rcShiftsDetectionRun.getAllBestSegmentation();
    }
    List<EstimatedParameter> shiftsUnordered = new ArrayList<>();
    for (BamSegmentation seg : segs) {
      for (EstimatedParameter p : seg.getTauParameters()) {
        shiftsUnordered.add(p);
      }
    }
    List<EstimatedParameter> shiftsOrdered = shiftsUnordered.stream().sorted((p1, p2) -> {
      return p1.getMaxpost() > p2.getMaxpost() ? 1 : -1;
    }).toList();

    double optimBinWidth = PlotBar.computeOptimalBinwidth(
        shiftsOrdered.stream().map(p -> p.mcmc).toList(), 50);

    Color[] palette = ColorPalette.getColors(shiftsOrdered.size() + 1, ColorPalette.VIRIDIS);

    shifts = new ArrayList<>();

    for (int k = 0; k < shiftsOrdered.size(); k++) {
      EstimatedParameter p = shiftsOrdered.get(k);
      double maxpost = p.getMaxpost();
      LocalDateTime date = DateTime.doubleToDateTime(maxpost);
      String dateStr = dateFormatter.format(date);
      PlotInfiniteLine line = new PlotInfiniteLine(
          dateStr,
          maxpost * 1000,
          palette[k],
          4);
      double[][] densities = PlotBar.densityEstimate(p.mcmc, optimBinWidth);
      PlotBar dist = new PlotBar(
          dateStr,
          DateTime.dateTimeDoubleToSecondsDouble(densities[0]),
          densities[1],
          palette[k],
          0.7f);

      shifts.add(new ResultShift(
          date,
          p,
          dateStr,
          palette[k],
          line,
          dist));
    }

    // **************************************************************
    // build the gauging dataset associated with each period
    double[] time = rcShiftsDetectionRun.ratingCurveEstimation.time;
    double[] stage = rcShiftsDetectionRun.ratingCurveEstimation.stage;
    double[] discharge = rcShiftsDetectionRun.ratingCurveEstimation.discharge;
    double[] dischargeStd = rcShiftsDetectionRun.ratingCurveEstimation.dischargeStd;

    periods = new ArrayList<>();

    ResultShift startShift = null;
    ResultShift endShift = null;
    for (int k = 0; k < shifts.size(); k++) {
      endShift = shifts.get(k);
      GaugingsDataset dataset = buildGaugingDataset(
          time, stage, discharge, dischargeStd,
          startShift == null ? - Double.MAX_VALUE : startShift.parameter().getMaxpost(),
          endShift.parameter().getMaxpost());
      Color color = palette[k];
      periods.add(buildResultPeriod(dataset, startShift, endShift, color));
      startShift = endShift;
    }
    GaugingsDataset dataset = buildGaugingDataset(
        time, stage, discharge, dischargeStd,
        startShift == null ?  - Double.MAX_VALUE : startShift.parameter().getMaxpost(),
        Double.MAX_VALUE);
    Color color = palette[shifts.size()];
    periods.add(buildResultPeriod(dataset, startShift, null, color));

    // **************************************************************
    // build the various results panels
    mainPlot = new ShiftDetectionMainPlot(shifts, periods);
    table = new ShiftDetectionTable(shifts);
    gaugings = new ShiftDetectionGaugings(periods);
    intermediateResults = new ShiftDetectionIntermediateResults(rcShiftsDetectionRun);
  }

  public void setPalette(ColorPalette palette) {
    mainPlot.setPalette(palette);
    gaugings.setPalette(palette);
    intermediateResults.setPalette(palette);
  }

  public void updateResults() {
    mainPlot.updatePlot();
    gaugings.updatePlot();
    intermediateResults.updatePlots();
  }

  private static ResultPeriod buildResultPeriod(
      GaugingsDataset dataset,
      ResultShift startShift,
      ResultShift endShift,
      Color color) {
    PlotPoints Qh = dataset.getPlotPoints(GaugingsDataset.PlotType.Qh);
    PlotPoints hQ = dataset.getPlotPoints(GaugingsDataset.PlotType.hQ);
    PlotPoints Qt = dataset.getPlotPoints(GaugingsDataset.PlotType.Qt);
    PlotPoints ht = dataset.getPlotPoints(GaugingsDataset.PlotType.ht);
    Qh.setLabel(dataset.getName());
    hQ.setLabel(dataset.getName());
    Qt.setLabel(dataset.getName());
    ht.setLabel(dataset.getName());
    Qh.setPaint(color);
    hQ.setPaint(color);
    Qt.setPaint(color);
    ht.setPaint(color);
    return new ResultPeriod(
        startShift,
        endShift,
        dataset,
        dataset.getName(),
        color,
        Qh,
        hQ,
        Qt,
        ht);
  }

  private static GaugingsDataset buildGaugingDataset(
      double[] time,
      double[] stage,
      double[] discharge,
      double[] dischargeStd,
      double startTime,
      double endTime) {

    int startIndex = 0;
    int endIndex = 0;
    for (int k = 0; k < time.length; k++) {
      if (time[k] >= startTime) {
        startIndex = k;
        break;
      }
    }
    boolean noEnd = true;
    for (int k = startIndex; k < time.length; k++) {
      if (time[k] >= endTime) {
        endIndex = k;
        noEnd = false;
        break;
      }
    }
    if (noEnd) {
      endIndex = time.length;
    }

    int n = endIndex - startIndex;
    LocalDateTime[] t = new LocalDateTime[n];
    double[] h = new double[n];
    double[] q = new double[n];
    double[] qupercent = new double[n];
    boolean[] active = new boolean[n];
    for (int k = startIndex; k < endIndex; k++) {
      t[k - startIndex] = DateTime.doubleToDateTime(time[k]);
      h[k - startIndex] = stage[k];
      q[k - startIndex] = discharge[k];
      qupercent[k - startIndex] = dischargeStd[k] / discharge[k] * 100 * 2;
      active[k - startIndex] = true;
    }

    String name = "";

    String endTimeString = endTime == Double.MAX_VALUE
        ? dateFormatter.format(DateTime.doubleToDateTime(time[time.length - 1]))
        : dateFormatter.format(DateTime.doubleToDateTime(endTime));
    String startTimeString = startTime == - Double.MAX_VALUE
        ? dateFormatter.format(DateTime.doubleToDateTime(time[0]))
        : dateFormatter.format(DateTime.doubleToDateTime(startTime));
    if ((startTime == - Double.MAX_VALUE && endTime == Double.MAX_VALUE) || (startTime !=  - Double.MAX_VALUE  && endTime != Double.MAX_VALUE)) {
      name = String.format("%s - %s", startTimeString, endTimeString);
    } else if (startTime == - Double.MAX_VALUE) {
      name = String.format("< %s", dateFormatter.format(DateTime.doubleToDateTime(endTime)));
    } else if (endTime == Double.MAX_VALUE) {
      name = String.format("> %s", dateFormatter.format(DateTime.doubleToDateTime(startTime)));
    }

    return GaugingsDataset.buildGaugingsDataset(
        name,
        h,
        q,
        qupercent,
        active,
        t,
        null);
  }

  public static record ResultPeriod(
      ResultShift start,
      ResultShift end,
      GaugingsDataset dataset,
      String name,
      Color color,
      PlotPoints QhPoints,
      PlotPoints hQPoints,
      PlotPoints QtPoints,
      PlotPoints htPoints) {
  }

  public static record ResultShift(
      LocalDateTime date,
      EstimatedParameter parameter,
      String name,
      Color color,
      PlotInfiniteLine line,
      PlotBar distribution) {

  }

}
