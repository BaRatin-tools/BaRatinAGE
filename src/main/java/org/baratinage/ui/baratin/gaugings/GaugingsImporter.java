package org.baratinage.ui.baratin.gaugings;

import java.awt.Component;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.ui.AppConfig;
import org.baratinage.utils.Misc;

import org.baratinage.ui.component.DataFileReader;
import org.baratinage.ui.component.DataParser;
import org.baratinage.ui.component.SimpleComboBox;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class GaugingsImporter extends RowColPanel {

    private class ColsMapping {
        public final SimpleComboBox hCol = new SimpleComboBox();
        public final SimpleComboBox qCol = new SimpleComboBox();
        public final SimpleComboBox uqCol = new SimpleComboBox();
        public String[] headers;

        public void resetHeaders(String[] headers) {
            this.headers = headers;
            hCol.setItems(headers);
            qCol.setItems(headers);
            uqCol.setItems(headers);
        }

        public void guessIndices() {
            int hColIndexGuess = Misc.getIndexGuess(headers, -1,
                    "(.*\\bh\\b.*)|(.*stage.*)");
            hCol.setSelectedItem(hColIndexGuess);
            int qColIndexGuess = Misc.getIndexGuess(headers, -1,
                    "(.*\\bQ\\b.*)|(.*discharge.*)|(.*streamflow.*)");
            qCol.setSelectedItem(qColIndexGuess);
            int uqColIndexGuess = Misc.getIndexGuess(headers, -1,
                    "(.*\\buQ\\b.*)|(.*discharge.*uncertainty.*)|(.*streamflow.*uncertainty.*)");
            uqCol.setSelectedItem(uqColIndexGuess);
        }

        public void setChangeListener(ChangeListener l) {
            hCol.addChangeListener(l);
            qCol.addChangeListener(l);
            uqCol.addChangeListener(l);
        }

        public boolean areValid() {
            return hCol.getSelectedIndex() != -1 &&
                    qCol.getSelectedIndex() != -1 &&
                    uqCol.getSelectedIndex() != -1;
        }
    }

    private List<String[]> rawData;
    private String[] headers;
    private String missingValueString;

    private JDialog dialog;
    private RowColPanel dataPreviewPanel;
    private GaugingsDataset dataset;

    public GaugingsImporter() {
        super(AXIS.COL);

        ColsMapping columnsMapping = new ColsMapping();

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

            columnsMapping.resetHeaders(headers);

            columnsMapping.guessIndices();

        });

        RowColPanel actionPanel = new RowColPanel();
        actionPanel.setPadding(5);
        actionPanel.setGap(5);
        JButton validateButton = new JButton(T.text("import"));
        validateButton.addActionListener((e) -> {
            String filePath = dataFileReader.getFilePath();
            String fileName = Path.of(filePath).getFileName().toString();

            // necessary to read all the data!
            rawData = dataFileReader.getData();
            dataParser.setRawData(rawData, headers, missingValueString);

            dataset = GaugingsDataset.buildFromData(
                    fileName,
                    dataParser.getDoubleCol(columnsMapping.hCol.getSelectedIndex()),
                    dataParser.getDoubleCol(columnsMapping.qCol.getSelectedIndex()),
                    dataParser.getDoubleCol(columnsMapping.uqCol.getSelectedIndex()));

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
            dataParser.ignoreAll();

            dataParser.setAsDoubleCol(columnsMapping.hCol.getSelectedIndex());
            dataParser.setAsDoubleCol(columnsMapping.qCol.getSelectedIndex());
            dataParser.setAsDoubleCol(columnsMapping.uqCol.getSelectedIndex());

            dataParser.setRawData(rawData, headers, missingValueString);

            validateButton.setEnabled(columnsMapping.areValid());

        };

        columnsMapping.setChangeListener(cbChangeListener);

        int rowIndex = 0;

        // JLabel mappingLabel = new JLabel(T.text("columns_selection"));
        // columnMappingPanel.insertChild(mappingLabel, 0, rowIndex, 2, 1);
        // rowIndex++;

        JLabel hColMapLabel = new JLabel(T.text("stage_level"));
        columnMappingPanel.insertChild(hColMapLabel, 0, rowIndex);
        columnMappingPanel.insertChild(columnsMapping.hCol, 1, rowIndex);
        rowIndex++;

        JLabel qColMapLabel = new JLabel(T.text("discharge"));
        columnMappingPanel.insertChild(qColMapLabel, 0, rowIndex);
        columnMappingPanel.insertChild(columnsMapping.qCol, 1, rowIndex);
        rowIndex++;

        JLabel uqColMapLabel = new JLabel(T.text("discharge_uncertainty_percent"));
        columnMappingPanel.insertChild(uqColMapLabel, 0, rowIndex);
        columnMappingPanel.insertChild(columnsMapping.uqCol, 1, rowIndex);
        // rowIndex++;

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

        dialog.setTitle(T.text("import_gauging_set"));
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.setPreferredSize(new Dimension(900, 600));

        dialog.pack();
        dialog.setLocationRelativeTo(AppConfig.AC.APP_MAIN_FRAME);
        dialog.setVisible(true);
        dialog.dispose();
    }

    public GaugingsDataset getDataset() {
        return dataset;
    }

}
