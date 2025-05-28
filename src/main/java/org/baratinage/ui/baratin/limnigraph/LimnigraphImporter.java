package org.baratinage.ui.baratin.limnigraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.component.data_import.DataImporter;
import org.baratinage.ui.component.data_import.column_mapper.DateTimeColumnMapper;
import org.baratinage.ui.component.data_import.column_mapper.DoubleColumnMapper;
import org.baratinage.ui.component.data_import.column_mapper.IntegerColumnMapper;
import org.baratinage.ui.container.BorderedSimpleFlowPanel;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

public class LimnigraphImporter extends DataImporter {

  private LimnigraphDataset dataset;

  private final DateTimeColumnMapper dateTimeColMapper;
  private final DoubleColumnMapper stageColMapper;
  private final DoubleColumnMapper stageNonSysErrColMapper;
  private final DoubleColumnMapper stageSysErrColMapper;
  private final IntegerColumnMapper stageSysIndColMapper;

  public LimnigraphImporter() {
    super();

    dataFileReader.setFilters(
        new CommonDialog.CustomFileFilter(
            T.text("data_text_file"),
            "txt", "csv", "dat"));

    // **************************************************************
    // Column mapping fields

    dateTimeColMapper = new DateTimeColumnMapper();

    stageColMapper = new DoubleColumnMapper();
    stageNonSysErrColMapper = new DoubleColumnMapper();

    stageSysErrColMapper = new DoubleColumnMapper();
    stageSysIndColMapper = new IntegerColumnMapper();

    // **************************************************************
    // layout and additional labels

    SimpleFlowPanel leftPanel = new SimpleFlowPanel(true);
    leftPanel.setPadding(5);
    leftPanel.setGap(5);
    SimpleFlowPanel rightPanel = new SimpleFlowPanel(true);
    rightPanel.setPadding(5);
    rightPanel.setGap(5);
    SimpleFlowPanel mappingPanel = new SimpleFlowPanel();
    mappingPanel.addChild(leftPanel, true);
    mappingPanel.addChild(rightPanel, true);

    JLabel mandatoryFieldsLabel = new JLabel();

    leftPanel.addChild(mandatoryFieldsLabel, false);
    leftPanel.addChild(new SimpleSep(), false);
    leftPanel.addChild(dateTimeColMapper, false);
    leftPanel.addChild(stageColMapper, false);

    JLabel optionalFieldsLabel = new JLabel();
    rightPanel.addChild(optionalFieldsLabel, false);
    rightPanel.addChild(new SimpleSep(), false);
    rightPanel.addChild(stageNonSysErrColMapper, false);

    BorderedSimpleFlowPanel stageSysErrPanel = new BorderedSimpleFlowPanel(true);
    stageSysErrPanel.addChild(stageSysErrColMapper, false);
    stageSysErrPanel.addChild(stageSysIndColMapper, false);
    rightPanel.addChild(stageSysErrPanel, false);

    mainConfigPanel.addChild(mappingPanel, true);

    // **************************************************************
    // i18n

    T.t(this, mandatoryFieldsLabel, false, "mandatory_fields");
    T.t(this, optionalFieldsLabel, false, "optional_fields");
    T.t(this, stageColMapper.label, false, "stage");
    T.t(this, stageNonSysErrColMapper.label, false, "stage_non_sys_error_uncertainty");
    T.t(this, stageSysErrColMapper.label, false, "stage_sys_error_uncertainty");
    T.t(this, stageSysIndColMapper.label, false, "stage_sys_error_ind");

    // **************************************************************
    // whenever a field is modified

    ChangeListener l = (e) -> {
      TimedActions.debounce(ID, AppSetup.CONFIG.DEBOUNCED_DELAY_MS, this::updateValidityStatus);
    };

    dateTimeColMapper.addChangeListener(l);
    stageColMapper.combobox.addChangeListener(l);
    stageNonSysErrColMapper.combobox.addChangeListener(l);
    stageSysErrColMapper.combobox.addChangeListener(l);
    stageSysIndColMapper.combobox.addChangeListener(l);

  }

  private static double estimateDoubleMatrixTextFileSizeInKb(int numRows, int numCols) {
    int numSemicolons = (numRows - 1) * numCols; // Semicolons between values
    int numNewlines = numRows - 1; // Newlines between rows

    // Assuming average double length as 20 bytes
    int estimatedSizeBytes = (numRows * numCols * 20) + numSemicolons + numNewlines;

    // Convert bytes to KB
    double estimatedSizeKB = (double) estimatedSizeBytes / 1024.0;

    return estimatedSizeKB;
  }

  @Override
  public LimnigraphDataset getDataset() {
    return dataset;
  }

