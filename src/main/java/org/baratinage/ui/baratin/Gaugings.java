package org.baratinage.ui.baratin;

import java.awt.Cursor;
import java.awt.Point;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsImporter;
import org.baratinage.ui.baratin.rating_curve.RatingCurvePlotToolsPanel;
import org.baratinage.ui.commons.DatasetConfig;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotPoints;
import org.baratinage.ui.plot.PlotUtils;
import org.baratinage.ui.plot.PointHighlight;
import org.baratinage.utils.Misc;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.baratinage.ui.bam.BamItemType;

import org.json.JSONObject;

public class Gaugings extends BamItem implements ICalibrationData {

    private final DataTable gaugingsTable;
    private GaugingsDataset gaugingDataset;
    private final SimpleFlowPanel plotPanel;
    private final JLabel importedDataSetSourceLabel;

    private final GaugingsImporter gaugingsImporter;

    private final RatingCurvePlotToolsPanel toolsPanel;

    public Gaugings(String uuid, BaratinProject project) {
        super(BamItemType.GAUGINGS, uuid, project);

        SimpleFlowPanel importGaugingsPanel = new SimpleFlowPanel(true);
        importGaugingsPanel.setPadding(5);
        importGaugingsPanel.setGap(5);

        plotPanel = new SimpleFlowPanel(true);

        toolsPanel = new RatingCurvePlotToolsPanel();
        toolsPanel.configure(true, true, false);
        toolsPanel.addChangeListener(l -> {
            setPlot();
        });

        SplitContainer content = new SplitContainer(
                importGaugingsPanel,
                plotPanel,
                true,
                0.3f);

        importedDataSetSourceLabel = new JLabel();

        gaugingsImporter = new GaugingsImporter();

        JButton importDataButton = new JButton();

        importDataButton.addActionListener((e) -> {
            gaugingsImporter.showDialog(T.text("import_gauging_set"));
            GaugingsDataset newGaugingDataset = gaugingsImporter.getDataset();
            if (newGaugingDataset != null && newGaugingDataset.getNumberOfColumns() >= 4) {
                gaugingDataset = newGaugingDataset;
                updateTable();
            }
        });

        gaugingsTable = new DataTable();
        gaugingsTable.addChangeListener((e) -> {
            int columnIndex = e.getColumn();
            Object[] activeGaugingColumn = gaugingsTable.getColumn(columnIndex);
            // double check that is indeed a boolean column
            if (activeGaugingColumn instanceof Boolean[]) {
                gaugingDataset.updateActiveStateValues((Boolean[]) activeGaugingColumn);
            }
            setPlot();
            fireChangeListeners();
        });

        importGaugingsPanel.addChild(importDataButton, false);
        importGaugingsPanel.addChild(importedDataSetSourceLabel, false);
        importGaugingsPanel.addChild(gaugingsTable, true);

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

        double[] stagePercentUncertainty = gaugingDataset.getStagePercentUncertainty();

        gaugingsTable.clearColumns();
        LocalDateTime[] dateTime = gaugingDataset.getDateTime();
        if (dateTime != null) {
            gaugingsTable.addColumn(dateTime);
        }
        gaugingsTable.addColumn(gaugingDataset.getStageValues());
        if (stagePercentUncertainty != null) {
            gaugingsTable.addColumn(stagePercentUncertainty);
        }
        gaugingsTable.addColumn(gaugingDataset.getDischargeValues());
        gaugingsTable.addColumn(gaugingDataset.getDischargePercentUncertainty());
        gaugingsTable.addColumn(gaugingDataset.getStateAsBoolean(), true);

        gaugingsTable.updateData();

        T.clear(gaugingsTable);
        T.t(gaugingsTable, () -> {
            int offset = 0;
            if (dateTime != null) {
                gaugingsTable.addColumn(dateTime);
                gaugingsTable.setHeader(0, T.text("date_time"));
                offset++;
            }
            gaugingsTable.setHeader(0 + offset, T.text("stage"));
            if (stagePercentUncertainty != null) {
                offset++;
                gaugingsTable.setHeader(0 + offset, T.text("stage_uncertainty_percent"));
            }
            gaugingsTable.setHeader(1 + offset, T.text("discharge_uncertainty_percent"));
            gaugingsTable.setHeader(2 + offset, T.text("stage_uncertainty_percent"));
            gaugingsTable.setHeader(3 + offset, T.text("active_gauging"));
            gaugingsTable.updateHeader();
        });

        updateImportedDatasetLabel();

    }

