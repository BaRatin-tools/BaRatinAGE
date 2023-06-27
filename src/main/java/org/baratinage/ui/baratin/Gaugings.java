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
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsImporter;
import org.baratinage.ui.baratin.gaugings.GaugingsPlot;
import org.baratinage.ui.baratin.gaugings.GaugingsTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.Misc;
import org.baratinage.ui.bam.BamItemList;
import org.json.JSONObject;

public class Gaugings extends BaRatinItem implements ICalibrationData, BamItemList.BamItemListChangeListener {

    static private final String defaultNameTemplate = "Jeu de jaugages #%s";
    static private int nInstance = 0;

    private GaugingsTable gaugingsTable;
    private GaugingsDataset gaugingDataset;
    private RowColPanel plotPanel;
    private JLabel importedDataSetSourceLabel;

    public Gaugings(String uuid) {
        super(ITEM_TYPE.GAUGINGS, uuid);

        nInstance++;
        setName(String.format(
                defaultNameTemplate,
                nInstance + ""));
        setDescription("");

        setNameFieldLabel("Nom du jeu de jaugeages");
        setDescriptionFieldLabel("Description du jeu de jaugeages");

        JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        content.setBorder(BorderFactory.createEmptyBorder());

        RowColPanel importGaugingsPanel = new RowColPanel(RowColPanel.AXIS.COL);
        importGaugingsPanel.setPadding(5);
        importGaugingsPanel.setGap(5);
        content.setLeftComponent(importGaugingsPanel);
        plotPanel = new RowColPanel();
        content.setRightComponent(plotPanel);

        importedDataSetSourceLabel = new JLabel("Jeu de jaugeages vide.");

        JButton importDataButton = new JButton("Importer un jeu de jaugeage");
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
            hasChanged();
        });

        importGaugingsPanel.appendChild(importDataButton, 0);
        importGaugingsPanel.appendChild(importedDataSetSourceLabel, 0);
        importGaugingsPanel.appendChild(gaugingsTable, 1);

        setContent(content);
    }

    public void updateTable() {
        gaugingsTable.set(gaugingDataset);
        String desc = String.format("Jeu de jaugeages importé depuis le fichier '%s'",
                gaugingDataset.getName());
        importedDataSetSourceLabel.setText(desc);
        if (getDescription() == null || getDescription().equals("")) {
            setDescription(desc);
        }

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
        String sanitizedName = Misc.sanitizeName(getName());
        System.out.println(sanitizedName);
        return new CalibrationData(sanitizedName, getInputs(), getOutputs());
    }

    @Override
    public void parentHasChanged(BamItem parent) {
        System.out.println("PARENT HAS CHANGED => PARENT = " + parent);
    }

    @Override
    public String[] getTempDataFileNames() {
        // writeDataFile();
        return gaugingDataset == null ? new String[] {}
                : new String[] { getDataFileName(gaugingDataset.getName(), gaugingDataset.hashCode()) };
    }

    public String getDataFileName(String name, int hashCode) {
        return gaugingDataset == null ? null : name + "_" + hashCode + ".txt";
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put("name", getName());
        json.put("description", getDescription());

        if (gaugingDataset != null) {
            JSONObject gaugingDatasetJson = new JSONObject();
            gaugingDatasetJson.put("name", gaugingDataset.getName());
            gaugingDatasetJson.put("hashCode", gaugingDataset.hashCode());
            json.put("gaugingDataset", gaugingDatasetJson);
            String dataFilePath = Path.of(App.TEMP_DIR, getDataFileName(
                    gaugingDataset.getName(),
                    gaugingDataset.hashCode())).toString();
            gaugingDataset.writeDataFile(dataFilePath);
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json.has("name")) {
            setName(json.getString("name"));
        }

        if (json.has("description")) {
            setDescription(json.getString("description"));
        }

        if (json.has("gaugingDataset")) {
            JSONObject gaugingDatasetJson = json.getJSONObject("gaugingDataset");
            String name = gaugingDatasetJson.getString("name");
            int hashCode = gaugingDatasetJson.getInt("hashCode");
            // String dataFileName = gaugingDatasetJson.getString("dataFileName");
            gaugingDataset = new GaugingsDataset();
            gaugingDataset.setName(name);

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

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {
    }

    public Gaugings clone(String uuid) {
        Gaugings cloned = new Gaugings(uuid);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }
}
