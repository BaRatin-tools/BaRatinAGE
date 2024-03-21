package org.baratinage.ui.baratin;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfigRecord;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsImporter;
import org.baratinage.ui.commons.DatasetConfig;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.utils.Misc;
import org.baratinage.ui.bam.BamItemType;

import org.json.JSONObject;

public class Gaugings extends BamItem implements ICalibrationData {

    private final DataTable gaugingsTable;
    private GaugingsDataset gaugingDataset;
    private final RowColPanel plotPanel;
    private final JLabel importedDataSetSourceLabel;

    private final GaugingsImporter gaugingsImporter;

    public Gaugings(String uuid, BaratinProject project) {
        super(BamItemType.GAUGINGS, uuid, project);

        RowColPanel importGaugingsPanel = new RowColPanel(RowColPanel.AXIS.COL);
        importGaugingsPanel.setPadding(5);
        importGaugingsPanel.setGap(5);

        plotPanel = new RowColPanel();

        SplitContainer content = new SplitContainer(
                importGaugingsPanel,
                plotPanel,
                true);

        importedDataSetSourceLabel = new JLabel();

        gaugingsImporter = new GaugingsImporter();

        JButton importDataButton = new JButton();

        importDataButton.addActionListener((e) -> {
            gaugingsImporter.showDialog();
            GaugingsDataset newGaugingDataset = gaugingsImporter.getDataset();
            if (newGaugingDataset != null && newGaugingDataset.getNumberOfColumns() == 4) {
                gaugingDataset = newGaugingDataset;
                updateTable();
            }
        });

        gaugingsTable = new DataTable();
        gaugingsTable.addChangeListener((e) -> {
            gaugingDataset.updateActiveStateValues((Boolean[]) gaugingsTable.getColumn(3));
            setPlot();
            fireChangeListeners();
        });

        importGaugingsPanel.appendChild(importDataButton, 0);
        importGaugingsPanel.appendChild(importedDataSetSourceLabel, 0);
        importGaugingsPanel.appendChild(gaugingsTable, 1);

        setContent(content);

        T.updateHierarchy(this, gaugingsImporter);
        T.updateHierarchy(this, gaugingsTable);
        T.updateHierarchy(this, plotPanel);
        T.t(this, importDataButton, false, "import_gauging_set");
        T.t(this, () -> {
            updateImportedDatasetLabel();
        });

    }

    private void updateImportedDatasetLabel() {
        if (gaugingDataset == null) {
            importedDataSetSourceLabel.setText(T.text("empty_gauging_set"));
        } else {
            importedDataSetSourceLabel.setText(
                    T.html("gauging_set_imported_from",
                            gaugingDataset.getName()));
        }
    }

    private void updateTable() {

        gaugingsTable.clearColumns();
        gaugingsTable.addColumn(gaugingDataset.getStageValues());
        gaugingsTable.addColumn(gaugingDataset.getDischargeValues());
        gaugingsTable.addColumn(gaugingDataset.getDischargePercentUncertainty());
        gaugingsTable.addColumn(gaugingDataset.getActiveStateAsBoolean(), true);
        gaugingsTable.updateData();

        T.clear(gaugingsTable);
        T.t(gaugingsTable, () -> {
            gaugingsTable.setHeader(0, T.text("stage"));
            gaugingsTable.setHeader(1, T.text("discharge"));
            gaugingsTable.setHeader(2, T.text("uncertainty_percent"));
            gaugingsTable.setHeader(3, T.text("active_gauging"));
            gaugingsTable.updateHeader();
        });

        updateImportedDatasetLabel();

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

        T.clear(plotPanel);
        T.t(plotPanel, () -> {
            points.get(0).setLabel(T.text("lgd_active_gaugings"));
            points.get(1).setLabel(T.text("lgd_inactive_gaugings"));
            plot.axisX.setLabel(T.text("stage") + " [m]");
            plot.axisY.setLabel(T.text("discharge") + " [m3/s]");
            plot.axisYlog.setLabel(T.text("discharge") + " [m3/s]");
            plot.update();
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
        inputs[0] = new UncertainData("stage", gaugingDataset.getActiveStageValues());
        return inputs;
    }

    @Override
    public UncertainData[] getOutputs() {
        if (gaugingDataset == null) {
            return null;
        }
        double[] q = gaugingDataset.getActiveDischargeValues();
        double[] uq = gaugingDataset.getActiveDischargeStdUncertainty();
        UncertainData[] outputs = new UncertainData[1];
        outputs[0] = new UncertainData("discharge", q, uq);
        return outputs;
    }

    @Override
    public CalibrationData getCalibrationData() {
        if (gaugingDataset == null) {
            return null;
        }
        String sanitizedName = Misc.sanitizeName(bamItemNameField.getText()) + "_" + ID;
        String dataFileName = String.format(BamFilesHelpers.DATA_CALIBRATION, sanitizedName);
        return new CalibrationData(
                sanitizedName,
                BamFilesHelpers.CONFIG_CALIBRATION,
                dataFileName,
                getInputs(),
                getOutputs());
    }

    @Override
    public BamConfigRecord save(boolean writeFiles) {

        JSONObject json = new JSONObject();

        String[] dataFilePaths = new String[0];
        if (gaugingDataset != null) {
            DatasetConfig dc = gaugingDataset.save(writeFiles);
            JSONObject gaugingDatasetJson = dc.toJSON();
            json.put("gaugingDataset", gaugingDatasetJson);
            dataFilePaths = dc.getAllFilePaths();
        }

        return new BamConfigRecord(json, dataFilePaths);
    }

    @Override
    public void load(BamConfigRecord bamItemBackup) {

        JSONObject json = bamItemBackup.jsonObject();

        if (json.has("gaugingDataset")) {
            JSONObject gaugingDatasetJson = json.getJSONObject("gaugingDataset");
            gaugingDataset = new GaugingsDataset(
                    gaugingDatasetJson.getString("name"),
                    gaugingDatasetJson.getString("hashString"));
            updateTable();
        }
    }

}
