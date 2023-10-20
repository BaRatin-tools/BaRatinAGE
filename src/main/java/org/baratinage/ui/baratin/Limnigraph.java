package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfigRecord;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.baratin.limnigraph.LimnigraphDataset;
import org.baratinage.ui.baratin.limnigraph.LimnigraphErrors;
import org.baratinage.ui.baratin.limnigraph.LimnigraphImporter;
import org.baratinage.ui.commons.DatasetConfig;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.Misc;
import org.json.JSONArray;
import org.json.JSONObject;

public class Limnigraph extends BamItem implements IPredictionData {

    private ImageIcon chartIcon = SvgIcon.buildCustomAppImageIcon("limnigraph.svg");
    private ImageIcon errorIcon = SvgIcon.buildCustomAppImageIcon("errors.svg");

    private LimnigraphErrors limniErrors;

    private DataTable limniTable;
    // private LimnigraphTable limniTable;
    private LimnigraphDataset limniDataset;
    private JLabel importedDataSetSourceLabel;

    private RowColPanel plotPanel;

    public Limnigraph(String uuid, BaratinProject project) {
        super(BamItemType.LIMNIGRAPH, uuid, project);

        importedDataSetSourceLabel = new JLabel();

        JButton importDataButton = new JButton();
        importDataButton.addActionListener((e) -> {
            LimnigraphImporter limniImporter = new LimnigraphImporter();
            limniImporter.showDialog();
            LimnigraphDataset newLimniDataset = limniImporter.getDataset();
            if (newLimniDataset != null && newLimniDataset.getNumberOfColumns() >= 1) {
                if (!newLimniDataset.hasStageErrMatrix()) {
                    newLimniDataset.computeErroMatrix(200);
                }
                updateDataset(newLimniDataset);
            }
        });

        plotPanel = new RowColPanel();

        limniTable = new DataTable();
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

        JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        content.setBorder(BorderFactory.createEmptyBorder());

        content.setLeftComponent(importLimnigraphPanel);
        content.setRightComponent(limniTablesAndPlots);

        setContent(content);

        T.updateHierarchy(this, limniTable);
        T.updateHierarchy(this, limniErrors);
        T.updateHierarchy(this, plotPanel);
        T.t(this, importDataButton, false, "import_limnigraph");
        T.t(this, () -> {
            limniTablesAndPlots.setTitleAt(0, T.text("chart"));
            limniTablesAndPlots.setTitleAt(1, T.text("stage_uncertainty"));
            if (limniDataset == null) {
                importedDataSetSourceLabel.setText(T.text("empty_limnigraph"));
            } else {
                importedDataSetSourceLabel.setText(
                        T.html("limnigraph_imported_from",
                                limniDataset.getName()));
            }
        });
    }

    private void updateDataset(LimnigraphDataset newDataset) {
        limniDataset = newDataset;
        limniErrors.updateDataset(newDataset);
        updateTables();
        updatePlot();
    }

    private void updateTables() {
        T.clear(limniTable);

        limniTable.clearColumns();

        if (limniDataset == null) {
            return;
        }

        limniTable.addColumn(limniDataset.getDateTime());
        limniTable.addColumn(limniDataset.getStage());
        if (limniDataset.hasStageErrMatrix()) {
            List<double[]> errEnv = limniDataset.getStageErrUncertaintyEnvelop();
            limniTable.addColumn(errEnv.get(0));
            limniTable.addColumn(errEnv.get(1));
        }

        limniTable.updateData();

        T.t(limniTable, () -> {
            limniTable.setHeader(0, T.text("date_time"));
            limniTable.setHeader(1, T.text("stage_level"));
            if (limniDataset.hasStageErrMatrix()) {
                limniTable.setHeader(2, T.text("percentile_0025"));
                limniTable.setHeader(3, T.text("percentile_0975"));
            }
            limniTable.autosetHeadersWidths(25, 150);
            limniTable.setHeaderWidth(0, 150);
            limniTable.updateHeader();
        });

    }

    private void updatePlot() {
        // FIXME: shouldn't this be handled in a proper TimeSeriesPlot class?

        Plot plot = new Plot(false, true);

        if (limniDataset.hasStageErrMatrix()) {
            plot.addXYItem(limniDataset.getPlotEnv());
        }
        plot.addXYItem(limniDataset.getPlotLine());

        T.clear(plotPanel);
        T.t(plotPanel, () -> {
            plot.axisXdate.setLabel(T.text("time"));
            plot.axisY.setLabel(T.text("stage_level"));
            plot.axisYlog.setLabel(T.text("stage_level"));
        });
        PlotContainer plotContainer = new PlotContainer(plot);
        T.updateHierarchy(this, plotContainer);

        plotPanel.clear();
        plotPanel.appendChild(plotContainer);
    }

    @Override
    public PredictionInput[] getPredictionInputs() {
        if (limniDataset == null) {
            return null;
        }

        double[] stage = limniDataset.getStage();
        List<double[]> stageVector = new ArrayList<>();
        stageVector.add(stage);

        int nPred = limniDataset.hasStageErrMatrix() ? 2 : 1;

        PredictionInput[] predInputs = new PredictionInput[nPred];

        List<double[]> dateTimeMatrix = new ArrayList<>();
        dateTimeMatrix.add(limniDataset.getDateTimeAsDouble());
        predInputs[0] = new PredictionInput(
                "limni_" + Misc.getTimeStampedId(),
                stageVector,
                dateTimeMatrix);
        if (nPred > 1) {
            List<double[]> stageMatrix = limniDataset.getStageErrMatrix();
            predInputs[1] = new PredictionInput(
                    "limni_errors_" + Misc.getTimeStampedId(),
                    stageMatrix);
        }

        return predInputs;

    }

    @Override
    public BamConfigRecord save(boolean writeFiles) {

        JSONObject json = new JSONObject();

        String[] dataFilePaths = new String[0];
        if (limniDataset != null) {
            DatasetConfig dc = limniDataset.save(writeFiles);
            JSONObject limniDatasetJson = dc.toJSON();
            json.put("limniDataset", limniDatasetJson);
            dataFilePaths = dc.getAllFilePaths();
        }

        return new BamConfigRecord(json, dataFilePaths);
    }

    @Override
    public void load(BamConfigRecord bamItemBackup) {

        JSONObject json = bamItemBackup.jsonObject();

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
