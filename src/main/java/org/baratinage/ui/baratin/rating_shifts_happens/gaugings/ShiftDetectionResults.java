package org.baratinage.ui.baratin.rating_shifts_happens.gaugings;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.baratin.Gaugings;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.rating_shifts_happens.BamSegmentation;
import org.baratinage.ui.commons.ColumnHeaderDescription;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.component.SimpleColorField;
import org.baratinage.ui.component.SimpleRadioButtons;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.plot.MultiPlotContainer;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBar;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotInfiniteLine;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.DateTime;

public class ShiftDetectionResults {

  private static final Color[] COLORS = {
      new Color(230, 159, 0), // #E69F00
      new Color(86, 180, 233), // #56B4E9
      new Color(0, 158, 115), // #009E73
      new Color(240, 228, 66), // #F0E442
      new Color(0, 114, 178), // #0072B2
      new Color(213, 94, 0), // #D55E00
      new Color(204, 121, 167), // #CC79A7
      new Color(153, 153, 153), // #999999
      new Color(0, 0, 0), // #000000
      new Color(228, 26, 28), // #E41A1C
      new Color(55, 126, 184), // #377EB8
      new Color(77, 175, 74), // #4DAF4A
      new Color(122, 47, 133), // #7a2f85
  };

  public static Color getColor(int index) {
    int safeIndex = Math.floorMod(index, COLORS.length);
    return COLORS[safeIndex];
  }

  private static ColumnHeaderDescription buildColumnDescs() {

    ColumnHeaderDescription headerDesc = new ColumnHeaderDescription();
    headerDesc.addColumnDesc("Name", () -> {
      return T.text("name");
    });

    headerDesc.addColumnDesc("Posterior_maxpost", () -> {
      return T.text("high_bound_parameter", T.text("maxpost_desc"));
    });
    headerDesc.addColumnDesc("Posterior_low", () -> {
      return T.text("low_bound_parameter", T.text("posterior_density"));
    });
    headerDesc.addColumnDesc("Posterior_high", () -> {
      return T.text("high_bound_parameter", T.text("posterior_density"));
    });

    return headerDesc;
  }

  private static final ColumnHeaderDescription resultsColumnsDescritption = buildColumnDescs();

  private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final ShiftDetectionIteration rcShiftsDetectionRun;

  private final List<ResultPeriod> periods;
  private final List<ResultShift> shifts;

