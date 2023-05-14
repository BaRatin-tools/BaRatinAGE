package org.baratinage.ui.baratin.gaugings;

import java.awt.Component;
import java.awt.Dimension;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.util.ArrayList;
// import java.util.List;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.App;
import org.baratinage.ui.component.DataFileImporter;
import org.baratinage.ui.component.ImportedDataset;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class GaugingsImporter extends RowColPanel {

    private DefaultComboBoxModel<DataColumnInfo> hColumn;
    private DefaultComboBoxModel<DataColumnInfo> qColumn;
    private DefaultComboBoxModel<DataColumnInfo> uqColumn;

    JDialog dialog;

    private GaugingsDataset dataset;

    public GaugingsImporter() {
        super(AXIS.COL);

        hColumn = new DefaultComboBoxModel<>();
        qColumn = new DefaultComboBoxModel<>();
        uqColumn = new DefaultComboBoxModel<>();

        // RowColPanel panel = new RowColPanel(RowColPanel.);
        dataset = null;

        DataFileImporter dataFileImporter = new DataFileImporter();

        RowColPanel actionPanel = new RowColPanel();
        actionPanel.setPadding(5);
        actionPanel.setGap(5);
        JButton validateButton = new JButton("Importer");
        validateButton.addActionListener((e) -> {
            ImportedDataset rawDataset = dataFileImporter.getDataset();
            int hIndex = hColumn.getIndexOf(hColumn.getSelectedItem());
            int qIndex = qColumn.getIndexOf(qColumn.getSelectedItem());
            int uqIndex = uqColumn.getIndexOf(uqColumn.getSelectedItem());
            List<double[]> data = new ArrayList<>();
            data.add(rawDataset.getColumn(hIndex));
            data.add(rawDataset.getColumn(qIndex));
            data.add(rawDataset.getColumn(uqIndex));
            String[] headers = new String[] {
                    "h", "Q", "uQ"
            };
            dataset = new GaugingsDataset(
                    rawDataset.getName(),
                    data,
                    headers);
            dialog.setVisible(false);

        });
        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener((e) -> {
            dialog.setVisible(false);
        });

        // JOptionPane
        actionPanel.appendChild(cancelButton, 0);
        actionPanel.appendChild(new Component() {
        }, 1);
        actionPanel.appendChild(validateButton, 0);

        dataFileImporter.addPropertyChangeListener("changed", (e) -> {
            hColumn.removeAllElements();
            qColumn.removeAllElements();
            uqColumn.removeAllElements();
            String[] headers = dataFileImporter.getHeaders();
            validateButton.setEnabled(false);
            if (headers != null && headers.length > 3) {
                for (int k = 0; k < headers.length; k++) {
                    hColumn.addElement(
                            new DataColumnInfo(headers[k], k));
                    qColumn.addElement(
                            new DataColumnInfo(headers[k], k));
                    uqColumn.addElement(
                            new DataColumnInfo(headers[k], k));
                }

                int hIndex = getHColGuess(headers, 0);
                int qIndex = getQColGuess(headers, 1);
                int uQIndex = getUQColGuess(headers, 2);

                hColumn.setSelectedItem(hColumn.getElementAt(hIndex));
                qColumn.setSelectedItem(qColumn.getElementAt(qIndex));
                uqColumn.setSelectedItem(uqColumn.getElementAt(uQIndex));

                validateButton.setEnabled(true);
            }

        });

        appendChild(dataFileImporter, 1);
        appendChild(new JSeparator(), 0);
        appendChild(getColumnMappingPanel(), 0);
        appendChild(new JSeparator(), 0);
        appendChild(actionPanel, 0);

    }

    public void showDialog() {

        dialog = new JDialog(App.MAIN_FRAME, true);
        dialog.setContentPane(this);

        dialog.setTitle("Importer un jeu de jaugeages");
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.setPreferredSize(new Dimension(900, 600));

        dialog.pack();
        dialog.setLocationRelativeTo(App.MAIN_FRAME);
        dialog.setVisible(true);
        dialog.dispose();
    }

    public GaugingsDataset getDataset() {
        return dataset;
    }

    private int getColumnGuess(String[][] keywords, String[] headers, int defaultIndex) {

        for (int k = 0; k < headers.length; k++) {
            String h = headers[k].toLowerCase();
            for (String[] keywordGroup : keywords) {
                boolean valid = true;
                for (String keyword : keywordGroup) {
                    if (!h.contains(keyword)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    return k;
                }
            }
        }
        return defaultIndex;
    }

    private int getHColGuess(String[] headers, int defaultIndex) {
        String[][] keywords = new String[][] {
                new String[] { "h" }
        };
        return getColumnGuess(keywords, headers, defaultIndex);
    }

    private int getQColGuess(String[] headers, int defaultIndex) {
        String[][] keywords = new String[][] {
                new String[] { "q" },
        };
        return getColumnGuess(keywords, headers, defaultIndex);
    }

    private int getUQColGuess(String[] headers, int defaultIndex) {
        String[][] keywords = new String[][] {
                new String[] { "u", "q" },
        };
        return getColumnGuess(keywords, headers, defaultIndex);
    }

    private record DataColumnInfo(String name, int index) {
        @Override
        public String toString() {
            return name;
        }
    }

    private GridPanel getColumnMappingPanel() {
        GridPanel panel = new GridPanel();
        panel.setColWeight(1, 1);
        panel.setPadding(5);
        panel.setGap(5);

        JLabel hLabel = new JLabel("Colonne des hauteurs d'eau: ");
        JComboBox<DataColumnInfo> hComboBox = new JComboBox<>(hColumn);

        panel.insertChild(hLabel, 0, 0);
        panel.insertChild(hComboBox, 1, 0);

        JLabel qLabel = new JLabel("Colonne des débits: ");
        JComboBox<DataColumnInfo> qComboBox = new JComboBox<>(qColumn);

        panel.insertChild(qLabel, 0, 1);
        panel.insertChild(qComboBox, 1, 1);

        JLabel uqLabel = new JLabel("Colonne des incertiudes de débits: ");
        JComboBox<DataColumnInfo> uqComboBox = new JComboBox<>(uqColumn);

        panel.insertChild(uqLabel, 0, 2);
        panel.insertChild(uqComboBox, 1, 2);

        return panel;
    }
}