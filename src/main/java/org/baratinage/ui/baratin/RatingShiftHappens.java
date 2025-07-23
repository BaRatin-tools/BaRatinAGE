package org.baratinage.ui.baratin;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import org.baratinage.AppSetup;

import org.baratinage.jbam.Model;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;

import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.rating_shifts_happens.RatingShiftsHappensResults;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionOverall;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionConfig;
import org.baratinage.ui.component.SimpleDialog;
import org.baratinage.ui.container.SimpleFlowPanel;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONObject;

public class RatingShiftHappens extends BamItem {

  private final BamItemParent hydrauConfParent;
  private final BamItemParent gaugingsParent;

  private final SimpleFlowPanel errorMsgPanel;

  private final JButton runBtn;

  private ShiftDetectionOverall ratingShiftDetection;

  private final RatingShiftsHappensResults ratingShiftResults;

  public RatingShiftHappens(String uuid, BamProject project) {
    super(BamItemType.RATING_SHIFT_HAPPENS, uuid, project);

    ratingShiftResults = new RatingShiftsHappensResults();

    errorMsgPanel = new SimpleFlowPanel(true);
    errorMsgPanel.setGap(5);

    // **************************************************************
    // Hydraulic config
    hydrauConfParent = new BamItemParent(
        this,
        BamItemType.HYDRAULIC_CONFIG, BamItemType.HYDRAULIC_CONFIG_BAC, BamItemType.HYDRAULIC_CONFIG_QFH);

    // FIXME: add public abstract JSONFilter getComparisonFilter() in BamItem
    String[] hydraulicConfigFilterKeys = new String[] {
        "ui", "bamRunId", "backup", "jsonStringBackup", "priorRatingCurve",
        "stageGridConfig", "allControlOptions", "controlTypeIndex", "isKACmode",
        "isLocked", "isReversed", "description", "autoInitialValue" };
    hydrauConfParent.setComparisonJSONfilter(
        BamItemType.HYDRAULIC_CONFIG, new JSONFilter(true, true, hydraulicConfigFilterKeys));
    hydrauConfParent.setComparisonJSONfilter(
        BamItemType.HYDRAULIC_CONFIG_BAC, new JSONFilter(true, true, hydraulicConfigFilterKeys));

    hydrauConfParent.setComparisonJSONfilter(
        BamItemType.HYDRAULIC_CONFIG_QFH, new JSONFilter(true, true,
            "eqConfigsAndPriors"));

    hydrauConfParent.addChangeListener((e) -> {
      BamItem bamItem = hydrauConfParent.getCurrentBamItem();
      System.out.println(bamItem);
      TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
    });

    // **************************************************************
    // Gaugings
    gaugingsParent = new BamItemParent(this, BamItemType.GAUGINGS);

    gaugingsParent.setComparisonJSONfilter(new JSONFilter(true, true,
        "name", "headers", "filePath", "nested"));
    gaugingsParent.addChangeListener((e) -> {
      Gaugings bamItem = (Gaugings) gaugingsParent.getCurrentBamItem();
      System.out.println(bamItem);
      TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::checkSync);
    });

    // **************************************************************
    // ui

