package org.baratinage.ui.component;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;

import org.baratinage.utils.ReadFile;

public class DataFileReader extends RowColPanel {
    public String filePath = null;
    public String sep = "\t";
    public int nRowSkip = 0;
    public String missingValueString = "";
    public boolean hasHeaderRow = true;
    public int nPreload = 15;

    private JLabel selectedFilePathLabel;
    private String[] fileLines;

    public DataFileReader() {
        super(AXIS.COL);

        // **********************************************************
        // file selection section

        GridPanel importFilePanel = new GridPanel();
        importFilePanel.setPadding(5);
        importFilePanel.setGap(5);
        importFilePanel.setColWeight(1, 1);
        // importFilePanel.setRowWeight(2, 10000);

        JLabel explainLabel = new JLabel();
        explainLabel.setText(T.text("select_file_to_import"));

        selectedFilePathLabel = new JLabel();
        selectedFilePathLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        JButton browseFileSystemButon = new JButton();
        browseFileSystemButon.setText(T.text("browse"));

        browseFileSystemButon.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(AppConfig.AC.lastUsedDir));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    boolean dir = f.isDirectory();
                    boolean txt = f.getName().endsWith(".txt");
                    boolean csv = f.getName().endsWith(".csv");
                    boolean bad = f.getName().endsWith(".bad");
                    boolean dat = f.getName().endsWith(".dat");
                    return dir || txt || csv || bad || dat;
                }

                @Override
                public String getDescription() {
                    return T.text("data_text_file") + " (.txt, .csv, .dat, .bad)";
                }

            });
            fileChooser.setDialogTitle(T.text("select_data_text_file"));
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                readFilesLines(fileChooser.getSelectedFile().getAbsolutePath());
                fireChangeListeners();
            }

        });

        importFilePanel.insertChild(explainLabel, 0, 0, 2, 1);
        importFilePanel.insertChild(browseFileSystemButon, 0, 1);
        importFilePanel.insertChild(selectedFilePathLabel, 1, 1);

        // **********************************************************
        // import settings section

        JLabel separatorLabel = new JLabel(T.text("column_separator"));

        SimpleRadioButtons<String> sepRatioButtons = new SimpleRadioButtons<>();

        sepRatioButtons.addChangeListener((chEvt) -> {
            sep = sepRatioButtons.getSelectedValue();
            fireChangeListeners();
        });
        JRadioButton tabOptBtn = sepRatioButtons.addOption("tab", T.text("sep_tab"), "\t");
        JRadioButton semicolOptBtn = sepRatioButtons.addOption("semicolon", T.text("sep_semicolon"), ";");
        JRadioButton commaOptBtn = sepRatioButtons.addOption("comma", T.text("sep_comma"), ",");
        JRadioButton spaceOptBtn = sepRatioButtons.addOption("space", T.text("sep_space"), " ");

        sepRatioButtons.setSelected("tab");

        RowColPanel sepOptionButtons = new RowColPanel(AXIS.ROW, ALIGN.START);
        sepOptionButtons.setGap(5);
        sepOptionButtons.appendChild(tabOptBtn);
        sepOptionButtons.appendChild(semicolOptBtn);
        sepOptionButtons.appendChild(commaOptBtn);
        sepOptionButtons.appendChild(spaceOptBtn);

        JCheckBox hasHeaderCheckBox = new JCheckBox(T.text("has_header_row"));
        hasHeaderCheckBox.setSelected(hasHeaderRow);
        hasHeaderCheckBox.addActionListener((e) -> {
            hasHeaderRow = hasHeaderCheckBox.isSelected();
            fireChangeListeners();
        });

        JLabel nSkipRowLabel = new JLabel(T.text("n_rows_to_skip"));
        SimpleIntegerField nSkipRowField = new SimpleIntegerField(0, Integer.MAX_VALUE, 1);
        nSkipRowField.setValue(0);
        nSkipRowField.addChangeListener((e) -> {
            nRowSkip = nSkipRowField.getIntValue();
            fireChangeListeners();
        });

        JLabel missingValueCodeLabel = new JLabel(T.text("missing_value_code"));
        SimpleTextField missingValueCodeField = new SimpleTextField();
        missingValueCodeField.setText(missingValueString);
        missingValueCodeField.addChangeListener((chEvt) -> {
            missingValueString = missingValueCodeField.getText();
            fireChangeListeners();
        });

        JLabel nRowPreloadLabel = new JLabel(T.text("n_rows_to_preload"));
        SimpleIntegerField nRowPreloadField = new SimpleIntegerField(10, Integer.MAX_VALUE, 1);
        nRowPreloadField.setValue(nPreload);
        nRowPreloadField.addChangeListener((e) -> {
            nPreload = nRowPreloadField.getIntValue();
            fireChangeListeners();
        });

        // **********************************************************
        // import settings panel

        GridPanel importSettingsPanel = new GridPanel();
        importSettingsPanel.setColWeight(1, 1);
        importSettingsPanel.setColWeight(3, 1);
        importSettingsPanel.setGap(5, 5);
        importSettingsPanel.setPadding(5);

        int rowIndex = 0;

        importSettingsPanel.insertChild(separatorLabel, 0, rowIndex);
        importSettingsPanel.insertChild(sepOptionButtons, 1, rowIndex, 3, 1);
        rowIndex++;

        importSettingsPanel.insertChild(hasHeaderCheckBox, 0, rowIndex, 2, 1);

        importSettingsPanel.insertChild(nSkipRowLabel, 2, rowIndex);
        importSettingsPanel.insertChild(nSkipRowField, 3, rowIndex);
        rowIndex++;

        importSettingsPanel.insertChild(missingValueCodeLabel, 0, rowIndex);
        importSettingsPanel.insertChild(missingValueCodeField, 1, rowIndex);

        importSettingsPanel.insertChild(nRowPreloadLabel, 2, rowIndex);
        importSettingsPanel.insertChild(nRowPreloadField, 3, rowIndex);
        rowIndex++;

        // **********************************************************
        // final panel

        appendChild(importFilePanel, 0);
        appendChild(new JSeparator(), 0);
        appendChild(importSettingsPanel, 0);

    }

    private void readFilesLines(String filePath) {

        try {
            fileLines = ReadFile.getLines(
                    filePath,
                    Integer.MAX_VALUE,
                    true);
        } catch (IOException e) {
            System.err.println("DataFileReader Error: Error while trying to read file!");
            return;
        }
        this.filePath = filePath;
        String displayPath = filePath;
        try {
            displayPath = (new File(filePath)).getCanonicalPath();
        } catch (IOException e) {
        }
        selectedFilePathLabel.setText(displayPath);
        selectedFilePathLabel.setToolTipText(displayPath);
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

        return dataString;
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
