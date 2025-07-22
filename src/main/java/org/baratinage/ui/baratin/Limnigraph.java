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
import org.baratinage.ui.baratin.limnigraph.LimnigraphTable;
import org.baratinage.ui.baratin.limnigraph.LimnigraphImporter;
import org.baratinage.ui.baratin.limnigraph.LimnigraphPlot;
import org.baratinage.ui.commons.DatasetConfig;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.translation.T;
import org.baratinage.utils.Misc;
import org.json.JSONArray;
import org.json.JSONObject;

public class Limnigraph extends BamItem {

    private Icon chartIcon = AppSetup.ICONS.getCustomAppImageIcon("limnigraph.svg");
    private Icon errorIcon = AppSetup.ICONS.getCustomAppImageIcon("errors.svg");

    private final LimnigraphErrors limniErrors;

    private final LimnigraphTable limniTable;

    private final LimnigraphPlot limniPlot;

    private LimnigraphDataset limniDataset;
    private final JLabel importedDataSetSourceLabel;

    private final LimnigraphImporter limniImporter;

    public Limnigraph(String uuid, BaratinProject project) {
        super(BamItemType.LIMNIGRAPH, uuid, project);

        importedDataSetSourceLabel = new JLabel();

        limniImporter = new LimnigraphImporter();

        JButton importDataButton = new JButton();
        importDataButton.addActionListener((e) -> {
            limniImporter.showDialog(T.text("import_limnigraph"));
            LimnigraphDataset newLimniDataset = limniImporter.getDataset();
            if (newLimniDataset != null) {
                updateDataset(newLimniDataset);
            }
            fireChangeListeners();
        });

        limniPlot = new LimnigraphPlot();

        // plotPanel = new SimpleFlowPanel();
        // plotEditor = new PlotEditor();

        limniTable = new LimnigraphTable();
        limniTable.setPadding(0);

        limniErrors = new LimnigraphErrors();

        // laying out panel

        SimpleFlowPanel importLimnigraphPanel = new SimpleFlowPanel(true);
        importLimnigraphPanel.setPadding(5);
        importLimnigraphPanel.setGap(5);

        importLimnigraphPanel.addChild(importDataButton, false);
        importLimnigraphPanel.addChild(importedDataSetSourceLabel, false);
        importLimnigraphPanel.addChild(limniTable, true);

        TabContainer limniTablesAndPlots = new TabContainer();

        limniTablesAndPlots.addTab("Plot", chartIcon, limniPlot);
        limniTablesAndPlots.addTab("Limnigraph errors", errorIcon, limniErrors);

        SplitContainer content = new SplitContainer(
                importLimnigraphPanel,
                limniTablesAndPlots,
                true, 0.2f);
        content.setBorder(BorderFactory.createEmptyBorder());

        setContent(content);

        T.updateHierarchy(this, limniImporter);
        T.updateHierarchy(this, limniTable);
        T.updateHierarchy(this, limniErrors);
        T.updateHierarchy(this, limniPlot);
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
        limniPlot.updatePlot(limniDataset);
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

        config.JSON.put("plotEditor", limniPlot.plotEditor.toJSON());
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
                newLimniDataset = new LimnigraphDataset(
                        limniDatasetJson.getString("name"),
                        limniDatasetJson.getString("hashString"),
                        limniErrMatrixJson.getString("name"),
                        limniErrMatrixJson.getString("hashString"));
            }
            if (newLimniDataset != null) {
                updateDataset(newLimniDataset);
            }
        }

        if (json.has("plotEditor")) {
            limniPlot.plotEditor.fromJSON(json.getJSONObject("plotEditor"));
        }
    }

}
