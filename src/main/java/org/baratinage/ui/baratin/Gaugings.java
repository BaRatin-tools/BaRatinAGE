package org.baratinage.ui.baratin;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.BamFilesHelpers;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.ICalibrationData;
import org.baratinage.ui.baratin.gaugings.GaugingData;
import org.baratinage.ui.baratin.gaugings.GaugingEditor;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.gaugings.GaugingsImporter;
import org.baratinage.ui.baratin.gaugings.GaugingsPlot;
import org.baratinage.ui.baratin.gaugings.GaugingsTable;
import org.baratinage.ui.commons.DatasetConfig;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.translation.T;
import org.baratinage.utils.Misc;
import org.baratinage.ui.bam.BamItemType;

import org.json.JSONObject;

public class Gaugings extends BamItem implements ICalibrationData, GaugingsDataset.IGaugingDatasetChangeListener {

    private final GaugingsTable gaugingsTable;
    private GaugingsDataset gaugingDataset;
    public final GaugingsPlot plotPanel;
    private final GaugingEditor gaugingEditor;
    private final JLabel importedDataSetSourceLabel;

    private final GaugingsImporter gaugingsImporter;

    public Gaugings(String uuid, BaratinProject project) {
        super(BamItemType.GAUGINGS, uuid, project);

        SimpleFlowPanel leftPanel = new SimpleFlowPanel(true);
        leftPanel.setPadding(5);
        leftPanel.setGap(5);

        gaugingsTable = new GaugingsTable();
        gaugingEditor = new GaugingEditor();
        plotPanel = new GaugingsPlot(gaugingsTable);

        SplitContainer content = new SplitContainer(
                leftPanel,
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
                newGaugingDataset.addChangeListener(this);
                gaugingDataset = newGaugingDataset;
                gaugingsTable.updateTable(gaugingDataset);
                gaugingEditor.setDataset(gaugingDataset);
                plotPanel.setGaugingsDataset(gaugingDataset);
                plotPanel.resetPlotZoom();
                updateImportedDatasetLabel();
            }
        });

        gaugingsTable.table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] rows = gaugingsTable.table.getSelectedRows();
                gaugingEditor.setSelectedIndices(rows);
            }
        });

        JScrollPane sp = new JScrollPane();
        sp.setViewportView(gaugingEditor);

        gaugingEditor.setVisible(false);
        sp.setVisible(false);
        gaugingsTable.editableGaugingsToggle.setSelected(false);
        gaugingsTable.editableGaugingsToggle.addActionListener(l -> {
            gaugingEditor.setVisible(gaugingsTable.editableGaugingsToggle.isSelected());
            sp.setVisible(gaugingsTable.editableGaugingsToggle.isSelected());
            leftPanel.updateUI();
        });

        leftPanel.addChild(importDataButton, false);
        leftPanel.addChild(importedDataSetSourceLabel, false);
        leftPanel.addChild(gaugingsTable, 2);
        leftPanel.addChild(sp, 1);

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

    public GaugingsDataset getGaugingDataset() {
        return gaugingDataset;
    }

    public void setGaugingDataset(GaugingsDataset gaugingDataset, String sourceName) {
        this.gaugingDataset = gaugingDataset;
        gaugingsTable.updateTable(gaugingDataset);
        gaugingEditor.setDataset(gaugingDataset);
        plotPanel.setGaugingsDataset(gaugingDataset);
        plotPanel.plotEditor.saveAsDefault(true);
        importedDataSetSourceLabel.setText(T.html("gauging_set_imported_from", sourceName));
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

            JSONObject plotEditorJson = plotPanel.plotEditor.toJSON();
            config.JSON.put("plotEditor", plotEditorJson);

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
            gaugingDataset.addChangeListener(this);
            gaugingsTable.updateTable(gaugingDataset);
            gaugingEditor.setDataset(gaugingDataset);
            plotPanel.setGaugingsDataset(gaugingDataset);
            updateImportedDatasetLabel();
        }

        if (json.has("plotEditor")) {
            JSONObject plotEditorJson = json.getJSONObject("plotEditor");
            plotPanel.plotEditor.fromJSON(plotEditorJson);
        }

    }

    @Override
    public void onGaugingModified(int index, GaugingData g) {
        gaugingsTable.updateGauging(index, g);
        plotPanel.setGaugingsDataset(gaugingDataset);
        gaugingEditor.updateFieldFromGaugings();
        fireChangeListeners();
    }

    @Override
    public void onGaugingAdded(int index, GaugingData g) {
        gaugingsTable.updateTable(gaugingDataset);
        plotPanel.setGaugingsDataset(gaugingDataset);
        gaugingEditor.updateFieldFromGaugings();
        gaugingsTable.selectRow(index);
        fireChangeListeners();
    }

    @Override
    public void onGaugingsDeleted(List<Integer> indices) {
        gaugingsTable.updateTable(gaugingDataset);
        plotPanel.setGaugingsDataset(gaugingDataset);
        gaugingEditor.updateFieldFromGaugings();
        fireChangeListeners();
    }

}
