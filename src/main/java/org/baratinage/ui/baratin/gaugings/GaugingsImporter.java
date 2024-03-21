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

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;
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
    }

    private List<String[]> rawData;
    private String[] headers;
    private String missingValueString;

    private JDialog dialog;
    private RowColPanel dataPreviewPanel;
    private GaugingsDataset dataset;

    private final DataParser dataParser;
    private final ColsMapping columnsMapping;
    private final JButton validateButton;

    private final String ID;

    public GaugingsImporter() {
        super(AXIS.COL);

        ID = Misc.getTimeStampedId();

        columnsMapping = new ColsMapping();

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

            int nItems = columnsMapping.hCol.getItemCount();
            int hIndex = columnsMapping.hCol.getSelectedIndex();
            int qIndex = columnsMapping.qCol.getSelectedIndex();
            int uqIndex = columnsMapping.uqCol.getSelectedIndex();

            columnsMapping.resetHeaders(headers);

            columnsMapping.guessIndices();
            if (nItems == headers.length) {
                if (hIndex != -1) {
                    columnsMapping.hCol.setSelectedItem(hIndex);
                }
                if (qIndex != -1) {
                    columnsMapping.qCol.setSelectedItem(qIndex);
                }
                if (uqIndex != -1) {
                    columnsMapping.uqCol.setSelectedItem(uqIndex);
                }
            }
        });

        RowColPanel actionPanel = new RowColPanel();
        actionPanel.setPadding(5);
        actionPanel.setGap(5);

        validateButton = new JButton("import");
        validateButton.addActionListener((e) -> {
            String filePath = dataFileReader.getFilePath();
            if (filePath != null) { // FIXME: validateButton should be disabled if import is invalid
                String fileName = Path.of(filePath).getFileName().toString();
                // necessary to read all the data!
                rawData = dataFileReader.getData();
                dataParser.setRawData(rawData, headers, missingValueString);

                dataset = new GaugingsDataset(fileName,
                        dataParser.getDoubleCol(columnsMapping.hCol.getSelectedIndex()),
                        dataParser.getDoubleCol(columnsMapping.qCol.getSelectedIndex()),
                        dataParser.getDoubleCol(columnsMapping.uqCol.getSelectedIndex()));
            }
            dialog.setVisible(false);
        });

        JButton cancelButton = new JButton("cancel");
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
            TimedActions.debounce(ID, AppSetup.CONFIG.DEBOUNCED_DELAY_MS, this::updateValidityStatus);
        };

        columnsMapping.setChangeListener(cbChangeListener);

        int rowIndex = 0;

        JLabel hColMapLabel = new JLabel("stage_level");
        columnMappingPanel.insertChild(hColMapLabel, 0, rowIndex);
        columnMappingPanel.insertChild(columnsMapping.hCol, 1, rowIndex);
        rowIndex++;

        JLabel qColMapLabel = new JLabel("discharge");
        columnMappingPanel.insertChild(qColMapLabel, 0, rowIndex);
        columnMappingPanel.insertChild(columnsMapping.qCol, 1, rowIndex);
        rowIndex++;

        JLabel uqColMapLabel = new JLabel("discharge_uncertainty_percent");
        columnMappingPanel.insertChild(uqColMapLabel, 0, rowIndex);
        columnMappingPanel.insertChild(columnsMapping.uqCol, 1, rowIndex);
        // rowIndex++;

        appendChild(dataFileReader, 0);
        appendChild(dataPreviewPanel, 1);
        appendChild(new JSeparator(), 0);
        appendChild(columnMappingPanel, 0);
        appendChild(new JSeparator(), 0);
        appendChild(actionPanel, 0);

        T.t(this, validateButton, false, "import");
        T.t(this, cancelButton, false, "cancel");
        T.t(this, hColMapLabel, false, "stage_level");
        T.t(this, qColMapLabel, false, "discharge");
        T.t(this, uqColMapLabel, false, "discharge_uncertainty_percent");
    }

    private void updateValidityStatus() {
        // if there's not data, not point checking validity
        if (rawData == null) {
            columnsMapping.hCol.setValidityView(false);
            columnsMapping.qCol.setValidityView(false);
            columnsMapping.uqCol.setValidityView(false);
            return;
        }
        dataParser.ignoreAll();

        // stage
        int hColIndex = columnsMapping.hCol.getSelectedIndex();
        dataParser.setAsDoubleCol(hColIndex);
        boolean hColOk = hColIndex >= 0 && dataParser.testColValidity(hColIndex);

        // discharge
        int qColIndex = columnsMapping.qCol.getSelectedIndex();
        dataParser.setAsDoubleCol(qColIndex);
        boolean qColOk = qColIndex >= 0 && dataParser.testColValidity(qColIndex);

        // discharge
        int uqColIndex = columnsMapping.uqCol.getSelectedIndex();
        dataParser.setAsDoubleCol(uqColIndex);
        boolean uqColOk = uqColIndex >= 0 && dataParser.testColValidity(uqColIndex);

        // update ui
        dataParser.updateColumnTypes();
        columnsMapping.hCol.setValidityView(hColOk);
        columnsMapping.qCol.setValidityView(qColOk);
        columnsMapping.uqCol.setValidityView(uqColOk);
        validateButton.setEnabled(hColOk && qColOk && uqColOk);
    }

    public void showDialog() {

        dialog = new JDialog(AppSetup.MAIN_FRAME, true);
        dialog.setContentPane(this);

        dialog.setTitle(T.text("import_gauging_set"));
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.setPreferredSize(new Dimension(900, 600));

        dialog.pack();
        dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        dialog.setVisible(true);
        dialog.dispose();
    }

    public GaugingsDataset getDataset() {
        return dataset;
    }

}
