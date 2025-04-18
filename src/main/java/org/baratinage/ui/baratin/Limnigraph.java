package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.baratin.limnigraph.LimnigraphDataset;
import org.baratinage.ui.baratin.limnigraph.LimnigraphErrors;
import org.baratinage.ui.baratin.limnigraph.LimnigraphImporter;
import org.baratinage.ui.baratin.limnigraph.LimnigraphTable;
import org.baratinage.ui.commons.DatasetConfig;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.utils.Misc;
import org.json.JSONArray;
import org.json.JSONObject;

public class Limnigraph extends BamItem {

    private Icon chartIcon = AppSetup.ICONS.getCustomAppImageIcon("limnigraph.svg");
    private Icon errorIcon = AppSetup.ICONS.getCustomAppImageIcon("errors.svg");

    private LimnigraphErrors limniErrors;

    private LimnigraphTable limniTable;

    private LimnigraphDataset limniDataset;
    private JLabel importedDataSetSourceLabel;

    private RowColPanel plotPanel;

    private LimnigraphImporter limniImporter;

    public Limnigraph(String uuid, BaratinProject project) {
        super(BamItemType.LIMNIGRAPH, uuid, project);

        importedDataSetSourceLabel = new JLabel();

        limniImporter = new LimnigraphImporter();

        JButton importDataButton = new JButton();
        importDataButton.addActionListener((e) -> {
            limniImporter.showDialog();
            LimnigraphDataset newLimniDataset = limniImporter.getDataset();
            if (newLimniDataset != null && newLimniDataset.getNumberOfColumns() >= 1) {
                if (!newLimniDataset.hasStageErrMatrix()) {
                    newLimniDataset.computeErroMatrix(AppSetup.CONFIG.N_SAMPLES_LIMNI_ERRORS.get());
                }
                updateDataset(newLimniDataset);
            }
            fireChangeListeners();
        });

        plotPanel = new RowColPanel();

        limniTable = new LimnigraphTable();
        limniTable.setPadding(0);

        limniErrors = new LimnigraphErrors();

        // laying out panel

        RowColPanel importLimnigraphPanel = new RowColPanel(RowColPanel.AXIS.COL);
        importLimnigraphPanel.setPadding(5);
        importLimnigraphPanel.setGap(5);

        importLimnigraphPanel.appendChild(importDataButton, 0);
        importLimnigraphPanel.appendChild(importedDataSetSourceLabel, 0);
        importLimnigraphPanel.appendChild(limniTable, 1);

        TabContainer limniTablesAndPlots = new TabContainer();

        limniTablesAndPlots.addTab("Plot", chartIcon, plotPanel);
        limniTablesAndPlots.addTab("Limnigraph errors", errorIcon, limniErrors);

        SplitContainer content = new SplitContainer(
                importLimnigraphPanel,
                limniTablesAndPlots,
                true);
        content.setBorder(BorderFactory.createEmptyBorder());

        setContent(content);

        T.updateHierarchy(this, limniImporter);
        T.updateHierarchy(this, limniTable);
        T.updateHierarchy(this, limniErrors);
        T.updateHierarchy(this, plotPanel);
        T.t(this, importDataButton, false, "import_limnigraph");
        T.t(this, () -> {
            limniTablesAndPlots.setTitleAt(0, T.text("chart"));
            limniTablesAndPlots.setTitleAt(1, T.text("stage_uncertainty"));
            updateImportedDatasetLabel();
        });
    }

    private void updateImportedDatasetLabel() {
        if (limniDataset == null) {
            importedDataSetSourceLabel.setText(T.text("empty_limnigraph"));
        } else {
            importedDataSetSourceLabel.setText(
                    T.html("limnigraph_imported_from",
                            limniDataset.getName()));
        }
    }

    private void updateDataset(LimnigraphDataset newDataset) {
        limniDataset = newDataset;
        limniErrors.updateDataset(newDataset);
        updateTables();
        updatePlot();
        updateImportedDatasetLabel();
        save(true);
    }

