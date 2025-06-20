package org.baratinage.ui.baratin.gaugings;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.utils.perf.TimedActions;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.DataFileReader;
import org.baratinage.ui.component.SimplePopup;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.component.data_import.DataImporter;
import org.baratinage.ui.component.data_import.column_mapper.BooleanColumnMapper;
import org.baratinage.ui.component.data_import.column_mapper.DateTimeColumnMapper;
import org.baratinage.ui.component.data_import.column_mapper.DoubleColumnMapper;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.SimpleFlowPanel;

public class GaugingsImporter extends DataImporter {

    private GaugingsDataset dataset;

    private final DoubleColumnMapper stageColMapper;
    private final DoubleColumnMapper dischargeColMapper;
    private final DoubleColumnMapper dischargeUColMapper;
    private final BooleanColumnMapper validityColMapper;
    private final DateTimeColumnMapper dateTimeColMapper;
    private final DoubleColumnMapper stageUColMapper;

    public GaugingsImporter() {
        super();

        dataFileReader.setFilters(
                new CommonDialog.CustomFileFilter(
                        T.text("data_text_file"),
                        "txt", "csv", "dat"),
                new CommonDialog.CustomFileFilter(
                        T.text("bareme_bad_text_file"),
                        "bad"));

        // **********************************************************
        // mapping fields

        stageColMapper = new DoubleColumnMapper();
        stageColMapper.setGuessPatterns("(?i)^(h|h_[^\\s,;]+|stage|stage_[^\\s,;]+)$");

        dischargeColMapper = new DoubleColumnMapper();
        dischargeColMapper.setGuessPatterns(
                "(?i)^(Q|Q_[^\\s,;]+|discharge|discharge_[^\\s,;]+|streamflow|streamflow_[^\\s,;]+)$");

        dischargeUColMapper = new DoubleColumnMapper();

        validityColMapper = new BooleanColumnMapper();
        dateTimeColMapper = new DateTimeColumnMapper();

        stageUColMapper = new DoubleColumnMapper();
        SimpleFlowPanel stageUColMapperPanel = new SimpleFlowPanel();
        stageUColMapperPanel.setGap(5);
        JButton btnWarning = new JButton();
        btnWarning.setIcon(AppSetup.ICONS.WARNING_SMALL);
        btnWarning.addActionListener(l -> {
            SimplePopup popup = new SimplePopup(btnWarning);
            popup.setContent(new JLabel(T.html("gauging_stage_uncertainty_import_warning")));
            popup.show();
        });
        stageUColMapperPanel.addChild(stageUColMapper, true);
        stageUColMapperPanel.addChild(btnWarning, false);

        // **********************************************************
        // layout and labels

        GridPanel columnMappingPanel = new GridPanel();
        columnMappingPanel.setPadding(5);
        columnMappingPanel.setGap(5);
        columnMappingPanel.setColWeight(1, 1);

        SimpleFlowPanel colMappingMainPanel = new SimpleFlowPanel();
        colMappingMainPanel.setGap(5);
        colMappingMainPanel.setPadding(5);
        SimpleFlowPanel colMappingLeftPanel = new SimpleFlowPanel(true);
        colMappingLeftPanel.setGap(5);
        colMappingMainPanel.addChild(colMappingLeftPanel, true);
        SimpleFlowPanel colMappingRightPanel = new SimpleFlowPanel(true);
        colMappingRightPanel.setGap(5);
        colMappingMainPanel.addChild(colMappingRightPanel, true);

        JLabel mandatoryFieldsLabel = new JLabel();
        T.t(this, mandatoryFieldsLabel, false, "mandatory_fields");
        colMappingRightPanel.addChild(mandatoryFieldsLabel, false);

        colMappingLeftPanel.addChild(mandatoryFieldsLabel, false);
        colMappingLeftPanel.addChild(new SimpleSep(), false);
        colMappingLeftPanel.addChild(stageColMapper, false);
        colMappingLeftPanel.addChild(dischargeColMapper, false);
        colMappingLeftPanel.addChild(dischargeUColMapper, false);

        JLabel optionalFieldsLabel = new JLabel();
        T.t(this, optionalFieldsLabel, false, "optional_fields");
        colMappingRightPanel.addChild(optionalFieldsLabel, false);

        colMappingRightPanel.addChild(new SimpleSep(), false);
        colMappingRightPanel.addChild(validityColMapper, false);

        colMappingRightPanel.addChild(dateTimeColMapper, false);
        colMappingRightPanel.addChild(stageUColMapperPanel, false);

        mainConfigPanel.addChild(colMappingMainPanel, true);

        // **********************************************************
        // i18n

        T.updateHierarchy(this, validityColMapper);
        T.updateHierarchy(this, dateTimeColMapper);
        T.t(this, validateButton, false, "import");
        T.t(this, stageColMapper.label, false, "stage");
        T.t(this, dischargeColMapper.label, false, "discharge");
        T.t(this, dischargeUColMapper.label, false, "discharge_uncertainty_percent");
        T.t(this, stageUColMapper.label, false, "stage_uncertainty_percent");

        // **********************************************************
        // when updating a field
        ChangeListener cbChangeListener = (chEvt) -> {
            TimedActions.debounce(ID, AppSetup.CONFIG.DEBOUNCED_DELAY_MS, this::updateValidityStatus);
        };

        stageColMapper.combobox.addChangeListener(cbChangeListener);
        dischargeColMapper.combobox.addChangeListener(cbChangeListener);
        dischargeUColMapper.combobox.addChangeListener(cbChangeListener);
        validityColMapper.addChangeListener(cbChangeListener);
        dateTimeColMapper.addChangeListener(cbChangeListener);
        stageUColMapper.combobox.addChangeListener(cbChangeListener);
    }