  public ShiftDetectionResults(ShiftDetectionIteration rcShiftsDetectionRun, boolean local) {
    this.rcShiftsDetectionRun = rcShiftsDetectionRun;

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

    shifts = new ArrayList<>();

    for (int k = 0; k < shiftsOrdered.size(); k++) {
      EstimatedParameter p = shiftsOrdered.get(k);
      double maxpost = p.getMaxpost();
      LocalDateTime date = DateTime.doubleToDateTime(maxpost);
      String dateStr = dateFormatter.format(date);
      PlotInfiniteLine line = new PlotInfiniteLine(
          dateStr,
          maxpost * 1000,
          getColor(k),
          4);
      double[][] densities = PlotBar.densityEstimate(p.mcmc, optimBinWidth);
      PlotBar dist = new PlotBar(
          dateStr,
          DateTime.dateTimeDoubleToSecondsDouble(densities[0]),
          densities[1],
          getColor(k),
          0.7f);

      shifts.add(new ResultShift(
          date,
          p,
          dateStr,
          getColor(k),
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
          startShift == null ? 0 : startShift.parameter().getMaxpost(),
          endShift.parameter().getMaxpost());
      Color color = getColor(k);
      periods.add(buildResultPeriod(dataset, startShift, endShift, color));
      startShift = endShift;
    }
    GaugingsDataset dataset = buildGaugingDataset(
        time, stage, discharge, dischargeStd,
        startShift == null ? 0 : startShift.parameter().getMaxpost(),
        Double.MAX_VALUE);
    Color color = getColor(shifts.size());
    periods.add(buildResultPeriod(dataset, startShift, null, color));

  }

  private static ResultPeriod buildResultPeriod(
      GaugingsDataset dataset,
      ResultShift startShift,
      ResultShift endShift,
      Color color) {

    PlotPoints Qh = dataset.getPlotPoints(GaugingsDataset.PlotType.Qh);
    PlotPoints Qt = dataset.getPlotPoints(GaugingsDataset.PlotType.Qt);
    PlotPoints ht = dataset.getPlotPoints(GaugingsDataset.PlotType.ht);
    Qh.setLabel(dataset.getName());
    Qt.setLabel(dataset.getName());
    ht.setLabel(dataset.getName());
    Qh.setPaint(color);
    Qt.setPaint(color);
    ht.setPaint(color);
    return new ResultPeriod(
        startShift,
        endShift,
        dataset,
        dataset.getName(),
        color,
        Qh,
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
    double[] qstd = new double[n];
    boolean[] active = new boolean[n];
    for (int k = startIndex; k < endIndex; k++) {
      t[k - startIndex] = DateTime.doubleToDateTime(time[k]);
      h[k - startIndex] = stage[k];
      q[k - startIndex] = discharge[k];
      qstd[k - startIndex] = dischargeStd[k];
      active[k - startIndex] = true;
    }

    String name = "";

    String endTimeString = endTime == Double.MAX_VALUE
        ? dateFormatter.format(DateTime.doubleToDateTime(time[time.length - 1]))
        : dateFormatter.format(DateTime.doubleToDateTime(endTime));
    String startTimeString = startTime == 0.0
        ? dateFormatter.format(DateTime.doubleToDateTime(time[0]))
        : dateFormatter.format(DateTime.doubleToDateTime(startTime));
    if ((startTime == 0 && endTime == Double.MAX_VALUE) || (startTime != 0 && endTime != Double.MAX_VALUE)) {
      name = String.format("%s - %s", startTimeString, endTimeString);
    } else if (startTime == 0) {
      name = String.format("< %s", dateFormatter.format(DateTime.doubleToDateTime(endTime)));
    } else if (endTime == Double.MAX_VALUE) {
      name = String.format("> %s", dateFormatter.format(DateTime.doubleToDateTime(startTime)));
    }

    return GaugingsDataset.buildGaugingsDataset(
        name,
        h,
        q,
        qstd,
        active,
        t);
  }

  public DataTable getShiftsDataTablePlotItems() {

    // int n = shiftsOld.size();
    int n = shifts.size();
    String[] names = new String[n];
    LocalDateTime[] postMaxpost = new LocalDateTime[n];
    LocalDateTime[] postLow = new LocalDateTime[n];
    LocalDateTime[] postHight = new LocalDateTime[n];
    for (int k = 0; k < n; k++) {
      ResultShift shift = shifts.get(k);
      EstimatedParameter p = shift.parameter();
      names[k] = String.format("tau_%d", k);
      postMaxpost[k] = DateTime.doubleToDateTime(p.getMaxpost());
      double[] interval = p.get95interval();
      postLow[k] = DateTime.doubleToDateTime(interval[0]);
      postHight[k] = DateTime.doubleToDateTime(interval[1]);
    }

    DataTable dataTable = new DataTable();
    dataTable.addColumn(names);
    dataTable.addColumn(postMaxpost);
    dataTable.addColumn(postLow);
    dataTable.addColumn(postHight);
    dataTable.updateData();
    dataTable.setHeader(0, "Name");
    dataTable.setHeader(1, "Posterior_maxpost");
    dataTable.setHeader(2, "Posterior_low");
    dataTable.setHeader(3, "Posterior_high");
    dataTable.updateHeader();
    dataTable.autoResizeColumns();

    JButton showHeaderDescription = new JButton();
    showHeaderDescription.addActionListener(l -> {
      resultsColumnsDescritption.openDialog(T.text("shifts"));
    });
    T.t(this, showHeaderDescription, false, "table_headers_desc");
    dataTable.toolsPanel.addChild(showHeaderDescription, false);

    return dataTable;
  }

  public SimpleFlowPanel getMainResultPanel() {
    SimpleFlowPanel panel = new SimpleFlowPanel();
    panel.setGap(5);
    SimpleFlowPanel config = new SimpleFlowPanel(true);
    SimpleFlowPanel plots = new SimpleFlowPanel();

    List<PlotBar> shiftBars = shifts.stream().map(s -> s.distribution()).toList();
    List<PlotInfiniteLine> shiftLines = shifts.stream().map(s -> s.line()).toList();
    List<PlotPoints> ht = periods.stream().map(p -> p.htPoints()).toList();
    List<PlotPoints> Qt = periods.stream().map(p -> p.QtPoints()).toList();

    Plot shiftsPlot = new Plot(true, true);
    shiftsPlot.addXYItems(shiftBars, true);
    shiftsPlot.addXYItems(shiftLines, false);
    shiftsPlot.plot.getRangeAxis().setVisible(false);

    Plot stagePlot = new Plot(true, true);
    stagePlot.addXYItems(ht, true);
    stagePlot.addXYItems(shiftLines, false);

    Plot dischargePlot = new Plot(true, true);
    dischargePlot.addXYItems(Qt, true);
    dischargePlot.addXYItems(shiftLines, false);

    MultiPlotContainer mainStagePlot = new MultiPlotContainer(true);
    mainStagePlot.addPlot(stagePlot, 3);
    if (shiftBars.size() > 0) {
      mainStagePlot.addPlot(shiftsPlot, 1);
    }

    MultiPlotContainer mainDischargePlot = new MultiPlotContainer(true);
    mainDischargePlot.addPlot(dischargePlot, 3);
    if (shiftBars.size() > 0) {
      mainDischargePlot.addPlot(shiftsPlot, 1);
    }

    SimpleRadioButtons<String> radioDischargeOrStage = new SimpleRadioButtons<>();
    JRadioButton stageBtn = radioDischargeOrStage.addOption("h", T.text("stage"), "h");
    JRadioButton dischargeBtn = radioDischargeOrStage.addOption("q", T.text("discharge"), "q");
    config.addChild(stageBtn, false);
    config.addChild(dischargeBtn, false);
    radioDischargeOrStage.addChangeListener(l -> {
      plots.removeAll();
      String id = radioDischargeOrStage.getSelectedId();
      if (id.equals("h")) {
        plots.addChild(mainStagePlot, true);
        mainStagePlot.setDomainRange(mainDischargePlot.getCurrentDomainRange());
      } else {
        plots.addChild(mainDischargePlot, true);
        mainDischargePlot.setDomainRange(mainStagePlot.getCurrentDomainRange());
      }
    });

    radioDischargeOrStage.setSelected("h");
    plots.addChild(mainStagePlot, true);

    panel.addChild(config, false);
    panel.addChild(plots, true);

    stagePlot.setYAxisLabel(String.format("%s [m]", T.text("stage")));
    dischargePlot.setYAxisLabel(String.format("%s [m3/s]", T.text("discharge")));
    stageBtn.setText(T.text("stage"));
    dischargeBtn.setText(T.text("discharge"));

    return panel;
  }

  public SimpleFlowPanel getGaugingsDatasetsPanel() {
    SimpleFlowPanel panel = new SimpleFlowPanel();
    panel.setGap(5);

    SimpleFlowPanel periodSelectionPanel = new SimpleFlowPanel(true);
    periodSelectionPanel.setPadding(5);
    periodSelectionPanel.setGap(5);
    SimpleFlowPanel plotPanel = new SimpleFlowPanel();

    List<PlotPoints> gaugingsPoints = periods.stream().map(p -> p.QhPoints()).toList();
    PlotContainer plotContainer = new PlotContainer();
    Plot plot = new Plot();
    plot.addXYItems(gaugingsPoints);

    plot.axisX.setLabel(T.text("stage") + " [m]");
    plot.axisY.setLabel(T.text("discharge") + " [m3/s]");
    plot.axisYlog.setLabel(T.text("discharge") + " [m3/s]");

    plotContainer.setPlot(plot);
    plotPanel.addChild(plotContainer, true);

    panel.addChild(periodSelectionPanel, false);
    panel.addChild(plotPanel, true);

    for (ResultPeriod p : periods) {
      periodSelectionPanel.addChild(buildPeriodAction(p), false);
    }

    JButton buildGaugingItemBtn = new JButton();
    buildGaugingItemBtn.setIcon(BamItemType.GAUGINGS.getAddIcon());
    buildGaugingItemBtn.addActionListener(l -> {
      for (ResultPeriod p : periods) {
        addGaugingBamItemFromPeriod(p);
      }
    });

    periodSelectionPanel.addChild(buildGaugingItemBtn, false);

    return panel;
  }

  private static SimpleFlowPanel buildPeriodAction(ResultPeriod period) {
    SimpleFlowPanel panel = new SimpleFlowPanel();
    panel.setGap(5);

    JLabel icon = new JLabel(SimpleColorField.buildColorIcon(period.color(), 20));

    // SimpleCheckbox cb = new SimpleCheckbox();

    JLabel lbl = new JLabel(period.name());

    JButton buildGaugingItemBtn = new JButton();
    buildGaugingItemBtn.setIcon(BamItemType.GAUGINGS.getAddIcon());
    buildGaugingItemBtn.addActionListener(l -> {
      addGaugingBamItemFromPeriod(period);
    });

    panel.addChild(icon, false);
    // panel.addChild(cb, false);
    panel.addChild(lbl, true);
    panel.addChild(buildGaugingItemBtn, false);

    return panel;
  }

  private static Gaugings addGaugingBamItemFromPeriod(ResultPeriod period) {
    Gaugings item = (Gaugings) AppSetup.MAIN_FRAME.currentProject.addBamItem(BamItemType.GAUGINGS);
    item.setGaugingDataset(period.dataset());
    item.bamItemNameField.setText(period.name());
    return item;
  }

  private static record ResultPeriod(
      ResultShift start,
      ResultShift end,
      GaugingsDataset dataset,
      String name,
      Color color,
      PlotPoints QhPoints,
      PlotPoints QtPoints,
      PlotPoints htPoints) {

  }

  private static record ResultShift(
      LocalDateTime date,
      EstimatedParameter parameter,
      String name,
      Color color,
      PlotInfiniteLine line,
      PlotBar distribution) {

  }

  public SplitContainer getDetailedResultsPanel() {

    SimpleFlowPanel currentPanel = new SimpleFlowPanel();

    Explorer explorer = new Explorer();
    buildTree(rcShiftsDetectionRun, explorer);
    explorer.addTreeSelectionListener(l -> {

      ExplorerItem item = explorer.getLastSelectedPathComponent();
      if (item == null) {
        return;
      }

      currentPanel.removeAll();

      ShiftDetectionIteration selectedRsd = rcShiftsDetectionRun.getRatingShiftDetection(item.id);
      if (selectedRsd != null) {
        ShiftDetectionResults localRes = new ShiftDetectionResults(selectedRsd, true);
        currentPanel.addChild(localRes.getMainResultPanel());
      }
    });

    SplitContainer container = new SplitContainer(explorer, currentPanel, true);

    return container;
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
