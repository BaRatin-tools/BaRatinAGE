package org.baratinage.ui.baratin.limnigraph;

import java.awt.Component;
import java.awt.Dimension;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.component.DataFileReader;
import org.baratinage.ui.component.DataParser;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class LimnigraphImporter extends RowColPanel {

    private final String ID;

    private List<String[]> rawData;
    private String[] headers;
    private String missingValueString;

    private LimnigraphDataset dataset;
    private JDialog dialog;

    private final SimpleComboBox timeColComboBox;
    private final SimpleTextField timeColFormatField;
    private final SimpleComboBox stageColComboBox;

    private final SimpleComboBox nonSysUncertaintyComboBox;
    private final SimpleComboBox sysUncertaintyComboBox;
    private final SimpleComboBox sysIndComboBox;

    private final JButton validateButton;

    private final RowColPanel dataPreviewPanel;

    private final DataParser dataParser;

    public LimnigraphImporter() {
        super(AXIS.COL);

        ID = Misc.getTimeStampedId();

        // ********************************************************
        // Time related fields

        timeColComboBox = new SimpleComboBox();
        timeColFormatField = new SimpleTextField();

        JLabel timeColMappingLabel = new JLabel(T.text("date_time"));

        JLabel timeColFormatLabel = new JLabel(T.text("date_time_format"));
        timeColFormatField.setText("y/M/d H:m:s");
        JLabel timeFormatDetails = new JLabel(
                "<html><code>" +
                        "y = " + T.text("year") + ", " +
                        "M = " + T.text("month") + ", " +
                        "d = " + T.text("day") + ", " +
                        "H = " + T.text("hour") + " (0-23)" + ", " +
                        "m = " + T.text("minute") + " (0-59)" + ", " +
                        "s = " + T.text("second") + " (0-59)" +
                        "<br>" +
                        "yyyy-MM-dd HH:mm:ss = 2005-07-26 14:32:09" +
                        "</code></html>");

        // ********************************************************
        // Stage related fields

        stageColComboBox = new SimpleComboBox();
        JLabel stageColLabel = new JLabel(T.text("stage_level"));

        nonSysUncertaintyComboBox = new SimpleComboBox();
        JLabel nonSysUncertaintyLabel = new JLabel(T.html("stage_non_sys_error_uncertainty"));

        sysUncertaintyComboBox = new SimpleComboBox();
        JLabel sysUncertaintyLabel = new JLabel(T.html("stage_sys_error_uncertainty"));

        sysIndComboBox = new SimpleComboBox();
        JLabel sysIndLabel = new JLabel(T.text("stage_sys_error_ind"));

        // ********************************************************
        // Import configuration panel layout

        GridPanel columnMappingPanel = new GridPanel();
        columnMappingPanel.setPadding(5);
        columnMappingPanel.setGap(5);
        columnMappingPanel.setColWeight(1, 1);

        int rowIndex = 0;

        columnMappingPanel.insertChild(timeColMappingLabel, 0, rowIndex);
        columnMappingPanel.insertChild(timeColComboBox, 1, rowIndex);
        rowIndex++;

        columnMappingPanel.insertChild(timeColFormatLabel, 0, rowIndex);
        columnMappingPanel.insertChild(timeColFormatField, 1, rowIndex);
        rowIndex++;

        columnMappingPanel.insertChild(timeFormatDetails, 0, rowIndex, 2, 1);
        rowIndex++;

        columnMappingPanel.insertChild(stageColLabel, 0, rowIndex);
        columnMappingPanel.insertChild(stageColComboBox, 1, rowIndex);
        rowIndex++;

        columnMappingPanel.insertChild(nonSysUncertaintyLabel, 0, rowIndex);
        columnMappingPanel.insertChild(nonSysUncertaintyComboBox, 1, rowIndex);
        rowIndex++;

        columnMappingPanel.insertChild(sysUncertaintyLabel, 0, rowIndex);
        columnMappingPanel.insertChild(sysUncertaintyComboBox, 1, rowIndex);
        rowIndex++;

        columnMappingPanel.insertChild(sysIndLabel, 0, rowIndex);
        columnMappingPanel.insertChild(sysIndComboBox, 1, rowIndex);
        rowIndex++;

        // ********************************************************
        // dataset preview panel

        dataset = null;

        dataPreviewPanel = new RowColPanel();

        DataFileReader dataFileReader = new DataFileReader();
        dataParser = new DataParser();

        dataPreviewPanel.appendChild(dataParser);

        dataFileReader.addChangeListener((chEvt) -> {

            rawData = dataFileReader.getData(dataFileReader.nPreload);
            headers = dataFileReader.getHeaders();
            missingValueString = dataFileReader.missingValueString;

            dataParser.setRawData(rawData, headers, missingValueString);

            int nItems = timeColComboBox.getItemCount();
            int timeIndex = timeColComboBox.getSelectedIndex();
            int stageIndex = stageColComboBox.getSelectedIndex();
            int nonSysUncertaintyIndex = nonSysUncertaintyComboBox.getSelectedIndex();
            int sysUncertaintyIndex = sysUncertaintyComboBox.getSelectedIndex();
            int sysIndIndex = sysIndComboBox.getSelectedIndex();

            timeColComboBox.setItems(headers);
            stageColComboBox.setItems(headers);
            nonSysUncertaintyComboBox.setItems(headers);
            sysUncertaintyComboBox.setItems(headers);
            sysIndComboBox.setItems(headers);

            if (nItems == headers.length) {
                timeColComboBox.setSelectedItem(timeIndex);
                stageColComboBox.setSelectedItem(stageIndex);
                nonSysUncertaintyComboBox.setSelectedItem(nonSysUncertaintyIndex);
                sysUncertaintyComboBox.setSelectedItem(sysUncertaintyIndex);
                sysIndComboBox.setSelectedItem(sysIndIndex);
            }
        });

        // ********************************************************
        // action buttons

        RowColPanel actionPanel = new RowColPanel();
        actionPanel.setPadding(5);
        actionPanel.setGap(5);
        validateButton = new JButton(T.text("import"));
        validateButton.setEnabled(false);
        validateButton.addActionListener((e) -> {
            String filePath = dataFileReader.getFilePath();
            String fileName = Path.of(filePath).getFileName().toString();

            dataParser.setRawData(dataFileReader.getData(), headers, fileName);

            int dateTimeColIndex = timeColComboBox.getSelectedIndex();
            LocalDateTime[] dateTimeVector = dataParser.getDateTimeCol(
                    dateTimeColIndex,
                    timeColFormatField.getText());
            int stageColIndex = stageColComboBox.getSelectedIndex();
            double[] stage = dataParser.getDoubleCol(stageColIndex);

            int nonSysUncertaintyInd = nonSysUncertaintyComboBox.getSelectedIndex();
            int sysUncertaintyInd = sysUncertaintyComboBox.getSelectedIndex();
            int sysIndInd = sysIndComboBox.getSelectedIndex();

            dataset = new LimnigraphDataset(
                    fileName,
                    dateTimeVector,
                    stage,
                    nonSysUncertaintyInd < 0 ? null
                            : Arrays.stream(dataParser.getDoubleCol(nonSysUncertaintyInd)).map(u -> u / 2.0).toArray(),
                    sysUncertaintyInd < 0 ? null
                            : Arrays.stream(dataParser.getDoubleCol(sysUncertaintyInd)).map(u -> u / 2.0).toArray(),
                    sysIndInd < 0 ? null : dataParser.getIntCol(sysIndInd));

            dialog.setVisible(false);

        });
        JButton cancelButton = new JButton(T.text("cancel"));
        cancelButton.addActionListener((e) -> {
            dialog.setVisible(false);
        });

        actionPanel.appendChild(cancelButton, 0);
        actionPanel.appendChild(new Component() {
        }, 1);
        actionPanel.appendChild(validateButton, 0);

        // ********************************************************
        // final import panel layout

        appendChild(dataFileReader, 0);
        appendChild(dataPreviewPanel, 1);
        appendChild(new JSeparator(), 0);
        appendChild(columnMappingPanel, 0);
        appendChild(new JSeparator(), 0);
        appendChild(actionPanel, 0);

        // ********************************************************
        // react to change in user inputs (preview table and import button)

        ChangeListener cbChangeListener = (chEvt) -> {
            TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, this::updateValidityStatus);
        };

        timeColComboBox.addChangeListener(cbChangeListener);
        timeColFormatField.addChangeListener(cbChangeListener);
        stageColComboBox.addChangeListener(cbChangeListener);
        nonSysUncertaintyComboBox.addChangeListener(cbChangeListener);
        sysUncertaintyComboBox.addChangeListener(cbChangeListener);
        sysIndComboBox.addChangeListener(cbChangeListener);

    }

    private void updateValidityStatus() {
        // if there's not data, not point checking validity
        if (rawData == null) {
            timeColComboBox.setValidityView(false);
            timeColFormatField.setValidityView(false);
            stageColComboBox.setValidityView(false);
            nonSysUncertaintyComboBox.setValidityView(false);
            sysUncertaintyComboBox.setValidityView(false);
            sysIndComboBox.setValidityView(false);
            validateButton.setEnabled(false);
            return;
        }
        dataParser.ignoreAll();

        // time

        int timeInd = timeColComboBox.getSelectedIndex();
        String dateTimeFormat = timeColFormatField.getText();
        dataParser.setAsDateTimeCol(timeInd, dateTimeFormat);
        boolean timeOk = timeInd >= 0 && dataParser.testColValidity(timeInd);
        boolean timeFormatOk = dataParser.testDateTimeFormat(dateTimeFormat);

        // stage

        int stageInd = stageColComboBox.getSelectedIndex();
        dataParser.setAsDoubleCol(stageInd);
        boolean stageOk = stageInd >= 0 &&
                dataParser.testColValidity(stageInd);

        // non-sys error

        int nonSysUncertaintyInd = nonSysUncertaintyComboBox.getSelectedIndex();
        boolean nonSysErrOk = true;
        if (nonSysUncertaintyInd >= 0) {
            dataParser.setAsDoubleCol(nonSysUncertaintyInd);
            nonSysErrOk = dataParser.testColValidity(nonSysUncertaintyInd);
        }

        // sys err

        int sysUncertaintyInd = sysUncertaintyComboBox.getSelectedIndex();
        int sysIndInd = sysIndComboBox.getSelectedIndex();

        boolean sysErrOk = ((sysUncertaintyInd >= 0 && sysIndInd >= 0) ||
                (sysUncertaintyInd < 0 && sysIndInd < 0));

        if (sysUncertaintyInd >= 0) {
            dataParser.setAsDoubleCol(sysUncertaintyInd);
            sysErrOk = sysErrOk && dataParser.testColValidity(sysUncertaintyInd);
        }
        if (sysIndInd >= 0) {
            dataParser.setAsIntCol(sysIndInd);
            sysErrOk = sysErrOk && dataParser.testColValidity(sysIndInd);
        }

        // update ui
        dataParser.updateColumnTypes();
        timeColComboBox.setValidityView(timeOk);
        timeColFormatField.setValidityView(timeFormatOk);
        stageColComboBox.setValidityView(stageOk);
        nonSysUncertaintyComboBox.setValidityView(nonSysErrOk);
        sysUncertaintyComboBox.setValidityView(sysErrOk);
        sysIndComboBox.setValidityView(sysErrOk);
        validateButton.setEnabled(timeOk && timeFormatOk && stageOk && nonSysErrOk && sysErrOk);

    }

    public void showDialog() {

        dialog = new JDialog(AppSetup.MAIN_FRAME, true);
        dialog.setContentPane(this);

        dialog.setTitle(T.text("import_limnigraph"));
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.setPreferredSize(new Dimension(900, 800));

        dialog.pack();
        dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        dialog.setVisible(true);
        dialog.dispose();
    }

    public LimnigraphDataset getDataset() {
        return dataset;
    }

}