    private static boolean isBaremeBadFile(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf('.');

        if (lastIndexOfDot == -1 || lastIndexOfDot == fileName.length() - 1) {
            return false;
        }

        return fileName.substring(lastIndexOfDot + 1).toLowerCase().equals("bad");
    }

    private void updateValidityStatus() {
        errorPanel.removeAll();

        if (dataFileReader.file == null) {
            return;
        }

        dataPreview.resetColumnMappers();

        dataPreview.setColumnMapper(stageColMapper.getIndex(), stageColMapper);
        dataPreview.setColumnMapper(dischargeColMapper.getIndex(), dischargeColMapper);
        dataPreview.setColumnMapper(dischargeUColMapper.getIndex(), dischargeUColMapper);
        dataPreview.setColumnMapper(validityColMapper.getIndex(), validityColMapper);
        int[] dateTimeIndices = dateTimeColMapper.getIndices();
        for (int i : dateTimeIndices) {
            dataPreview.setColumnMapper(i, dateTimeColMapper);
        }

        Set<Integer> hInvalidIndices = stageColMapper.getInvalidIndices();
        boolean hColOk = stageColMapper.getIndex() >= 0 && hInvalidIndices.size() == 0;

        Set<Integer> qInvalidIndices = dischargeColMapper.getInvalidIndices();
        boolean qColOk = dischargeColMapper.getIndex() >= 0 && qInvalidIndices.size() == 0;

        Set<Integer> uqInvalidIndices = dischargeUColMapper.getInvalidIndices();
        boolean uqColOk = dischargeUColMapper.getIndex() >= 0 && uqInvalidIndices.size() == 0;

        Set<Integer> tInvalidIndices = dateTimeColMapper.getInvalidIndices();
        boolean tColOk = !dateTimeColMapper.hasValidSelection()
                || (dateTimeColMapper.hasValidSelection() && tInvalidIndices.size() == 0);

        Set<Integer> uhInvalidIndices = stageUColMapper.getInvalidIndices();
        boolean uhColOk = stageUColMapper.getIndex() >= 0 && uhInvalidIndices.size() == 0;

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
        if (!hColOk) {
            errorPanel.addChild(MsgPanel.buildMsgPanel(
                    T.text("stage"),
                    buildErrorMessage(hInvalidIndices), MsgPanel.TYPE.ERROR));
        }
        if (!qColOk) {
            errorPanel.addChild(MsgPanel.buildMsgPanel(
                    T.text("discharge"),
                    buildErrorMessage(hInvalidIndices), MsgPanel.TYPE.ERROR));
        }
        if (!uqColOk) {
            errorPanel.addChild(MsgPanel.buildMsgPanel(
                    T.text("discharge_uncertainty_percent"),
                    buildErrorMessage(uqInvalidIndices), MsgPanel.TYPE.ERROR));
        }
        if (!tColOk) {
            errorPanel.addChild(MsgPanel.buildMsgPanel(
                    T.text("date_time"),
                    buildErrorMessage(tInvalidIndices), MsgPanel.TYPE.ERROR));
        }

        if (!uhColOk) {
            errorPanel.addChild(MsgPanel.buildMsgPanel(
                    T.text("stage_uncertainty_percent"),
                    buildErrorMessage(uhInvalidIndices), MsgPanel.TYPE.ERROR));
        }

        stageColMapper.combobox.setValidityView(hColOk);
        dischargeColMapper.combobox.setValidityView(qColOk);
        dischargeUColMapper.combobox.setValidityView(uqColOk);
        dateTimeColMapper.setValidityView(tColOk);
        stageUColMapper.combobox.setValidityView(uhColOk);

        validateButton.setEnabled(hColOk && qColOk && uqColOk && tColOk && uhColOk);

        dataPreview.updatePreviewTable();
    }

