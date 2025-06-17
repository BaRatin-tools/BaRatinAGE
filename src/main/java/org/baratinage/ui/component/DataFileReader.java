package org.baratinage.ui.component;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

public class DataFileReader extends SimpleFlowPanel {
    public File file = null;
    public String filePath = null;
    public String sep = ";";
    public int nRowSkip = 0;
    public String missingValueString = "";
    public boolean hasHeaderRow = true;

    private CommonDialog.CustomFileFilter[] fileFilters = new CommonDialog.CustomFileFilter[0];

    // public int nPreload = 15;

    private JLabel selectedFilePathLabel;
    private String[] fileLines;

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

        SimpleComboBox colSepChooser = new SimpleComboBox();
        colSepChooser.setEmptyItem(null);
        HashMap<String, String> colSepOptions = new HashMap<>();
        colSepOptions.put("sep_tab", "\\t");
        colSepOptions.put("sep_semicolon", ";");
        colSepOptions.put("sep_comma", ",");
        colSepOptions.put("sep_space", " ");
        String[] colSepOptionsKeys = new String[] {
                "sep_tab",
                "sep_semicolon",
                "sep_comma",
                "sep_space" };

        colSepChooser.setItems(colSepOptionsKeys);
        colSepChooser.setSelectedItem(1);
        colSepChooser.addChangeListener(l -> {
            int index = colSepChooser.getSelectedIndex();
            if (index >= 0 && index < colSepOptionsKeys.length) {
                sep = colSepOptions.get(colSepOptionsKeys[index]);
                fireChangeListeners();
            }
        });

        JCheckBox hasHeaderCheckBox = new JCheckBox("has_header_row");
        hasHeaderCheckBox.setSelected(hasHeaderRow);
        hasHeaderCheckBox.addActionListener((e) -> {
            hasHeaderRow = hasHeaderCheckBox.isSelected();
            fireChangeListeners();
        });

        JLabel nSkipRowLabel = new JLabel("n_rows_to_skip");
        SimpleIntegerField nSkipRowField = new SimpleIntegerField(0, Integer.MAX_VALUE, 1);
        nSkipRowField.setValue(0);
        nSkipRowField.addChangeListener((e) -> {
            nRowSkip = nSkipRowField.getIntValue();
            fireChangeListeners();
        });

        JLabel missingValueCodeLabel = new JLabel("missing_value_code");
        SimpleTextField missingValueCodeField = new SimpleTextField();
        missingValueCodeField.setText(missingValueString);
        missingValueCodeField.addChangeListener((chEvt) -> {
            missingValueString = missingValueCodeField.getText();
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

        T.t(this, explainLabel, false, "select_file_to_import");
        T.t(this, browseFileSystemButon, false, "browse");
        T.t(this, separatorLabel, false, "column_separator");

        T.t(this, () -> {
            String[] colSepOptionsLabels = new String[colSepOptionsKeys.length];
            for (int k = 0; k < colSepOptionsKeys.length; k++) {
                colSepOptionsLabels[k] = T.text(colSepOptionsKeys[k]);
            }
            int index = colSepChooser.getSelectedIndex();
            colSepChooser.setItems(colSepOptionsLabels);
            colSepChooser.setSelectedItem(index);
        });
        T.t(this, hasHeaderCheckBox, false, "has_header_row");
        T.t(this, nSkipRowLabel, false, "n_rows_to_skip");
        T.t(this, missingValueCodeLabel, false, "missing_value_code");

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
        this.filePath = filePath;
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

    public List<String[]> getData(int nRows) {

        if (fileLines == null || fileLines.length <= nRowSkip) {
            List<String[]> dataString = new ArrayList<>();
            dataString.add(new String[0]);
            return dataString;
        }

        List<String[]> dataString = ReadFile.linesToStringMatrix(
                fileLines,
                sep,
                nRowSkip + (hasHeaderRow ? 1 : 0),
                nRows,
                true);

        boolean hasSkippedRows = ReadFile.didLastReadSkipRows(false);

        skippedIndices.clear();
        if (hasSkippedRows) {
            skippedIndices.addAll(ReadFile.getLastSkippedIndices());
        }

        return dataString;
    }

    private final Set<Integer> skippedIndices = new HashSet<>();

    public Set<Integer> getLastSkippedIndices() {
        return skippedIndices;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<String[]> getData() {
        return getData(Integer.MAX_VALUE);
    }

    public String[] getHeaders() {

        if (fileLines == null || fileLines.length <= nRowSkip) {
            return new String[0];
        }

        List<String[]> dataString = ReadFile.linesToStringMatrix(
                fileLines,
                sep,
                nRowSkip,
                1,
                true);

        int nCol = dataString.size();
        String[] headers = new String[nCol];
        if (hasHeaderRow) {
            headers = ReadFile.getStringRow(dataString, 0);
        } else {
            for (int k = 0; k < nCol; k++) {
                headers[k] = "col #" + (k + 1);
            }
        }

        return headers;
    }

    public String getMissingValue() {
        return missingValueString;
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        if (fileLines == null) {
            return;
        }
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

}
