package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JSeparator;

import org.baratinage.App;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.BamItemParent;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IMcmc;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.lg.LgElement;
import org.json.JSONObject;

public class RatingCurve extends BamItem implements ICalibratedModel, IMcmc, BamItemList.BamItemListChangeListener {

    private BamItemParent hydrauConfParent;
    private BamItemParent gaugingsParent;
    private BamItemParent structErrorParent;

    private PosteriorRatingCurve posteriorRatingCurve;

    private RowColPanel outdatedInfoPanel;

    private String jsonStringBackup;

    public RatingCurve(String uuid, BaratinProject project) {
        super(BamItemType.RATING_CURVE, uuid, project);

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
                BamItemType.HYDRAULIC_CONFIG);

        LgElement.registerLabel(hydrauConfParent.comboboxLabel, "ui", "hydraulic_config");
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
        hydrauConfParent.setSyncJsonKeys(new String[] { "ui" }, true);
        hydrauConfParent.setCreateBackupBamItemAction((json) -> {
            BamItem bamItem = project.addHydraulicConfig();
            bamItem.fromJSON(json);
            project.setCurrentBamItem(this);
            return bamItem;
        });
        // **********************************************************
        // Gaugings
        // **********************************************************
        gaugingsParent = new BamItemParent(
                this,
                BamItemType.GAUGINGS);

        LgElement.registerLabel(gaugingsParent.comboboxLabel, "ui", "gaugings");
        gaugingsParent.combobox.setEmptyItemText("Selectionner un jeu de jaugeages");
        gaugingsParent.addChangeListener((e) -> {
            BamItem bamItem = gaugingsParent.getCurrentBamItem();
            if (bamItem != null) {
                posteriorRatingCurve.setCalibrationData((Gaugings) bamItem);
            }
            checkSync();
        });
        gaugingsParent.setCreateBackupBamItemAction((json) -> {
            BamItem bamItem = project.addGaugings();
            bamItem.fromJSON(json);
            project.setCurrentBamItem(this);
            return bamItem;
        });
        // **********************************************************
        // Structural error
        // **********************************************************
        structErrorParent = new BamItemParent(
                this,
                BamItemType.STRUCTURAL_ERROR);

        LgElement.registerLabel(structErrorParent.comboboxLabel, "ui",
                "structural_error_model");
        structErrorParent.combobox.setEmptyItemText("Selectionner un modèle d'erreur structurelle");
        structErrorParent.addChangeListener((e) -> {
            BamItem bamItem = structErrorParent.getCurrentBamItem();
            if (bamItem != null) {
                posteriorRatingCurve.setStructuralErrorModel((StructuralError) bamItem);
            }
            checkSync();
        });
        structErrorParent.setCreateBackupBamItemAction((json) -> {
            BamItem bamItem = project.addGaugings();
            bamItem.fromJSON(json);
            project.setCurrentBamItem(this);
            return bamItem;
        });
        // **********************************************************

        mainConfigPanel.appendChild(hydrauConfParent.comboboxPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(gaugingsParent.comboboxPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(structErrorParent.comboboxPanel, 0);

        posteriorRatingCurve = new PosteriorRatingCurve();
        posteriorRatingCurve.addPropertyChangeListener("bamHasRun", (e) -> {
            JSONObject json = toJSON();
            json.remove("jsonStringBackup");
            jsonStringBackup = json.toString();
            hydrauConfParent.updateBackup();
            gaugingsParent.updateBackup();
            structErrorParent.updateBackup();
            checkSync();
        });
        posteriorRatingCurve.addPropertyChangeListener("stageGridConfigChanged", (e) -> {
            // checkSynchronicity();
        });
        mainContentPanel.appendChild(posteriorRatingCurve);

        setContent(content);

        outdatedInfoPanel = new RowColPanel(RowColPanel.AXIS.COL);
        outdatedInfoPanel.setGap(2);
        outdatedInfoPanel.setColWeight(0, 1);

        onBamItemListChange(PROJECT.BAM_ITEMS);

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

    private void checkSync() {
        List<WarningAndActions> warnings = new ArrayList<>();
        WarningAndActions warning;
        warning = hydrauConfParent.getOutOfSyncWarning();
        if (warning != null) {
            warnings.add(warning);
        }
        warning = gaugingsParent.getOutOfSyncWarning();
        if (warning != null) {
            warnings.add(warning);
        }
        warning = structErrorParent.getOutOfSyncWarning();
        if (warning != null) {
            warnings.add(warning);
        }

        posteriorRatingCurve.outdatedPanel.clear();
        outdatedInfoPanel.clear();
        if (warnings.size() > 0) {
            LgElement.registerButton(posteriorRatingCurve.runBamButton, "ui", "recompute_posterior_rc", true);
            posteriorRatingCurve.runBamButton.setForeground(App.INVALID_COLOR);
            for (WarningAndActions w : warnings) {
                outdatedInfoPanel.appendChild(w);
            }
            posteriorRatingCurve.outdatedPanel.appendChild(outdatedInfoPanel);
        } else {
            LgElement.registerButton(posteriorRatingCurve.runBamButton, "ui", "compute_posterior_rc", true);
            posteriorRatingCurve.runBamButton.setForeground(new JButton().getForeground());
        }

        // since text within warnings changes, it is necessary to
        // call Lg.updateTexts() so changes are accounted for.
        Lg.updateTexts();
        posteriorRatingCurve.updateUI();

    }

    @Override
    public String[] getTempDataFileNames() {
        RunBam runBam = posteriorRatingCurve.getRunBam();
        return runBam == null ? new String[] {} : new String[] { runBam.zipName };
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        json.put("hydrauConfig", hydrauConfParent.toJSON());
        json.put("gaugings", gaugingsParent.toJSON());
        json.put("structError", structErrorParent.toJSON());

        RatingCurveStageGrid ratingCurveGrid = posteriorRatingCurve.getRatingCurveStageGrid();
        JSONObject jsonStageGridConfig = new JSONObject();
        jsonStageGridConfig.put("min", ratingCurveGrid.getMinValue());
        jsonStageGridConfig.put("max", ratingCurveGrid.getMaxValue());
        jsonStageGridConfig.put("step", ratingCurveGrid.getStepValue());

        json.put("stageGridConfig", jsonStageGridConfig);

        RunBam runBam = posteriorRatingCurve.getRunBam();
        if (runBam != null) {
            json.put("bamRunId", runBam.id);
        }

        json.put("jsonStringBackup", jsonStringBackup);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

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

        if (json.has("bamRunId")) {
            String bamRunId = json.getString("bamRunId");
            posteriorRatingCurve.setRunBam(bamRunId);
        }

        if (json.has("jsonStringBackup")) {
            jsonStringBackup = json.getString("jsonStringBackup");
        }

    }

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {
        hydrauConfParent.updateCombobox(bamItemList);
        gaugingsParent.updateCombobox(bamItemList);
        structErrorParent.updateCombobox(bamItemList);
        checkSync();
    }

    @Override
    public RatingCurve clone(String uuid) {
        RatingCurve cloned = new RatingCurve(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

}
