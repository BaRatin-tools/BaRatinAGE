package org.baratinage.ui.component;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.translation.T;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.json.JSONObject;

public class DataFileReader extends SimpleFlowPanel {

    private File file = null;

    private final LinkedHashMap<String, String> colSepOptions = new LinkedHashMap<>();

    private final SimpleComboBox colSepChooser;
    private final JCheckBox hasHeaderCheckBox;
    private final SimpleIntegerField nSkipRowField;
    private final SimpleTextField missingValueCodeField;

    private CommonDialog.CustomFileFilter[] fileFilters = new CommonDialog.CustomFileFilter[0];

    private JLabel selectedFilePathLabel;
    private String[] fileLines;

    private List<String[]> parsedData = new ArrayList<>();
    private String[] parsedHeaders = new String[0];

    public DataFileReader() {
        super(true);

        // **********************************************************
        // file selection section

        SimpleFlowPanel importFilePanel = new SimpleFlowPanel();
        importFilePanel.setPadding(5);
        importFilePanel.setGap(5);

        JLabel explainLabel = new JLabel();
        explainLabel.setText("select_file_to_import");

        selectedFilePathLabel = new JLabel();
        selectedFilePathLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JButton browseFileSystemButon = new JButton();
        browseFileSystemButon.setText("browse");

        browseFileSystemButon.addActionListener(e -> {
            file = CommonDialog.openFileDialog(
                    T.text("select_data_text_file"),
                    fileFilters);

            if (file == null) {
                ConsoleLogger.error("selected file is null.");
                return;
            }
            readFilesLines(file.getAbsolutePath());
            fireChangeListeners();
        });

        importFilePanel.addChild(explainLabel, false);
        importFilePanel.addChild(browseFileSystemButon, false);
        importFilePanel.addChild(selectedFilePathLabel, true);

        // **********************************************************
        // import settings section

        JLabel separatorLabel = new JLabel("column_separator");

        colSepOptions.put("sep_tab", "\\t");
        colSepOptions.put("sep_semicolon", ";");
        colSepOptions.put("sep_comma", ",");
        colSepOptions.put("sep_space", " ");

        colSepChooser = new SimpleComboBox();
        colSepChooser.setEmptyItem(null);

        colSepChooser.setItems(colSepOptions
                .keySet()
                .stream()
                .map(key -> T.text(key))
                .toList()
                .toArray(new String[] {}));
        colSepChooser.setSelectedItem(1);
        colSepChooser.addChangeListener(l -> {
            fireChangeListeners();
        });

        hasHeaderCheckBox = new JCheckBox("has_header_row");
        hasHeaderCheckBox.setSelected(true);
        hasHeaderCheckBox.addActionListener((e) -> {
            fireChangeListeners();
        });

        JLabel nSkipRowLabel = new JLabel("n_rows_to_skip");
        nSkipRowField = new SimpleIntegerField(0, Integer.MAX_VALUE, 1);
        nSkipRowField.setValue(0);
        nSkipRowField.addChangeListener((e) -> {
            fireChangeListeners();
        });

        JLabel missingValueCodeLabel = new JLabel("missing_value_code");
        missingValueCodeField = new SimpleTextField();
        missingValueCodeField.setText("");
        missingValueCodeField.addChangeListener((chEvt) -> {
            fireChangeListeners();
        });

        // **********************************************************
        // import settings panel

        SimpleFlowPanel importSettingsPanel = new SimpleFlowPanel();
        importSettingsPanel.setGap(5);
        importSettingsPanel.setPadding(5);

        SimpleFlowPanel importSettingsLeftPanel = new SimpleFlowPanel(true);
        importSettingsLeftPanel.setGap(5);
        importSettingsPanel.addChild(importSettingsLeftPanel, true);

        SimpleFlowPanel importSettingsRightPanel = new SimpleFlowPanel(true);
        importSettingsRightPanel.setGap(5);
        importSettingsPanel.addChild(importSettingsRightPanel, true);

        SimpleFlowPanel colSepPanel = new SimpleFlowPanel();
        colSepPanel.setGap(5);
        colSepPanel.addChild(separatorLabel, false);
        colSepPanel.addChild(colSepChooser, true);
        importSettingsLeftPanel.addChild(colSepPanel, false);

        SimpleFlowPanel missingValueCodePanel = new SimpleFlowPanel();
        missingValueCodePanel.setGap(5);
        missingValueCodePanel.addChild(missingValueCodeLabel, false);
        missingValueCodePanel.addChild(missingValueCodeField, true);
        importSettingsLeftPanel.addChild(missingValueCodePanel, false);

        SimpleFlowPanel nSkipRowPanel = new SimpleFlowPanel();
        nSkipRowPanel.setGap(5);
        nSkipRowPanel.addChild(nSkipRowLabel, false);
        nSkipRowPanel.addChild(nSkipRowField, true);
        importSettingsRightPanel.addChild(nSkipRowPanel, false);

        importSettingsRightPanel.addChild(hasHeaderCheckBox, false);

        // **********************************************************
        // final panel

        addChild(importFilePanel, false);
        addChild(new SimpleSep(), false);
        addChild(importSettingsPanel, true);

        // FIXME: no need of dynamic i18n here

        T.t(this, explainLabel, false, "select_file_to_import");
        T.t(this, browseFileSystemButon, false, "browse");
        T.t(this, separatorLabel, false, "column_separator");

        T.t(this, () -> {
            String[] colSepOptionsLabels = getColSepOptionKeys();
            for (int k = 0; k < colSepOptions.size(); k++) {
                colSepOptionsLabels[k] = T.text(colSepOptionsLabels[k]);
            }
            int index = colSepChooser.getSelectedIndex();
            colSepChooser.setItems(colSepOptionsLabels);
            colSepChooser.setSelectedItem(index);
        });
        T.t(this, hasHeaderCheckBox, false, "has_header_row");
        T.t(this, nSkipRowLabel, false, "n_rows_to_skip");
        T.t(this, missingValueCodeLabel, false, "missing_value_code");

    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("sepIndex", colSepChooser.getSelectedIndex());
        json.put("nSkip", nSkipRowField.getIntValue());
        json.put("mvCode", missingValueCodeField.getText());
        json.put("hasHeaderRow", hasHeaderCheckBox.isSelected());
        return json;
    }

