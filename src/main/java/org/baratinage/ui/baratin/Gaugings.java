package org.baratinage.ui.baratin;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemConfig;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsImporter;
import org.baratinage.ui.baratin.gaugings.GaugingsTable;
import org.baratinage.ui.commons.DatasetConfig;
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
                                gaugingDataset.getName()));
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

        T.clear(plotPanel);
        T.t(plotPanel, () -> {
            points.get(0).setLabel(T.text("lgd_active_gaugings"));
            points.get(0).setLabel(T.text("lgd_inactive_gaugings"));
            plot.axisX.setLabel(T.text("stage_level"));
            plot.axisY.setLabel(T.text("discharge"));
            plot.axisYlog.setLabel(T.text("discharge"));
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
    public BamItemConfig save(boolean writeFiles) {

        JSONObject json = new JSONObject();

        if (gaugingDataset != null) {
            DatasetConfig adc = gaugingDataset.save(writeFiles);
            json.put("gaugingDataset", adc.toJSON());
        }

        return new BamItemConfig(json);
    }

    @Override
    public void load(BamItemConfig bamItemBackup) {

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
