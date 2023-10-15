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
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.baratin.limnigraph.LimnigraphDataset;
import org.baratinage.ui.baratin.limnigraph.LimnigraphErrors;
import org.baratinage.ui.baratin.limnigraph.LimnigraphImporter;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.Misc;
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
                updateDataset(newLimniDataset);
            }
        });

        plotPanel = new RowColPanel();

        limniTable = new DataTable();

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
                                limniDataset.getDatasetName()));
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
        limniTable.updateData();

        T.t(limniTable, () -> {
            limniTable.setHeaderWidth(0, 150);
            limniTable.setHeader(0, T.text("date_time"));
            limniTable.setHeader(1, T.text("stage_level"));
            limniTable.updateHeader();
        });

        // T.updateTranslation(limniTable);
        // T.updateTranslation(limniErrConfigTable);

    }

    private void updatePlot() {
        // FIXME: shouldn't this be handled in a proper TimeSeriesPlot class?

        Plot plot = new Plot(false, true);

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
        List<double[]> stageMatrix = new ArrayList<>();
        stageMatrix.add(stage);
        // List<double[]> stageMatrix = limniDataset.getStageErrMatrix();

        List<double[]> dateTimeMatrix = new ArrayList<>();
        dateTimeMatrix.add(limniDataset.getDateTimeAsDouble());
        return new PredictionInput[] {
                new PredictionInput(
                        "limni_" + Misc.getTimeStampedId(),
                        stageMatrix,
                        dateTimeMatrix) };

    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        if (limniDataset != null) {
            json.put("limniDataset", limniDataset.toJSON(PROJECT));
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json.has("limniDataset")) {
            JSONObject limniDatasetJson = json.getJSONObject("limniDataset");
            updateDataset(LimnigraphDataset.buildFromJSON(limniDatasetJson));
        }
    }

    @Override
    public BamItem clone(String uuid) {
        Limnigraph cloned = new Limnigraph(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

}
