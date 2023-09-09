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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
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
        Lg.register(explainLabel, "select_file_to_import");
        selectedFilePathLabel = new JLabel();
        selectedFilePathLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JButton browseFileSystemButon = new JButton();
        Lg.register(browseFileSystemButon, "browse");
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
                    return Lg.text("data_text_file") + " (.txt, .csv, .dat, .bad)";
                }

            });
            fileChooser.setDialogTitle(Lg.text("select_data_text_file"));
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

        GridPanel importSettingsPanel = new GridPanel();
        importSettingsPanel.setColWeight(1, 1);
        importSettingsPanel.setGap(5, 5);
        importSettingsPanel.setPadding(5);

        JLabel separatorLabel = new JLabel(Lg.text("separator"));

        RadioButtons separatorOptions = new RadioButtons();
        separatorOptions.addOption("\t", new JRadioButton(Lg.text("sep_tab")));
        separatorOptions.addOption(";", new JRadioButton(Lg.text("sep_semicolon")));
        separatorOptions.addOption(",", new JRadioButton(Lg.text("sep_comma")));
        separatorOptions.addOption(" ", new JRadioButton(Lg.text("sep_space")));
        separatorOptions.addChangeListener((chEvt) -> {
            sep = separatorOptions.getSelectedValue();
            fireChangeListeners();
        });
        separatorOptions.setSelectedValue("\t");

        importSettingsPanel.insertChild(separatorLabel, 0, 0);
        importSettingsPanel.insertChild(separatorOptions, 1, 0);

        JCheckBox hasHeaderCheckBox = new JCheckBox(Lg.text("has_header_row"));
        hasHeaderCheckBox.setSelected(hasHeaderRow);
        hasHeaderCheckBox.addActionListener((e) -> {
            hasHeaderRow = hasHeaderCheckBox.isSelected();
            fireChangeListeners();
        });

        importSettingsPanel.insertChild(hasHeaderCheckBox, 0, 1, 2, 1);

        JLabel nSkipRowLabel = new JLabel(Lg.text("n_rows_to_skip"));
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        JSpinner nSkipRowField = new JSpinner(spinnerModel);
        nSkipRowField.addChangeListener((e) -> {
            nRowSkip = (int) nSkipRowField.getValue();
            fireChangeListeners();
        });

        importSettingsPanel.insertChild(nSkipRowLabel, 0, 2);
        importSettingsPanel.insertChild(nSkipRowField, 1, 2);

        JLabel missingValueCodeLabel = new JLabel(Lg.text("missing_value_code"));
        SimpleTextField missingValueCodeField = new SimpleTextField();
        missingValueCodeField.setText(missingValueString);
        missingValueCodeField.addChangeListener((chEvt) -> {
            missingValueString = missingValueCodeField.getText();
            fireChangeListeners();
        });

        importSettingsPanel.insertChild(missingValueCodeLabel, 0, 3);
        importSettingsPanel.insertChild(missingValueCodeField, 1, 3);

        JLabel nRowPreloadLabel = new JLabel(Lg.text("n_rows_to_preload"));
        SpinnerNumberModel nRowPreloadSpinnerModel = new SpinnerNumberModel(nPreload, 10, Integer.MAX_VALUE, 1);
        JSpinner nRowPreloadField = new JSpinner(nRowPreloadSpinnerModel);
        nRowPreloadField.addChangeListener((e) -> {
            nPreload = (int) nRowPreloadField.getValue();
            fireChangeListeners();
        });

        importSettingsPanel.insertChild(nRowPreloadLabel, 0, 4);
        importSettingsPanel.insertChild(nRowPreloadField, 1, 4);

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
            System.err.println("ERROR while trying to read file!");
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
