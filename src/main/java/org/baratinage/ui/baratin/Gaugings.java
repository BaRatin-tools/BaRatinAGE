package org.baratinage.ui.baratin;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

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
import org.baratinage.ui.bam.BamItemList;
import org.json.JSONObject;

// FIXME: should it rather extend ImportedDataset or GaugingDataset?
public class Gaugings extends BaRatinItem implements ICalibrationData, BamItemList.BamItemListChangeListener {

    static private final String defaultNameTemplate = "Jeu de jaugages #%s";
    static private int nInstance = 0;

    private GaugingsTable gaugingsTable;
    private GaugingsDataset gaugingDataset;
    RowColPanel plotPanel;

    public Gaugings() {
        super(ITEM_TYPE.GAUGINGS);

        nInstance++;
        setName(String.format(
                defaultNameTemplate,
                nInstance + ""));
        setDescription("");

        setNameFieldLabel("Nom du jeu de jaugeages");
        setDescriptionFieldLabel("Description du jeu de jaugeages");

        JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        content.setBorder(BorderFactory.createEmptyBorder());

        RowColPanel importGaugingsPanel = new RowColPanel(AXIS.COL);
        importGaugingsPanel.setPadding(5);
        importGaugingsPanel.setGap(5);
        content.setLeftComponent(importGaugingsPanel);
        plotPanel = new RowColPanel();
        content.setRightComponent(plotPanel);

        JLabel importedDataSetSourceLabel = new JLabel("Jeu de jaugeages vide.");

        JButton importDataButton = new JButton("Importer un jeu de jaugeage");
        importDataButton.addActionListener((e) -> {
            GaugingsImporter gaugingsImporter = new GaugingsImporter();
            gaugingsImporter.showDialog();
            GaugingsDataset newGaugingDataset = gaugingsImporter.getDataset();
            if (newGaugingDataset != null && newGaugingDataset.getNumberOfColumns() == 3) {
                gaugingDataset = newGaugingDataset;
                gaugingsTable.set(newGaugingDataset);
                String desc = String.format("Jeu de jaugeages importé depuis le fichier '%s'",
                        gaugingDataset.getName());
                importedDataSetSourceLabel.setText(desc);
                if (getDescription() == null || getDescription().equals("")) {
                    setDescription(desc);
                }

            }
        });

        gaugingsTable = new GaugingsTable();
        gaugingsTable.getTableModel().addTableModelListener((e) -> {
            System.out.println("Table has changed!");
            setPlot();
        });

        importGaugingsPanel.appendChild(importDataButton, 0);
        importGaugingsPanel.appendChild(importedDataSetSourceLabel, 0);
        importGaugingsPanel.appendChild(gaugingsTable, 1);

        setContent(content);
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
        return new CalibrationData(getName(), getInputs(), getOutputs());
    }

    @Override
    public void parentHasChanged(BamItem parent) {
        System.out.println("PARENT HAS CHANGED => PARENT = " + parent);
    }

    @Override
    public String[] getTempDataFileNames() {
        return gaugingDataset == null ? new String[] {} : gaugingDataset.getTempDataFileNames();
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", getName());
        json.put("description", getName());

        if (gaugingDataset != null) {
            json.put("gaugingDataset", gaugingDataset.toJSON());
        }

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
        System.out.println("GAUGINGS === " + json.getString("name"));
    }

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {
        // // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'onChange'");
    }

}