    private void setPlot() {

        // boolean axisFlipped = toolsPanel.axisFlipped();
        PlotPoints activeGaugings = gaugingDataset.getPlotPoints(
                toolsPanel.axisFlipped() ? GaugingsDataset.PlotType.hQ : GaugingsDataset.PlotType.Qh, true);
        PlotPoints inactiveGaugings = gaugingDataset.getPlotPoints(
                toolsPanel.axisFlipped() ? GaugingsDataset.PlotType.hQ : GaugingsDataset.PlotType.Qh, false);

        PointHighlight highlight = new PointHighlight(2, 20, AppSetup.COLORS.PLOT_HIGHLIGHT);

        Plot plot = new Plot(true);

        plot.addXYItem(highlight, false);
        plot.addXYItem(activeGaugings);
        plot.addXYItem(inactiveGaugings);

        T.clear(plotPanel);
        T.t(plotPanel, () -> {
            toolsPanel.updatePlotAxis(plot);
            activeGaugings.setLabel(T.text("lgd_active_gaugings"));
            inactiveGaugings.setLabel(T.text("lgd_inactive_gaugings"));
            plot.update();
        });

        PlotContainer plotContainer = new PlotContainer(plot);
        ChartPanel chartPanel = plotContainer.getChartPanel();

        double[] stage = gaugingDataset.getStageValues();
        double[] discharge = gaugingDataset.getDischargeValues();

        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                updateHighlight(event.getTrigger().getPoint(), false);
            }

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                updateHighlight(event.getTrigger().getPoint(), true);
            }

            private void updateHighlight(Point screenPoint, boolean includeTable) {
                double[] distances = PlotUtils.getDistancesFromPoint(
                        plotContainer,
                        stage,
                        discharge,
                        screenPoint);
                int minIndex = -1;
                double minValue = Double.POSITIVE_INFINITY;
                for (int k = 0; k < stage.length; k++) {
                    if (distances[k] < minValue) {
                        minValue = distances[k];
                        minIndex = k;
                    }
                }
                if (minIndex >= 0 && minValue < 20) {
                    highlight.setPosition(stage[minIndex], discharge[minIndex]);
                    highlight.setVisible(true);
                    chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    if (includeTable) {
                        gaugingsTable.selectRow(minIndex);
                    }
                    plot.update();
                } else {
                    highlight.setVisible(false);
                    chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    plot.update();
                }
            }
        });

        T.updateHierarchy(this, plotContainer);
        plotPanel.removeAll();
        plotPanel.addChild(plotContainer, true);
        plotPanel.addChild(toolsPanel, 0, 5);

    }

    public GaugingsDataset getGaugingDataset() {
        return gaugingDataset;
    }

    public void setGaugingDataset(GaugingsDataset gaugingsDataset) {
        this.gaugingDataset = gaugingsDataset;
        updateTable();
        setPlot();
        importedDataSetSourceLabel.setText(
                T.html("gauging_set_imported_from",
                        T.text(BamItemType.RATING_SHIFT_HAPPENS.id)));
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
    public BamConfig save(boolean writeFiles) {
        BamConfig config = new BamConfig(0);
        if (gaugingDataset != null) {
            DatasetConfig dc = gaugingDataset.save(writeFiles);
            JSONObject gaugingDatasetJson = dc.toJSON();
            config.JSON.put("gaugingDataset", gaugingDatasetJson);
            String[] dataFilePaths = dc.getAllFilePaths();
            for (String dfp : dataFilePaths) {
                config.FILE_PATHS.add(dfp);
            }
        }
        return config;
    }

    @Override
    public void load(BamConfig config) {

        JSONObject json = config.JSON;

        if (json.has("gaugingDataset")) {
            JSONObject gaugingDatasetJson = json.getJSONObject("gaugingDataset");
            gaugingDataset = GaugingsDataset.buildGaugingsDataset(
                    gaugingDatasetJson.getString("name"),
                    gaugingDatasetJson.getString("hashString"));
            updateTable();
        }
    }

}