    runBtn = new JButton();
    runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));
    runBtn.setText(T.text("detect_rating_shifts"));
    runBtn.addActionListener((l) -> {
      initShiftDetection();
      runShiftDetection();
    });

    // **************************************************************
    // ui

    SimpleFlowPanel configPanel = new SimpleFlowPanel();
    configPanel.setGap(5);
    configPanel.setPadding(5);
    configPanel.setAlign(SimpleFlowPanel.ALIGN.END);
    configPanel.addChild(hydrauConfParent, true);
    configPanel.addChild(gaugingsParent, true);
    configPanel.addChild(runBtn, true);
    // configPanel.addChild(updateResults, false);

    SimpleFlowPanel mainPanel = new SimpleFlowPanel(true);
    mainPanel.setGap(5);
    mainPanel.addChild(configPanel, false);
    mainPanel.addChild(errorMsgPanel, false);
    mainPanel.addChild(ratingShiftResults, true);

    setContent(mainPanel);

    checkSync();

  }

  private void checkSync() {
    errorMsgPanel.removeAll();
    List<MsgPanel> allMessagePanels = new ArrayList<>();

    // **************************************************************
    // gaugings
    boolean gaugingsValid = true;
    gaugingsParent.updateSyncStatus();
    MsgPanel gaugingErrMsg = gaugingsParent.getMessagePanel();
    if (gaugingErrMsg == null) {
      // here, an element must be selected
      BamItem gaugingsBamItem = gaugingsParent.getCurrentBamItem();
      GaugingsDataset gaugingsDataset = ((Gaugings) gaugingsBamItem).getGaugingDataset();
      if (gaugingsDataset == null) {
        ConsoleLogger.error("Gaugings BamItem is missing data!");
        MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
        msg.message.setText(T.text("missing_gaugings_data_error"));
        allMessagePanels.add(
            msg);
        gaugingsValid = false;
      } else {
        if (gaugingsDataset.getActiveDateTimeAsDouble() == null) {
          ConsoleLogger.error("Gaugings BamItem is missing a date/time component!");
          MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
          msg.message.setText(T.text("missing_gaugings_datetime_data_error"));
          allMessagePanels.add(
              msg);
          gaugingsValid = false;
        }

        if (gaugingsDataset.containsMissingValues()) {
          ConsoleLogger.error("Gaugings BamItem contains missing values!");
          MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
          msg.message.setText(T.text("gaugings_data_contains_missing_values_error"));
          allMessagePanels.add(
              msg);
          gaugingsValid = false;
        }
      }
    } else {
      gaugingsValid = false;
      allMessagePanels.add(gaugingErrMsg);
    }
    gaugingsParent.cb.setValidityView(gaugingsValid);

    // **************************************************************
    // hydraulic config
    boolean hydrauConfValid = true;
    hydrauConfParent.updateSyncStatus();
    MsgPanel hydrauConfErrMsg = hydrauConfParent.getMessagePanel();
    if (hydrauConfErrMsg == null) {
      Model model = buildModel();
      if (model == null) {
        ConsoleLogger.error("Hydraulic configuration is not valid!");
        MsgPanel msg = new MsgPanel(MsgPanel.TYPE.ERROR, true);
        msg.message.setText(T.text("invalid_hydrauconf_error"));
        allMessagePanels.add(
            msg);
        hydrauConfValid = false;
      }
    } else {
      hydrauConfValid = false;
      allMessagePanels.add(hydrauConfErrMsg);
    }
    hydrauConfParent.cb.setValidityView(hydrauConfValid);

    runBtn.setEnabled(allMessagePanels.size() == 0);

    for (MsgPanel msg : allMessagePanels) {
      errorMsgPanel.addChild(msg, false);
    }
  }

  private void initShiftDetection() {

    // **************************************************************
    // checking conditions

    BamItem gaugingsBamItem = gaugingsParent.getCurrentBamItem();
    if (gaugingsBamItem == null || !(gaugingsBamItem instanceof Gaugings)) {
      ConsoleLogger.error("Gaugings BamItem is invalid!");
      return;
    }

    GaugingsDataset gaugingsDataset = ((Gaugings) gaugingsBamItem).getGaugingDataset();

    if (gaugingsDataset == null) {
      ConsoleLogger.error("Gaugings BamItem is missing data!");
      return;
    }

    if (gaugingsDataset.getActiveDateTimeAsDouble() == null) {
      ConsoleLogger.error("Gaugings BamItem is missing a date/time component!");
      return;
    }

    if (gaugingsDataset.containsMissingValues()) {
      ConsoleLogger.error("Gaugings BamItem contains missing values!");
      return;
    }

    Model model = buildModel();
    if (model == null) {
      ConsoleLogger.error("Hydraulic configuration is not valid!");
      return;
    }

    // **************************************************************
    // rertieve main data

    double[] ldt = gaugingsDataset.getActiveDateTimeAsDouble();
    double[] h = gaugingsDataset.getActiveStageValues();
    double[] hstd = gaugingsDataset.getActiveStageStdUncertainty();
    double[] q = gaugingsDataset.getActiveDischargeValues();
    double[] qstd = gaugingsDataset.getActiveDischargeStdUncertainty();

    // **************************************************************
    // intializing, configure initial BaM run

    // build Model

    // sort gaugings by date
    Integer[] indices = Misc.range(0, ldt.length);
    Arrays.sort(indices, (a, b) -> {
      return Double.compare(ldt[a], ldt[b]);
    });

    ratingShiftDetection = new ShiftDetectionOverall(
        Misc.reorderArray(indices, ldt),
        Misc.reorderArray(indices, h),
        hstd == null ? null : Misc.reorderArray(indices, hstd),
        Misc.reorderArray(indices, q),
        Misc.reorderArray(indices, qstd),
        model,
        3,
        2);

  }

  private void runShiftDetection() {

    SimpleDialog dialog = new SimpleDialog(AppSetup.MAIN_FRAME, true);
    JProgressBar pbOverall = new JProgressBar();
    pbOverall.setStringPainted(true);
    pbOverall.setMaximum(100);
    pbOverall.setString("0 %");

    JProgressBar pbStep = new JProgressBar();
    pbStep.setStringPainted(true);
    pbStep.setMaximum(100);
    pbStep.setString("0 %");

    Dimension dim = new Dimension(300, 20);
    pbOverall.setPreferredSize(dim);
    pbStep.setPreferredSize(dim);

    JButton cancelBtn = new JButton();
    cancelBtn.setText(T.text("cancel"));
    cancelBtn.addActionListener(l -> {
      ratingShiftDetection.cancel();
    });

    SimpleFlowPanel pbPanel = new SimpleFlowPanel(true);
    pbPanel.setGap(5);
    pbPanel.addChild(pbOverall, false);
    pbPanel.addChild(pbStep, false);
    pbPanel.addChild(cancelBtn, false);
    dialog.setContent(pbPanel);
    dialog.setTitle(T.text("rating_shift_dectection_running"));

    ratingShiftDetection.runShiftDetection(
        o -> {
          float p = o.overallProgress() * 100;

          // estimation that compromize between the maximum number of steps and what is
          // currently planned.
          float nStepsEstimate = ((float) o.plannedSteps() + (float) o.doneSteps() + (float) o.numberOfStepsEstimated())
              / 2f;
          float pSteps = (float) o.doneSteps() / nStepsEstimate * 100f;
          p = pSteps;

          int pbOverallPercent = (int) (p);
          pbOverall.setValue(pbOverallPercent);
          int pbPercent = (int) (o.stepProgress() * 100);
          pbStep.setValue(pbPercent);

          String overallString = String.format("%.0f %% - %d / %d (%d)",
              p,
              o.doneSteps(),
              o.numberOfStepsEstimated(),
              o.plannedSteps() + o.doneSteps());

          pbOverall.setString(overallString);
          pbOverall.repaint();

          String stepString = String.format("%.0f %%", o.stepProgress() * 100);
          pbStep.setString(stepString);
          pbStep.repaint();

          dialog.update();
        },
        () -> {
          updateResultsPanel();
          dialog.closeDialog();
        },
        () -> {
          dialog.closeDialog();
        }

    );

    dialog.openDialog();
  }

  private Model buildModel() {

    BamItem hydraulicConfigItem = hydrauConfParent.getCurrentBamItem();
    if (hydraulicConfigItem == null) {
      return null;
    }

    if (!(hydraulicConfigItem instanceof HydraulicConfiguration)) {
      return null;
    }

    // hydraulic config

    HydraulicConfiguration hc = (HydraulicConfiguration) hydraulicConfigItem;

    // Model
    String xTra = hc.getXtra("");
    String modelId = hc.getModelId();
    Parameter[] parameters = hc.getParameters();
    String[] inputNames = hc.getInputNames();
    String[] outputNames = hc.getOutputNames();
    int nInputs = inputNames.length;
    int nOutputs = outputNames.length;

    if (parameters == null) {
      return null;
    }

    Model model = new Model(
        BamFilesHelpers.CONFIG_MODEL,
        modelId,
        nInputs,
        nOutputs,
        parameters,
        xTra,
        BamFilesHelpers.CONFIG_XTRA);

    return model;
  }

  private void updateResultsPanel() {

    if (ratingShiftDetection == null) {
      return;
    }

    ratingShiftResults.setGaugingsBasedDetectionResults(ratingShiftDetection);

  }

  @Override
  public BamConfig save(boolean writeFiles) {
    BamConfig config = new BamConfig(0);

    // parents
    config.JSON.put("hydrauConfig", hydrauConfParent.saveConfig().JSON);
    BamConfig gaugingsConfig = gaugingsParent.saveConfig();
    config.FILE_PATHS.addAll(gaugingsConfig.FILE_PATHS);
    config.JSON.put("gaugings", gaugingsConfig.JSON);

    // recursive rating shift detection
    if (ratingShiftDetection != null) {
      ShiftDetectionConfig rcdConfig = ratingShiftDetection.save(writeFiles);
      config.FILE_PATHS.addAll(rcdConfig.getAllFilePaths());
      config.JSON.put("ratingShiftDetection", rcdConfig.getFullConfig());
    }

    // saving results panel
    config.JSON.put("results", ratingShiftResults.toJSON());

    return config;
  }

  @Override
  public void load(BamConfig config) {

    JSONObject json = config.JSON;

    if (json.has("hydrauConfig")) {
      hydrauConfParent.fromJSON(json.getJSONObject("hydrauConfig"));
    } else {
      ConsoleLogger.log("missing 'hydrauConfig'");
    }

    if (json.has("gaugings")) {
      gaugingsParent.fromJSON(json.getJSONObject("gaugings"));
    } else {
      ConsoleLogger.log("missing 'gaugings'");
    }

    if (json.has("ratingShiftDetection")) {
      JSONObject rcdConfig = json.getJSONObject("ratingShiftDetection");
      ratingShiftDetection = ShiftDetectionOverall.load(new ShiftDetectionConfig(rcdConfig));
      updateResultsPanel();
    }

    if (json.has("results")) {
      ratingShiftResults.fromJSON(json.getJSONObject("results"));
    }
  }

}