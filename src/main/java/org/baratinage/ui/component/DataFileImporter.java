package org.baratinage.ui.component;

import java.awt.Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;

import org.baratinage.App;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ReadFile;

public class DataFileImporter extends RowColPanel {

    public class ImportSettings {
        public String filePath = null;
        public String sep = "\t";
        public int nRowSkip = 0;
        public int nRowMax = 30;
        public int nColMax = Integer.MAX_VALUE;
        public String missingValueString = "";
        public boolean hasHeaderRow = true;

        @Override
        public String toString() {
            String str = "Import Settings: \n";
            str += "filePath: '" + filePath + "'\n";
            str += "sep:                '" + sep + "'\n";
            str += "nRowSkip:           '" + nRowSkip + "'\n";
            str += "nRowMax:            '" + nRowMax + "'\n";
            str += "nColMax:            '" + nColMax + "'\n";
            str += "missingValueString: '" + missingValueString + "'\n";
            str += "hasHeaderRow:       '" + hasHeaderRow + "'";
            return str;
        }
    }

    private JLabel selectedFilePathLabel;
    private ImportPreviewTable dataTable;
    private ImportSettings importSettings;
    private String[] fileLines;
    private List<String[]> dataString;
    private String[] headers;

    public DataFileImporter() {
        super(AXIS.COL);
        setGap(5);

        importSettings = new ImportSettings();

        appendChild(getImportFilePanel(), 0);
        appendChild(new JSeparator(), 0);
        appendChild(getParsingSettingsPanel(), 0);
        appendChild(new JSeparator(), 0);
        appendChild(getPreviewTablePanel(), 1);

    }

    public List<String[]> getPreviewData() {
        return dataString;
    }

    public String[] getHeaders() {
        return headers;
    }

