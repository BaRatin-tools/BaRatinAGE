package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemCombobox;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IMcmc;
import org.baratinage.ui.commons.OutOfSyncWarning;
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

public class RatingCurve extends BaRatinItem implements ICalibratedModel, IMcmc, BamItemList.BamItemListChangeListener {

    static private final String defaultNameTemplate = "Courbe de tarage #%s";
    static private int nInstance = 0;

    private BamItemParent hydrauConfParent;
    private BamItemParent gaugingsParent;
    private BamItemParent structErrorParent;

    private PosteriorRatingCurve posteriorRatingCurve;

    private RowColPanel outdatedInfoPanel;

    private String jsonStringBackup;

    public RatingCurve(String uuid) {
        super(ITEM_TYPE.RATING_CURVE, uuid);
        nInstance++;
        setName(String.format(
                defaultNameTemplate,
                nInstance + ""));
        setDescription("");

        setNameFieldLabel("Nom de la courbe de tarage");
        setDescriptionFieldLabel("Description de la courbe de tarage");

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);

        RowColPanel mainConfigPanel = new RowColPanel(RowColPanel.AXIS.ROW, RowColPanel.ALIGN.START);
        RowColPanel mainContentPanel = new RowColPanel();

        content.appendChild(mainConfigPanel, 0);
        content.appendChild(new JSeparator(), 0);
        content.appendChild(mainContentPanel, 1);

        // **********************************************************
        // Hydraulic configuration
        // **********************************************************
        hydrauConfParent = new BamItemParent(
                this,
                BamItem.ITEM_TYPE.HYRAULIC_CONFIG);

        hydrauConfParent.comboboxLabel.setText("Configuration hydraulique");
        hydrauConfParent.combobox.setEmptyItemText("Selectionner une configuration hydraulique");
        hydrauConfParent.addChangeListener((e) -> {
            BamItem bamItem = hydrauConfParent.getCurrentBamItem();
            if (bamItem != null) {
                HydraulicConfiguration hydrauConf = (HydraulicConfiguration) bamItem;
                posteriorRatingCurve.setModelDefintion(hydrauConf);
                posteriorRatingCurve.setPriors(hydrauConf);
            }
            checkSync();
        });
        hydrauConfParent.setSyncJsonKeys(new String[] { "name", "description", "ui" }, true);

        // **********************************************************
        // Gaugings
        // **********************************************************
        gaugingsParent = new BamItemParent(
                this,
                BamItem.ITEM_TYPE.GAUGINGS);

        gaugingsParent.comboboxLabel.setText("Jeu de jaugeages");
        gaugingsParent.combobox.setEmptyItemText("Selectionner un jeu de jaugeages");
        gaugingsParent.addChangeListener((e) -> {
            BamItem bamItem = gaugingsParent.getCurrentBamItem();
            if (bamItem != null) {
                posteriorRatingCurve.setCalibrationData((Gaugings) bamItem);
            }
            checkSync();
        });
        gaugingsParent.setSyncJsonKeys(new String[] { "name", "description" }, true);

        // **********************************************************
        // Structural error
        // **********************************************************
        structErrorParent = new BamItemParent(
                this,
                BamItem.ITEM_TYPE.STRUCTURAL_ERROR);

        structErrorParent.comboboxLabel.setText("Modèle d'erreur structurelle");
        structErrorParent.combobox.setEmptyItemText("Selectionner un modèle d'erreur structurelle");
        structErrorParent.addChangeListener((e) -> {
            BamItem bamItem = structErrorParent.getCurrentBamItem();
            if (bamItem != null) {
                posteriorRatingCurve.setStructuralErrorModel((StructuralError) bamItem);
            }
            checkSync();
        });
        structErrorParent.setSyncJsonKeys(new String[] { "name", "description" }, true);
        // **********************************************************

        mainConfigPanel.appendChild(hydrauConfParent.comboboxPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(gaugingsParent.comboboxPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(structErrorParent.comboboxPanel, 0);

        posteriorRatingCurve = new PosteriorRatingCurve();
        posteriorRatingCurve.addPropertyChangeListener("bamHasRun", (e) -> {
            jsonStringBackup = toJSON().toString();

            hydrauConfParent.updateBackup();
            gaugingsParent.updateBackup();
            structErrorParent.updateBackup();

            // checkSynchronicity();
        });
        posteriorRatingCurve.addPropertyChangeListener("stageGridConfigChanged", (e) -> {
            // checkSynchronicity();
        });
        mainContentPanel.appendChild(posteriorRatingCurve);

        setContent(content);

        outdatedInfoPanel = new RowColPanel(RowColPanel.AXIS.COL);
        outdatedInfoPanel.setGap(2);
        outdatedInfoPanel.setColWeight(0, 1);

    }

    @Override
    public McmcConfig getMcmcConfig() {
        return new McmcConfig();
    }

    @Override
    public McmcCookingConfig getMcmcCookingConfig() {
        return new McmcCookingConfig();
    }

    @Override
    public CalibrationConfig getCalibrationConfig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCalibrationConfig'");
    }

