package org.baratinage.ui.baratin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsImporter;
import org.baratinage.ui.baratin.gaugings.GaugingsTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotPoints;
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

        JButton importDataButton = new JButton();

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

        T.updateHierarchy(this, gaugingsTable);
        T.updateHierarchy(this, plotPanel);
        T.t(this, importDataButton, false, "import_gauging_set");
        T.t(this, () -> {
            if (gaugingDataset == null) {
                importedDataSetSourceLabel.setText(T.text("empty_gauging_set"));
            } else {
                importedDataSetSourceLabel.setText(
                        T.html("gauging_set_imported_from",
                                gaugingDataset.getDatasetName()));
            }
        });

    }

    private void updateTable() {
        gaugingsTable.set(gaugingDataset);
        T.updateTranslation(this);
    }

    private void setPlot() {
        // FIXME: shouldn't this be handled in a proper GaugingPlot class?
        // GaugingsPlot gaugingsPlot = new GaugingsPlot(
        // true,
        // gaugingDataset);

        Plot plot = new Plot(true);

        List<PlotPoints> points = gaugingDataset.getPlotPointsItems();

        plot.addXYItem(points.get(0));
        plot.addXYItem(points.get(1));

        // T.t(points.get(0), "lgd_active_gaugings");
        // T.t(points.get(1), "lgd_inactive_gaugings");

        // T.t(() -> {
        // points.get(0).setLabel(T.text("lgd_active_gaugings"));
        // });

        // T.t(() -> {
        // points.get(0).setLabel(T.text("lgd_inactive_gaugings"));
        // });

        // T.t(plot, (plt) -> {
        // plt.axisX.setLabel(T.text("stage_level"));
        // plt.axisY.setLabel(T.text("discharge"));
        // plt.axisYlog.setLabel(T.text("discharge"));
        // });

        T.clear(plotPanel);
        T.t(plotPanel, () -> {
            points.get(0).setLabel(T.text("lgd_active_gaugings"));
            points.get(0).setLabel(T.text("lgd_inactive_gaugings"));
            plot.axisX.setLabel(T.text("stage_level"));
            plot.axisY.setLabel(T.text("discharge"));
            plot.axisYlog.setLabel(T.text("discharge"));
        });

        PlotContainer plotContainer = new PlotContainer(plot);
        T.updateHierarchy(this, plotContainer);

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
        String dataFileName = String.format(BamFilesHelpers.DATA_CALIBRATION, sanitizedName);
        return new CalibrationData(
                sanitizedName,
                BamFilesHelpers.CONFIG_CALIBRATION,
                dataFileName,
                getInputs(),
                getOutputs());
    }

    private static String buildDataFileName(String name, int hashCode) {
        return name + "_" + hashCode + ".txt";
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        if (gaugingDataset != null) {
            JSONObject gaugingDatasetJson = new JSONObject();
            gaugingDatasetJson.put("name", gaugingDataset.getDatasetName());
            gaugingDatasetJson.put("hashCode", gaugingDataset.hashCode());
            json.put("gaugingDataset", gaugingDatasetJson);
            String dataFilePath = Path.of(AppConfig.AC.APP_TEMP_DIR, buildDataFileName(
                    gaugingDataset.getDatasetName(),
                    gaugingDataset.hashCode())).toString();
            gaugingDataset.writeDataFile(dataFilePath);
            registerFile(dataFilePath);
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json.has("gaugingDataset")) {
            JSONObject gaugingDatasetJson = json.getJSONObject("gaugingDataset");
            String name = gaugingDatasetJson.getString("name");
            int hashCode = gaugingDatasetJson.getInt("hashCode");

            String dataFilePath = Path.of(AppConfig.AC.APP_TEMP_DIR, buildDataFileName(name, hashCode))
                    .toString();
            if (Files.exists(Path.of(dataFilePath))) {
                System.out.println("Gaugings: Reading file '" + dataFilePath + "'...");
                gaugingDataset = GaugingsDataset.buildGaugingDataset(name, dataFilePath);
            } else {
                System.err.println("Gaugings Error: File '" + dataFilePath + "' not found!");
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