    public ImportedDataset getDataset() {
        if (fileLines == null) {
            return null;
        }
        String name = Path.of(importSettings.filePath).getFileName().toString();
        try {
            List<double[]> data = ReadFile.readMatrix(
                    importSettings.filePath,
                    importSettings.sep,
                    importSettings.nRowSkip + (importSettings.hasHeaderRow ? 1 : 0),
                    Integer.MAX_VALUE,
                    importSettings.missingValueString,
                    true, true);
            ImportedDataset dataset = new ImportedDataset(
                    name, data, headers);
            return dataset;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GridPanel getImportFilePanel() {
        GridPanel importFilePanel = new GridPanel();
        importFilePanel.setPadding(5);
        importFilePanel.setGap(5);
        importFilePanel.setColWeight(1, 1);
        // importFilePanel.setRowWeight(2, 10000);
        JLabel explainLabel = new JLabel("Selectionner un fichier à importer");
        selectedFilePathLabel = new JLabel();
        selectedFilePathLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JButton browseFileSystemButon = new JButton("Parcourir...");
        browseFileSystemButon.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            // fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setCurrentDirectory(new File(App.TEST_DIR));
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
                    return "Fichier text de données (.txt, .csv, .dat, .bad)";
                }

            });
            fileChooser.setDialogTitle("Selectionner un fichier text...");
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                readFile(fileChooser.getSelectedFile().getAbsolutePath());
            }

        });

        importFilePanel.insertChild(explainLabel, 0, 0, 2, 1);
        importFilePanel.insertChild(browseFileSystemButon, 0, 1);
        importFilePanel.insertChild(selectedFilePathLabel, 1, 1);

        return importFilePanel;
    }

    private GridPanel getParsingSettingsPanel() {
        GridPanel panel = new GridPanel();
        panel.setColWeight(1, 1);
        panel.setGap(5, 5);
        panel.setPadding(5);

        JLabel separatorLabel = new JLabel("Séparateur: ");
        // FIXME: need refactorization to account for default value!
        RowColPanel separatorOptions = getSeparatorOptions((txt) -> {
            importSettings.sep = txt;
            onImportSettingsChange();
        });

        panel.insertChild(separatorLabel, 0, 0);
        panel.insertChild(separatorOptions, 1, 0);

        JCheckBox hasHeaderCheckBox = new JCheckBox("Présence d'en-têtes (noms des colonnes)");
        hasHeaderCheckBox.setSelected(importSettings.hasHeaderRow);
        hasHeaderCheckBox.addActionListener((e) -> {
            importSettings.hasHeaderRow = hasHeaderCheckBox.isSelected();
            onImportSettingsChange();
        });

        panel.insertChild(hasHeaderCheckBox, 0, 1, 2, 1);

        JLabel nSkipRowLabel = new JLabel("Nombre de ligne à ignorer: ");
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        JSpinner nSkipRowField = new JSpinner(spinnerModel);
        nSkipRowField.addChangeListener((e) -> {
            importSettings.nRowSkip = (int) nSkipRowField.getValue();
            onImportSettingsChange();
        });

        panel.insertChild(nSkipRowLabel, 0, 2);
        panel.insertChild(nSkipRowField, 1, 2);

        JLabel missingValueCodeLabel = new JLabel("Code de valeur manquante: ");
        TextField missingValueCodeField = new TextField();
        missingValueCodeField.setText(importSettings.missingValueString);
        missingValueCodeField.addChangeListener((txt) -> {
            importSettings.missingValueString = txt;
            onImportSettingsChange();
        });

        panel.insertChild(missingValueCodeLabel, 0, 3);
        panel.insertChild(missingValueCodeField, 1, 3);

        // panel.insertChild(nPreloadLabel, 0, 4);
        // panel.insertChild(nPreloadPanel, 1, 4);

        return panel;
    }

    private RowColPanel getPreviewTablePanel() {
        RowColPanel panel = new RowColPanel(AXIS.COL);
        panel.setPadding(5);
        panel.setGap(5);
        JLabel label = new JLabel("Aperçu des données");
        dataTable = new ImportPreviewTable();

        JLabel nPreloadLabel = new JLabel("Nombre de lignes à pré-charger: ");
        RowColPanel nPreloadPanel = new RowColPanel();
        nPreloadPanel.setGap(5);
        SpinnerNumberModel nPreloadSpinnerModel = new SpinnerNumberModel(importSettings.nRowMax, 10,
                Integer.MAX_VALUE,
                1);
        JSpinner nPreloadSpinner = new JSpinner(nPreloadSpinnerModel);
        JButton nPreloadButton = new JButton("Appliquer");
        nPreloadButton.addActionListener((e) -> {
            importSettings.nRowMax = (int) nPreloadSpinner.getValue();
            readFile(importSettings.filePath);
        });
        nPreloadPanel.appendChild(nPreloadLabel, 0);
        nPreloadPanel.appendChild(nPreloadSpinner, 1);
        nPreloadPanel.appendChild(nPreloadButton, 0);

        panel.appendChild(label, 0);
        panel.appendChild(dataTable, 1);
        panel.appendChild(nPreloadPanel, 0);
        return panel;
    }

    private void readFile(String filePath) {

        try {
            fileLines = ReadFile.getLines(
                    filePath,
                    importSettings.nRowSkip + importSettings.nRowMax,
                    true);
        } catch (IOException e) {
            System.err.println("ERROR while trying to read file!");
        }
        importSettings.filePath = filePath;
        String displayPath = filePath;
        try {
            displayPath = (new File(filePath)).getCanonicalPath();
        } catch (IOException e) {
        }
        selectedFilePathLabel.setText(displayPath);
        selectedFilePathLabel.setToolTipText(displayPath);
        onImportSettingsChange();
    }

    private void onImportSettingsChange() {

        if (fileLines == null || fileLines.length <= importSettings.nRowSkip) {
            return;
        }
        dataString = ReadFile.linesToStringMatrix(
                fileLines,
                importSettings.sep,
                importSettings.nRowSkip,
                importSettings.nRowMax,
                true);

        int nCol = dataString.size();
        headers = new String[nCol];
        if (importSettings.hasHeaderRow) {
            headers = ReadFile.getStringRow(dataString, importSettings.nRowSkip);
            dataString = ReadFile.getSubStringMatrix(dataString, 1);
        } else {
            for (int k = 0; k < nCol; k++) {
                headers[k] = "col #" + (k + 1);
            }
        }

        dataTable.set(
                dataString,
                headers,
                importSettings.missingValueString);

        firePropertyChange("changed", null, null);
    }

    @FunctionalInterface
    private interface IOptionChange {
        public void onOptionChange(String option);
    }

    // FIXME: refactor, generalize, extract!
    private RowColPanel getSeparatorOptions(IOptionChange l) {
        RowColPanel panel = new RowColPanel();

        JRadioButton tabOption = new JRadioButton("Tabulation (\\t)");
        JRadioButton scOption = new JRadioButton("Point virgule (;)");
        JRadioButton commaOption = new JRadioButton("Virgule (,)");
        JRadioButton sapaceOption = new JRadioButton("Espace ( )");

        ButtonGroup options = new ButtonGroup();
        options.add(tabOption);
        options.add(scOption);
        options.add(commaOption);
        options.add(sapaceOption);
        tabOption.setSelected(true);

        tabOption.addActionListener((e) -> l.onOptionChange("\t"));
        scOption.addActionListener((e) -> l.onOptionChange(";"));
        commaOption.addActionListener((e) -> l.onOptionChange(","));
        sapaceOption.addActionListener((e) -> l.onOptionChange(" "));

        panel.appendChild(tabOption);
        panel.appendChild(scOption);
        panel.appendChild(commaOption);
        panel.appendChild(sapaceOption);
        return panel;
    }

}