    @Override
    public boolean isCalibrated() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isCalibrated'");
    }

    @Override
    public CalibrationResult getCalibrationResults() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCalibrationResults'");
    }

    @Override
    public void parentHasChanged(BamItem parent) {
        System.out.println("PARENT HAS CHANGED DECTECTED FROM '" + this + "'");
        // checkSynchronicity();

    }

    private void checkSync() {
        List<WarningAndActions> warnings = new ArrayList<>();
        warnings.addAll(hydrauConfParent.checkSync());
        warnings.addAll(gaugingsParent.checkSync());
        warnings.addAll(structErrorParent.checkSync());

        posteriorRatingCurve.outdatedPanel.clear();
        outdatedInfoPanel.clear();
        if (warnings.size() > 0) {

            JLabel outdatedMainLabel = new JLabel();
            outdatedMainLabel.setForeground(Color.RED);
            outdatedMainLabel.setFont(outdatedMainLabel.getFont().deriveFont(Font.BOLD));
            outdatedMainLabel.setText("Résultats obsolètes!");
            outdatedInfoPanel.appendChild(outdatedMainLabel);
            for (WarningAndActions w : warnings) {
                outdatedInfoPanel.appendChild(w);
            }
            posteriorRatingCurve.outdatedPanel.appendChild(outdatedInfoPanel);
        }

        posteriorRatingCurve.updateUI();

    }

    @Override
    public String[] getTempDataFileNames() {
        String priorRatingCurveZipFileName = posteriorRatingCurve.getBamRunZipFileName();
        return priorRatingCurveZipFileName == null ? new String[] {} : new String[] { priorRatingCurveZipFileName };
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put("name", getName());
        json.put("description", getDescription());

        json.put("hydrauConfig", hydrauConfParent.toJSON());
        json.put("gaugings", gaugingsParent.toJSON());
        json.put("structError", structErrorParent.toJSON());

        RatingCurveStageGrid ratingCurveGrid = posteriorRatingCurve.getRatingCurveStageGrid();
        JSONObject jsonStageGridConfig = new JSONObject();
        jsonStageGridConfig.put("min", ratingCurveGrid.getMinValue());
        jsonStageGridConfig.put("max", ratingCurveGrid.getMaxValue());
        jsonStageGridConfig.put("step", ratingCurveGrid.getStepValue());

        json.put("stageGridConfig", jsonStageGridConfig);

        json.put("bamRunZipFileName", posteriorRatingCurve.getBamRunZipFileName());

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json.has("name")) {
            setName(json.getString("name"));
        }
        if (json.has("description")) {
            setDescription(json.getString("description"));
        }
        if (json.has("hydrauConfig")) {
            hydrauConfParent.fromJSON(json.getJSONObject("hydrauConfig"));
        }

        if (json.has("gaugings")) {
            gaugingsParent.fromJSON(json.getJSONObject("gaugings"));
        }

        if (json.has("structError")) {
            structErrorParent.fromJSON(json.getJSONObject("structError"));
        }

        if (json.has("stageGridConfig")) {
            RatingCurveStageGrid ratingCurveGrid = posteriorRatingCurve.getRatingCurveStageGrid();
            JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
            ratingCurveGrid.setMinValue(stageGridJson.getDouble("min"));
            ratingCurveGrid.setMaxValue(stageGridJson.getDouble("max"));
            ratingCurveGrid.setStepValue(stageGridJson.getDouble("step"));
        }

        if (json.has("bamRunZipFileName")) {
            String bamRunZipFileName = json.getString("bamRunZipFileName");
            posteriorRatingCurve.setBamRunZipFileName(bamRunZipFileName);
        }

    }

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {

        hydrauConfParent.updateCombobox(bamItemList);
        gaugingsParent.updateCombobox(bamItemList);
        structErrorParent.updateCombobox(bamItemList);

        // checkSynchronicity();
    }

}