    private void updateTables() {

        if (limniDataset == null) {
            return;
        }
        double[] stage_low = null;
        double[] stage_high = null;
        if (limniDataset.hasStageErrMatrix()) {
            List<double[]> errEnv = limniDataset.getStageErrUncertaintyEnvelop();
            stage_low = errEnv.get(0);
            stage_high = errEnv.get(1);
        }
        limniTable.updateTable(limniDataset.getDateTime(), limniDataset.getStage(), stage_low, stage_high);
    }

    private void updatePlot() {
        // FIXME: shouldn't this be handled in a proper TimeSeriesPlot class?

        Plot plot = new Plot(true, true);

        PlotItem envelop = limniDataset.hasStageErrMatrix() ? limniDataset.getPlotEnv() : null;
        if (envelop != null) {
            plot.addXYItem(envelop);
        }
        PlotItem limnigraph = limniDataset.getPlotLine();
        plot.addXYItem(limnigraph);

        T.clear(plotPanel);
        T.t(plotPanel, () -> {
            plot.axisXdate.setLabel(T.text("time"));
            plot.axisY.setLabel(T.text("stage") + " [m]");
            plot.axisYlog.setLabel(T.text("stage") + " [m]");
            if (envelop != null) {
                envelop.setLabel(T.text("stage_uncertainty"));
            }
            limnigraph.setLabel(T.text("limnigraph"));
        });
        PlotContainer plotContainer = new PlotContainer(plot);
        T.updateHierarchy(this, plotContainer);

        plotPanel.clear();
        plotPanel.appendChild(plotContainer);
    }

    public PredictionInput getErrorFreePredictionInput() {
        if (limniDataset == null) {
            return null;
        }

        double[] stage = limniDataset.getStage(true);
        List<double[]> stageVector = new ArrayList<>();
        stageVector.add(stage);

        return new PredictionInput(
                "limni_" + Misc.getTimeStampedId(),
                stageVector);
    }

    public PredictionInput getUncertainPredictionInput() {
        if (limniDataset == null) {
            return null;
        }
        if (!limniDataset.hasStageErrMatrix()) {
            return null;
        }
        return new PredictionInput(
                "limni_errors_" + Misc.getTimeStampedId(),
                limniDataset.getStageErrMatrix(true));
    }

    public double[] getMissingValuesExtraData() {
        return limniDataset.getMissingValueIndicesAsDouble();
    }

    public double[] getDateTimeExtraData() {
        return limniDataset.getDateTimeAsDouble();
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        BamConfig config = new BamConfig(0);
        if (limniDataset != null) {
            DatasetConfig dc = limniDataset.save(writeFiles);
            JSONObject limniDatasetJson = dc.toJSON();
            config.JSON.put("limniDataset", limniDatasetJson);
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

        if (json.has("limniDataset")) {
            JSONObject limniDatasetJson = json.getJSONObject("limniDataset");
            JSONObject limniErrMatrixJson = null;
            if (limniDatasetJson.has("nested")) {
                JSONArray nestedJson = limniDatasetJson.getJSONArray("nested");
                limniErrMatrixJson = nestedJson.getJSONObject(0);
            }
            LimnigraphDataset newLimniDataset = null;
            if (limniErrMatrixJson == null) {
                newLimniDataset = new LimnigraphDataset(
                        limniDatasetJson.getString("name"),
                        limniDatasetJson.getString("hashString"));
            } else {
                int nCol = limniErrMatrixJson.getJSONArray("headers").length();
                newLimniDataset = new LimnigraphDataset(
                        limniDatasetJson.getString("name"),
                        limniDatasetJson.getString("hashString"),
                        limniErrMatrixJson.getString("name"),
                        limniErrMatrixJson.getString("hashString"), nCol);
            }
            if (newLimniDataset != null) {
                updateDataset(newLimniDataset);
            }
        }
    }

}