    public GaugingsDataset getDataset() {
        return dataset;
    }

    private static GaugingsDataset buildGaugingDatasetFromBaremeFile(String fileName, String filePath) {
        new DataFileReader();

        try {
            List<double[]> data = ReadFile.readMatrix(
                    filePath,
                    " ",
                    1,
                    Integer.MAX_VALUE,
                    "",
                    true,
                    true);

            double[] h = data.get(0);
            double[] Q = data.get(2);
            double[] uQ = data.get(3);

            int n = h.length;
            for (int k = 0; k < n; k++) {
                double uQpercent = (uQ[k] * 2) / Q[k] * 100;
                uQ[k] = uQpercent;
            }

            return GaugingsDataset.buildGaugingsDataset(
                    fileName,
                    h,
                    Q,
                    uQ,
                    null,
                    null,
                    null);
        } catch (IOException e) {
            ConsoleLogger.error("Failed to read Barème .bad file!");
            ConsoleLogger.error(e);
        }

        return null;
    }

    @Override
    protected void applyInputFileChange(List<String[]> data, String[] headers, String missingValue) {

        String fileName = dataFileReader.file.getName();

        if (isBaremeBadFile(fileName)) {
            GaugingsDataset gaugings = buildGaugingDatasetFromBaremeFile(fileName, dataFileReader.filePath);
            if (gaugings == null) {
                CommonDialog.errorDialog(T.text("bareme_bad_import_error"));
                return;
            }
            dataset = gaugings;
            return;
        }

        stageColMapper.setData(data, headers, missingValue);
        dischargeColMapper.setData(data, headers, missingValue);
        dischargeUColMapper.setData(data, headers, missingValue);
        validityColMapper.setData(data, headers, missingValue);
        dateTimeColMapper.setData(data, headers, missingValue);
        stageUColMapper.setData(data, headers, missingValue);

        stageColMapper.guessColumnIndex(false);
        dischargeColMapper.guessColumnIndex(false);

        updateValidityStatus();

    }

    @Override
    protected void buildDataset() {

        String fileName = dataFileReader.file.getName();

        double[] h = stageColMapper.getParsedColumn();
        double[] Q = dischargeColMapper.getParsedColumn();
        double[] uQ = dischargeUColMapper.getParsedColumn();
        // double[] vDouble = Misc.ones(h.length);
        LocalDateTime[] dateTime = dateTimeColMapper.getParsedColumn();

        boolean[] v = validityColMapper.getParsedColumn();

        double[] uh = stageUColMapper.getParsedColumn();

        dataset = GaugingsDataset.buildGaugingsDataset(
                fileName,
                h,
                Q,
                uQ,
                v,
                dateTime,
                uh);
    }

}
