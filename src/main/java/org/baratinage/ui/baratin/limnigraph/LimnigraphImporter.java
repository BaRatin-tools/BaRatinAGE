package org.baratinage.ui.baratin.limnigraph;

import java.awt.Component;
import java.awt.Dimension;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.AppConfig;

import org.baratinage.ui.component.DataFileReader;
import org.baratinage.ui.component.DataParser;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;

public class LimnigraphImporter extends RowColPanel {

    private List<String[]> rawData;
    private String[] headers;
    private String missingValueString;

    private SimpleComboBox timeColComboBox;
    private SimpleTextField timeColFormatField;

    private JCheckBox stageAllColumnsCheckBox;
    private SimpleComboBox stageColComboBox;

    private JDialog dialog;
    private RowColPanel dataPreviewPanel;
    private LimnigraphDataset dataset;

    public LimnigraphImporter() {
        super(AXIS.COL);

        timeColComboBox = new SimpleComboBox();
        timeColFormatField = new SimpleTextField();

        stageColComboBox = new SimpleComboBox();
        stageAllColumnsCheckBox = new JCheckBox(T.text("use_all_columns_for_stage"));
        stageAllColumnsCheckBox.setSelected(true);
        stageAllColumnsCheckBox.addChangeListener((chEvt) -> {
            stageColComboBox.setEnabled(!stageAllColumnsCheckBox.isSelected());
        });
        dataset = null;

        dataPreviewPanel = new RowColPanel();

        DataFileReader dataFileReader = new DataFileReader();
        DataParser dataParser = new DataParser();

        dataPreviewPanel.appendChild(dataParser);

        dataFileReader.addChangeListener((chEvt) -> {

            rawData = dataFileReader.getData(dataFileReader.nPreload);
            headers = dataFileReader.getHeaders();
            missingValueString = dataFileReader.missingValueString;

            dataParser.setRawData(rawData, headers, missingValueString);

            int nItems = timeColComboBox.getItemCount();
            int timeIndex = timeColComboBox.getSelectedIndex();
            int stageIndex = stageColComboBox.getSelectedIndex();

            timeColComboBox.setItems(headers);
            stageColComboBox.setItems(headers);

            if (nItems == headers.length) {
                timeColComboBox.setSelectedItem(timeIndex);
                stageColComboBox.setSelectedItem(stageIndex);
            }
        });

        RowColPanel actionPanel = new RowColPanel();
        actionPanel.setPadding(5);
        actionPanel.setGap(5);
        JButton validateButton = new JButton(T.text("import"));
        validateButton.setEnabled(false);
        validateButton.addActionListener((e) -> {
            String filePath = dataFileReader.getFilePath();
            String fileName = Path.of(filePath).getFileName().toString();

            dataParser.setRawData(dataFileReader.getData(), headers, fileName);

            int dateTimeColIndex = timeColComboBox.getSelectedIndex();
            LocalDateTime[] dateTimeVector = dataParser.getDateTimeCol(
                    dateTimeColIndex,
                    timeColFormatField.getText());
            List<double[]> stageData = new ArrayList<>();
            if (stageAllColumnsCheckBox.isSelected()) {
                for (int k = 0; k < rawData.size(); k++) {
                    if (k != dateTimeColIndex) {
                        stageData.add(dataParser.getDoubleCol(k));
                    }
                }
            } else {
                int stageColIndex = stageColComboBox.getSelectedIndex();
                stageData.add(dataParser.getDoubleCol(stageColIndex));
            }
            dataset = LimnigraphDataset.buildLimnigraphDataset(fileName, dateTimeVector, stageData);
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

        GridPanel columnMappingPanel = new GridPanel();
        columnMappingPanel.setPadding(5);
        columnMappingPanel.setGap(5);
        columnMappingPanel.setColWeight(1, 1);

        ChangeListener cbChangeListener = (chEvt) -> {
            if (rawData == null) {
                return;
            }
            dataParser.ignoreAll();

            int timeColIndex = timeColComboBox.getSelectedIndex();
            dataParser.setAsDateTimeCol(timeColIndex, timeColFormatField.getText());
            boolean isTimeVectorValid = dataParser.testColValidity(timeColIndex);

            boolean areStageVectorsValid = true;
            if (stageAllColumnsCheckBox.isSelected()) {
                for (int k = 0; k < rawData.size(); k++) {
                    if (k == timeColIndex)
                        continue;
                    dataParser.setAsDoubleCol(k);
                    if (!dataParser.testColValidity(k)) {
                        areStageVectorsValid = false;
                    }
                }
            } else {
                int stageColIndex = stageColComboBox.getSelectedIndex();
                dataParser.setAsDoubleCol(stageColIndex);
                areStageVectorsValid = dataParser.testColValidity(stageColIndex);
            }

            dataParser.setRawData(rawData, headers, missingValueString);

            validateButton.setEnabled(isTimeVectorValid && areStageVectorsValid);

        };

        timeColComboBox.addChangeListener(cbChangeListener);
        timeColFormatField.addChangeListener(cbChangeListener);
        stageAllColumnsCheckBox.addChangeListener(cbChangeListener);
        stageColComboBox.addChangeListener(cbChangeListener);

        JLabel timeColMappingLabel = new JLabel(T.text("date_time_column"));
        columnMappingPanel.insertChild(timeColMappingLabel, 0, 0);
        columnMappingPanel.insertChild(timeColComboBox, 1, 0);

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

        columnMappingPanel.insertChild(timeColFormatLabel, 0, 1);
        columnMappingPanel.insertChild(timeColFormatField, 1, 1);
        columnMappingPanel.insertChild(timeFormatDetails, 1, 2, 2, 1);

        columnMappingPanel.insertChild(stageAllColumnsCheckBox, 0, 3, 2, 1);
        JLabel stageColLabel = new JLabel(T.text("stage_level_column"));
        columnMappingPanel.insertChild(stageColLabel, 0, 4);
        columnMappingPanel.insertChild(stageColComboBox, 1, 4);

        appendChild(dataFileReader, 0);
        appendChild(dataPreviewPanel, 1);
        appendChild(new JSeparator(), 0);
        appendChild(columnMappingPanel, 0);
        appendChild(new JSeparator(), 0);
        appendChild(actionPanel, 0);

    }

    public void showDialog() {

        dialog = new JDialog(AppConfig.AC.APP_MAIN_FRAME, true);
        dialog.setContentPane(this);

        dialog.setTitle(T.text("import_limnigraph"));
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.setPreferredSize(new Dimension(900, 600));

        dialog.pack();
        dialog.setLocationRelativeTo(AppConfig.AC.APP_MAIN_FRAME);
        dialog.setVisible(true);
        dialog.dispose();
    }

    public LimnigraphDataset getDataset() {
        return dataset;
    }

}
