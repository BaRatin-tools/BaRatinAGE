package org.baratinage.ui.baratin;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.baratinage.App;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsImporter;
import org.baratinage.ui.baratin.gaugings.GaugingsPlot;
import org.baratinage.ui.baratin.gaugings.GaugingsTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.lg.LgElement;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.Misc;
import org.baratinage.ui.bam.BamItemType;
import org.json.JSONObject;

public class Gaugings extends BamItem implements ICalibrationData {

    private GaugingsTable gaugingsTable;
    private GaugingsDataset gaugingDataset;
    private RowColPanel plotPanel;
    private JLabel importedDataSetSourceLabel;

    public Gaugings(String uuid, BaratinProject project) {
        super(BamItemType.GAUGINGS, uuid, project);

        JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        content.setBorder(BorderFactory.createEmptyBorder());

        RowColPanel importGaugingsPanel = new RowColPanel(RowColPanel.AXIS.COL);
        importGaugingsPanel.setPadding(5);
        importGaugingsPanel.setGap(5);
        content.setLeftComponent(importGaugingsPanel);
        plotPanel = new RowColPanel();
        content.setRightComponent(plotPanel);

        importedDataSetSourceLabel = new JLabel();
        LgElement.registerLabel(importedDataSetSourceLabel, "ui", "empty_gauging_set");

        JButton importDataButton = new JButton();
        LgElement.registerButton(importDataButton, "ui", "import_gauging_set");
        importDataButton.addActionListener((e) -> {
            GaugingsImporter gaugingsImporter = new GaugingsImporter();
            gaugingsImporter.showDialog();
            GaugingsDataset newGaugingDataset = gaugingsImporter.getDataset();
            if (newGaugingDataset != null && newGaugingDataset.getNumberOfColumns() == 4) {
                gaugingDataset = newGaugingDataset;
                updateTable();
            }
        });

        gaugingsTable = new GaugingsTable();
        gaugingsTable.getTableModel().addTableModelListener((e) -> {
            setPlot();
            fireChangeListeners();
        });

        importGaugingsPanel.appendChild(importDataButton, 0);
        importGaugingsPanel.appendChild(importedDataSetSourceLabel, 0);
        importGaugingsPanel.appendChild(gaugingsTable, 1);

        setContent(content);
    }

    public void updateTable() {
        gaugingsTable.set(gaugingDataset);
        // String desc = String.format("Jeu de jaugeages importé depuis le fichier
        // '%s'",
        // gaugingDataset.getDatasetName());
        // importedDataSetSourceLabel.setText(desc);
        Lg.register(new LgElement<JLabel>(importedDataSetSourceLabel) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText("ui", "gauging_set_imported_from", true);
                text = Lg.format(text, gaugingDataset.getDatasetName());
                object.setText(text);
            }
        });
    }

    private void setPlot() {
        GaugingsPlot gaugingsPlot = new GaugingsPlot(
                "Hauteur d'eau",
                "Débit",
                true,
                gaugingDataset);

        PlotContainer plotContainer = new PlotContainer(gaugingsPlot);

        plotPanel.clear();
        plotPanel.appendChild(plotContainer);

    }

    public GaugingsDataset getGaugingDataset() {
        return gaugingDataset;
    }

    @Override
    public UncertainData[] getInputs() {
        if (gaugingDataset == null) {
            return null;
        }
        UncertainData[] inputs = new UncertainData[1];
        inputs[0] = new UncertainData("stage", gaugingDataset.getStageValues());
        return inputs;
    }

    @Override
    public UncertainData[] getOutputs() {
        if (gaugingDataset == null) {
            return null;
        }
        double[] q = gaugingDataset.getDischargeValues();
        double[] uq = gaugingDataset.getDischargeStdUncertainty();
        UncertainData[] outputs = new UncertainData[1];
        outputs[0] = new UncertainData("discharge", q, uq);
        return outputs;
    }

    @Override
    public CalibrationData getCalibrationData() {
        if (gaugingDataset == null) {
            return null;
        }
        int hashCode = gaugingDataset.hashCode();
        String sanitizedName = Misc.sanitizeName(bamItemNameField.getText()) + "_" + hashCode;
        // FIXME: writting data at the root of BAM_WORKSPACE... good idea? NO!!
        String dataFileName = String.format(BamFilesHelpers.DATA_CALIBRATION, sanitizedName);
        return new CalibrationData(
                sanitizedName,
                BamFilesHelpers.CONFIG_CALIBRATION,
                dataFileName,
                getInputs(),
                getOutputs());
    }

    @Override
    public String[] getTempDataFileNames() {
        return gaugingDataset == null ? new String[] {}
                : new String[] { getDataFileName(gaugingDataset.getDatasetName(), gaugingDataset.hashCode()) };
    }

    public String getDataFileName(String name, int hashCode) {
        return gaugingDataset == null ? null : name + "_" + hashCode + ".txt";
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        if (gaugingDataset != null) {
            JSONObject gaugingDatasetJson = new JSONObject();
            gaugingDatasetJson.put("name", gaugingDataset.getDatasetName());
            gaugingDatasetJson.put("hashCode", gaugingDataset.hashCode());
            json.put("gaugingDataset", gaugingDatasetJson);
            String dataFilePath = Path.of(App.TEMP_DIR, getDataFileName(
                    gaugingDataset.getDatasetName(),
                    gaugingDataset.hashCode())).toString();
            gaugingDataset.writeDataFile(dataFilePath);
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json.has("gaugingDataset")) {
            JSONObject gaugingDatasetJson = json.getJSONObject("gaugingDataset");
            String name = gaugingDatasetJson.getString("name");
            int hashCode = gaugingDatasetJson.getInt("hashCode");
            // String dataFileName = gaugingDatasetJson.getString("dataFileName");
            gaugingDataset = new GaugingsDataset();
            gaugingDataset.setDatasetName(name);

            String dataFilePath = Path.of(App.TEMP_DIR, getDataFileName(name, hashCode)).toString();
            if (Files.exists(Path.of(dataFilePath))) {
                System.out.println("Reading file ... (" + dataFilePath + ")");
                gaugingDataset.setDataFromFile(dataFilePath);
            } else {
                System.err.println("No file found (" + dataFilePath + ")");
            }
            updateTable();
        }
    }

    public Gaugings clone(String uuid) {
        Gaugings cloned = new Gaugings(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }
}