  private void updateValidityStatus() {

    errorPanel.removeAll();

    if (dataFileReader.file == null) {
      return;
    }

    dataPreview.resetColumnMappers();

    int[] dateTimeIndices = dateTimeColMapper.getIndices();
    for (int i : dateTimeIndices) {
      dataPreview.setColumnMapper(i, dateTimeColMapper);
    }

    dataPreview.setColumnMapper(stageColMapper.getIndex(), stageColMapper);
    dataPreview.setColumnMapper(stageNonSysErrColMapper.getIndex(), stageNonSysErrColMapper);
    dataPreview.setColumnMapper(stageSysErrColMapper.getIndex(), stageSysErrColMapper);
    dataPreview.setColumnMapper(stageSysIndColMapper.getIndex(), stageSysIndColMapper);

    Set<Integer> tInvalidRows = dateTimeColMapper.getInvalidIndices();
    boolean tColOk = dateTimeColMapper.hasValidSelection() && tInvalidRows.size() == 0;

    Set<Integer> hInvalidRows = stageColMapper.getInvalidIndices();
    boolean hColOk = stageColMapper.getIndex() >= 0 && hInvalidRows.size() == 0;

    Set<Integer> uNonSysInvalidRows = stageNonSysErrColMapper.getInvalidIndices();
    boolean uNonSysOk = stageNonSysErrColMapper.getIndex() == -1
        || (stageNonSysErrColMapper.getIndex() >= 0 && uNonSysInvalidRows.size() == 0);

    boolean uSysErrOk = !(stageSysErrColMapper.getIndex() == -1 && stageSysIndColMapper.getIndex() >= 0);
    boolean uSysIndOk = !(stageSysIndColMapper.getIndex() == -1 && stageSysErrColMapper.getIndex() >= 0);
    Set<Integer> uSysErrInvalidRows = stageSysErrColMapper.getInvalidIndices();
    Set<Integer> uSysIndInvalidRows = stageSysIndColMapper.getInvalidIndices();
    boolean uSysOk = uSysErrOk && uSysIndOk
        && uSysErrInvalidRows.size() == 0
        && uSysIndInvalidRows.size() == 0;

    dateTimeColMapper.setValidityView(tColOk);
    stageColMapper.combobox.setValidityView(hColOk);
    stageNonSysErrColMapper.combobox.setValidityView(uNonSysOk);
    stageSysErrColMapper.combobox.setValidityView(uSysErrOk);
    stageSysIndColMapper.combobox.setValidityView(uSysIndOk);

    Set<Integer> lastSkippedIndices = dataFileReader.getLastSkippedIndices();
    if (lastSkippedIndices.size() > 0) {
      errorPanel.addChild(MsgPanel.buildMsgPanel(
          T.text("msg_incomplete_rows_skipped_during_import",
              lastSkippedIndices.size(),
              Misc.createIntegerStringList(
                  lastSkippedIndices.stream().map(i -> i + 1).toList(),
                  5)),
          MsgPanel.TYPE.ERROR));
    }
    if (!tColOk) {
      errorPanel.addChild(MsgPanel.buildMsgPanel(
          T.text("date_time"),
          buildErrorMessage(tInvalidRows), MsgPanel.TYPE.ERROR));
    }
    if (!hColOk) {
      errorPanel.addChild(MsgPanel.buildMsgPanel(
          T.text("stage"),
          buildErrorMessage(hInvalidRows), MsgPanel.TYPE.ERROR));
    }
    if (!uNonSysOk) {
      errorPanel.addChild(MsgPanel.buildMsgPanel(
          T.text("stage_non_sys_error_uncertainty"),
          buildErrorMessage(uNonSysInvalidRows), MsgPanel.TYPE.ERROR));
    }
    if (!uSysOk) {
      errorPanel.addChild(MsgPanel.buildMsgPanel(
          T.text("stage_sys_error_uncertainty"),
          buildErrorMessage(null), MsgPanel.TYPE.ERROR));
    }

    validateButton.setEnabled(tColOk && hColOk && uNonSysOk && uSysOk);

    dataPreview.updatePreviewTable();

  }

  @Override
  protected void applyInputFileChange(List<String[]> data, String[] headers, String missingValue) {

    dateTimeColMapper.setData(data, headers, missingValue);
    stageColMapper.setData(data, headers, missingValue);
    stageNonSysErrColMapper.setData(data, headers, missingValue);
    stageSysErrColMapper.setData(data, headers, missingValue);
    stageSysIndColMapper.setData(data, headers, missingValue);

    updateValidityStatus();
  }

  @Override
  protected void buildDataset() {

    String fileName = dataFileReader.file.getName();

    LocalDateTime[] dateTime = dateTimeColMapper.getParsedColumn();
    double[] stage = stageColMapper.getParsedColumn();
    double[] nonSysStd = stageNonSysErrColMapper.getParsedColumn();
    double[] sysStd = stageSysErrColMapper.getParsedColumn();
    int[] sysInd = stageSysIndColMapper.getParsedColumn();

    int nCol = nonSysStd != null || sysStd != null ? AppSetup.CONFIG.N_SAMPLES_LIMNI_ERRORS.get() + 4
        : 4;
    double size = estimateDoubleMatrixTextFileSizeInKb(stage.length, nCol) / 2; // I devide by 2 to estimate the zip
                                                                                // compression
    System.out.println(size);
    if (size > 25000) {
      String sizeString = Misc.formatKilobitesSize(size);
      ConsoleLogger.warn("Large error matrix file size : " + size + "Kb (" + sizeString + ")");
      String areYouSure = T.text("are_you_sure");
      String message = String.format("<html>%s<br>%s</html>",
          areYouSure, T.text("large_file_size_warning", sizeString));
      if (!CommonDialog.confirmDialog(message, areYouSure)) {
        dataset = null;
        return;
      }
    }

    dataset = new LimnigraphDataset(
        fileName,
        dateTime,
        stage,
        nonSysStd,
        sysStd,
        sysInd);
  }

}
