package org.baratinage.ui.baratin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.baratinage.jbam.PredictionInput;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IPredictionData;
import org.baratinage.ui.baratin.limnigraph.LimnigraphDataset;
import org.baratinage.ui.baratin.limnigraph.LimnigraphImporter;
import org.baratinage.ui.baratin.limnigraph.LimnigraphTable;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.utils.Misc;
import org.json.JSONObject;

public class Limnigraph extends BamItem implements IPredictionData {

    private LimnigraphTable limniTable;
    private LimnigraphDataset limniDataset;
    private JLabel importedDataSetSourceLabel;

    private RowColPanel plotPanel;

    public Limnigraph(String uuid, BaratinProject project) {
        super(BamItemType.LIMNIGRAPH, uuid, project);

        JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        content.setBorder(BorderFactory.createEmptyBorder());

        RowColPanel importLimnigraphPanel = new RowColPanel(RowColPanel.AXIS.COL);
        importLimnigraphPanel.setPadding(5);
        importLimnigraphPanel.setGap(5);

        importedDataSetSourceLabel = new JLabel();

        JButton importDataButton = new JButton();
        importDataButton.addActionListener((e) -> {
            LimnigraphImporter limniImporter = new LimnigraphImporter();
            limniImporter.showDialog();
            LimnigraphDataset newLimniDataset = limniImporter.getDataset();
            if (newLimniDataset != null && newLimniDataset.getNumberOfColumns() >= 1) {
                limniDataset = newLimniDataset;
                updateTable();
            }
        });

        limniTable = new LimnigraphTable();
        limniTable.getTableModel().addTableModelListener((e) -> {
            setPlot();
            fireChangeListeners();
        });

        importLimnigraphPanel.appendChild(importDataButton, 0);
        importLimnigraphPanel.appendChild(importedDataSetSourceLabel, 0);
        importLimnigraphPanel.appendChild(limniTable, 1);

        content.setLeftComponent(importLimnigraphPanel);

        plotPanel = new RowColPanel();
        content.setRightComponent(plotPanel);

        setContent(content);

        T.updateHierarchy(this, limniTable);
        T.updateHierarchy(this, plotPanel);
        T.t(this, importDataButton, false, "import_limnigraph");
        T.t(this, () -> {
            if (limniDataset == null) {
                importedDataSetSourceLabel.setText(T.text("empty_limnigraph"));
            } else {
                importedDataSetSourceLabel.setText(
                        T.html("limnigraph_imported_from",
                                limniDataset.getDatasetName()));
            }
        });
    }

    private void updateTable() {
        limniTable.set(limniDataset);
        // T.updateRegisteredObject(this);
        T.updateTranslation(this);
        // Lg.register(importedDataSetSourceLabel, () -> {
        // importedDataSetSourceLabel.setText(
        // Lg.html("limnigraph_imported_from",
        // limniDataset.getDatasetName()));
        // });
    }

    private void setPlot() {
        // FIXME: shouldn't this be handled in a proper TimeSeriesPlot class?

        Plot plot = new Plot(false, true);

        PlotItem[] items = limniDataset.getPlotLines();
        for (PlotItem item : items) {
            plot.addXYItem(item);
        }

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
        List<double[]> stageMatrix = limniDataset.getStageMatrix();
        List<double[]> dateTimeMatrix = new ArrayList<>();
        dateTimeMatrix.add(limniDataset.getDateTimeAsDouble());
        return new PredictionInput[] {
                new PredictionInput(
                        "limni_" + Misc.getTimeStampedId(),
                        stageMatrix,
                        dateTimeMatrix) };

    }

    private static String buildDataFileName(String name, int hashCode) {
        return name + "_" + hashCode + ".txt";
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        if (limniDataset != null) {
            JSONObject limniDatasetJson = new JSONObject();
            limniDatasetJson.put("name", limniDataset.getDatasetName());
            limniDatasetJson.put("hashCode", limniDataset.hashCode());
            json.put("limniDataset", limniDatasetJson);
            String dataFilePath = Path.of(AppConfig.AC.APP_TEMP_DIR, buildDataFileName(
                    limniDataset.getDatasetName(),
                    limniDataset.hashCode())).toString();
            limniDataset.writeDataFile(dataFilePath);
            registerFile(dataFilePath);
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json.has("limniDataset")) {
            JSONObject limniDatasetJson = json.getJSONObject("limniDataset");
            String name = limniDatasetJson.getString("name");
            int hashCode = limniDatasetJson.getInt("hashCode");

            String dataFilePath = Path.of(AppConfig.AC.APP_TEMP_DIR, buildDataFileName(name, hashCode))
                    .toString();
            if (Files.exists(Path.of(dataFilePath))) {
                System.out.println("Limnigraph: Reading file ... (" + dataFilePath + ")");
                limniDataset = LimnigraphDataset.buildLimnigraphDataset(name, dataFilePath);
            } else {
                System.err.println("Limnigraph Error: No file found (" + dataFilePath + ")");
            }
            updateTable();
        }
    }

    @Override
    public BamItem clone(String uuid) {
        Limnigraph cloned = new Limnigraph(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

}
