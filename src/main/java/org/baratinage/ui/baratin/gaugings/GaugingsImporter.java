package org.baratinage.ui.baratin.gaugings;

import java.awt.Component;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.AppConfig;
import org.baratinage.utils.Misc;

import org.baratinage.ui.component.DataFileReader;
import org.baratinage.ui.component.DataParser;
import org.baratinage.ui.component.SimpleComboBox;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

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
        JButton validateButton = new JButton(Lg.text("import"));
        validateButton.addActionListener((e) -> {
            String filePath = dataFileReader.getFilePath();
            String fileName = Path.of(filePath).getFileName().toString();

            // necessary to read all the data!
            rawData = dataFileReader.getData();
            dataParser.setRawData(rawData, headers, missingValueString);

            List<double[]> data = new ArrayList<>();
            data.add(dataParser.getDoubleCol(columnsMapping.hCol.getSelectedIndex()));
            data.add(dataParser.getDoubleCol(columnsMapping.qCol.getSelectedIndex()));
            data.add(dataParser.getDoubleCol(columnsMapping.uqCol.getSelectedIndex()));

            dataset = new GaugingsDataset(
                    fileName,
                    data);
            dialog.setVisible(false);

        });
        JButton cancelButton = new JButton(Lg.text("cancel"));
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

            // columnsMapping.recordCurrentIndices();

            dataParser.setRawData(rawData, headers, missingValueString);

            validateButton.setEnabled(columnsMapping.areValid());

        };

        columnsMapping.setChangeListener(cbChangeListener);

        JLabel hColMapLabel = new JLabel(Lg.text("stage_level_column"));
        columnMappingPanel.insertChild(hColMapLabel, 0, 0);
        columnMappingPanel.insertChild(columnsMapping.hCol, 1, 0);

        JLabel qColMapLabel = new JLabel(Lg.text("discharge_column"));
        columnMappingPanel.insertChild(qColMapLabel, 0, 1);
        columnMappingPanel.insertChild(columnsMapping.qCol, 1, 1);

        JLabel uqColMapLabel = new JLabel(Lg.text("discharge_uncertainty_column"));
        columnMappingPanel.insertChild(uqColMapLabel, 0, 2);
        columnMappingPanel.insertChild(columnsMapping.uqCol, 1, 2);

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

        dialog.setTitle(Lg.text("import_gauging_set"));
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