    public void fromJSON(JSONObject json) {
        if (json.has("sepIndex")) {
            colSepChooser.setSelectedItem(json.getInt("sepIndex"));
        }
        if (json.has("nSkip")) {
            nSkipRowField.setValue(json.getInt("nSkip"));
        }
        if (json.has("mvCode")) {
            missingValueCodeField.setText(json.getString("mvCode"));
        }
        if (json.has("hasHeaderRow")) {
            hasHeaderCheckBox.setSelected(json.getBoolean("hasHeaderRow"));
        }
    }

    private String[] getColSepOptionKeys() {
        return colSepOptions.keySet().stream().toList().toArray(new String[] {});
    }

    public String getSep() {

        int index = colSepChooser.getSelectedIndex();
        String[] colSepOptionsKeys = getColSepOptionKeys();
        if (index >= 0 && index < colSepOptionsKeys.length) {
            return colSepOptions.get(colSepOptionsKeys[index]);
        }
        return null;
    }

    public File getFile() {
        return file;
    }

    private void readFilesLines(String filePath) {

        try {
            fileLines = ReadFile.getLines(
                    filePath,
                    Integer.MAX_VALUE,
                    true);
        } catch (IOException e) {
            ConsoleLogger.error("Error while trying to read file!");
            return;
        }
        // this.filePath = filePath;
        String displayPath = filePath;
        try {
            displayPath = (new File(filePath)).getCanonicalPath();
        } catch (IOException e) {
        }
        if (displayPath.length() > 60) {
            String start = displayPath.substring(0, 15);
            String end = displayPath.substring(displayPath.length() - 35, displayPath.length());
            displayPath = String.format("%s ... %s", start, end);
        }
        selectedFilePathLabel.setText(displayPath);
        selectedFilePathLabel.setToolTipText(displayPath);
    }

    public void setFilters(CommonDialog.CustomFileFilter... fileFilters) {
        this.fileFilters = fileFilters;
    }

    private List<String[]> getData(int nRows) {
        return parsedData;
    }

    private final Set<Integer> skippedIndices = new HashSet<>();

    public Set<Integer> getLastSkippedIndices() {
        return skippedIndices;
    }

    public String getFilePath() {
        return file == null ? null : file.getAbsolutePath();
    }

    public List<String[]> getData() {
        return getData(Integer.MAX_VALUE);
    }

    public String[] getHeaders() {
        return parsedHeaders;
    }

    private void updateParsedData() {
        if (fileLines == null || fileLines.length <= nSkipRowField.getIntValue()) {
            // when there is no data to parse
            parsedHeaders = new String[0];
            List<String[]> dataString = new ArrayList<>();
            parsedData = dataString;
            return;
        }

        // headers
        List<String[]> headersDataString = ReadFile.linesToStringMatrix(
                fileLines,
                getSep(),
                nSkipRowField.getIntValue(),
                1,
                true);

        // data
        List<String[]> dataString = ReadFile.linesToStringMatrix(
                fileLines,
                getSep(),
                nSkipRowField.getIntValue() + (hasHeaderCheckBox.isSelected() ? 1 : 0),
                Integer.MAX_VALUE,
                true);

        // handle case where there is a mismatch
        if (headersDataString.size() != dataString.size()) {
            ConsoleLogger.error("Mismatch between header length and the number of column infered from first data row!");
            parsedData = new ArrayList<>();
            int nRowStart = nSkipRowField.getIntValue() + (hasHeaderCheckBox.isSelected() ? 1 : 0);
            int nRow = fileLines.length - nRowStart;
            String[] singleColumn = new String[nRow];
            for (int k = nRowStart; k < fileLines.length; k++) {
                singleColumn[k - nRowStart] = fileLines[k];
            }
            parsedData.add(singleColumn);
            String header = nRowStart - 1 >= 0 ? fileLines[nRowStart - 1] : "-";
            parsedHeaders = new String[] { header };
            return;
        }

        // update parsed headers
        int nCol = headersDataString.size();
        String[] headers = new String[nCol];
        if (hasHeaderCheckBox.isSelected()) {
            headers = ReadFile.getStringRow(headersDataString, 0);
        } else {
            for (int k = 0; k < nCol; k++) {
                headers[k] = "col #" + (k + 1);
            }
        }
        parsedHeaders = headers;

        // update parsed data
        parsedData = dataString;

        // update skipped rows set
        boolean hasSkippedRows = ReadFile.didLastReadSkipRows(false);
        skippedIndices.clear();
        if (hasSkippedRows) {
            skippedIndices.addAll(ReadFile.getLastSkippedIndices());
        }

    }

    public String getMissingValue() {
        return missingValueCodeField.getText();
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        updateParsedData();
        if (fileLines == null) {
            return;
        }
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

}
