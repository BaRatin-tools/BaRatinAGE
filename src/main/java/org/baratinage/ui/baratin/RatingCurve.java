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
import org.baratinage.ui.commons.WarningAndActions;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.lg.LgElement;
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

    public RatingCurve(String uuid, BaratinProject project) {
        super(BamItemType.RATING_CURVE, uuid, project);
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
                BamItemType.HYDRAULIC_CONFIG);

        Lg.registerLabel(hydrauConfParent.comboboxLabel, "ui", "hydraulic_config");
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
        hydrauConfParent.setCreateBackupBamItemAction((id, json) -> {
            HydraulicConfiguration bamItem = new HydraulicConfiguration(id, (BaratinProject) PROJECT);
            bamItem.fromJSON(json);
            bamItem.addTimeStampToName();
            project.addHydraulicConfig(bamItem);
            project.setCurrentBamItem(this);
        });
        // **********************************************************
        // Gaugings
        // **********************************************************
        gaugingsParent = new BamItemParent(
                this,
                BamItemType.GAUGINGS);

        Lg.registerLabel(gaugingsParent.comboboxLabel, "ui", "gaugings");
        gaugingsParent.combobox.setEmptyItemText("Selectionner un jeu de jaugeages");
        gaugingsParent.addChangeListener((e) -> {
            BamItem bamItem = gaugingsParent.getCurrentBamItem();
            if (bamItem != null) {
                posteriorRatingCurve.setCalibrationData((Gaugings) bamItem);
            }
            checkSync();
        });
        gaugingsParent.setSyncJsonKeys(new String[] { "name", "description" }, true);
        gaugingsParent.setCreateBackupBamItemAction((id, json) -> {
            Gaugings bamItem = new Gaugings(id, (BaratinProject) PROJECT);
            bamItem.fromJSON(json);
            bamItem.addTimeStampToName();
            project.addGaugings(bamItem);
            project.setCurrentBamItem(this);
        });
        // **********************************************************
        // Structural error
        // **********************************************************
        structErrorParent = new BamItemParent(
                this,
                BamItemType.STRUCTURAL_ERROR);

        Lg.registerLabel(structErrorParent.comboboxLabel, "ui", "structural_error_model");
        structErrorParent.combobox.setEmptyItemText("Selectionner un modèle d'erreur structurelle");
        structErrorParent.addChangeListener((e) -> {
            BamItem bamItem = structErrorParent.getCurrentBamItem();
            if (bamItem != null) {
                posteriorRatingCurve.setStructuralErrorModel((StructuralError) bamItem);
            }
            checkSync();
        });
        structErrorParent.setSyncJsonKeys(new String[] { "name", "description" }, true);
        structErrorParent.setCreateBackupBamItemAction((id, json) -> {
            StructuralError bamItem = new StructuralError(id, (BaratinProject) PROJECT);
            bamItem.fromJSON(json);
            bamItem.addTimeStampToName();
            project.addStructuralErrorModel(bamItem);
            project.setCurrentBamItem(this);
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

        onBamItemListChange(PROJECT.getBamItems());

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
        warnings.addAll(hydrauConfParent.checkSync());
        warnings.addAll(gaugingsParent.checkSync());
        warnings.addAll(structErrorParent.checkSync());

        posteriorRatingCurve.outdatedPanel.clear();
        outdatedInfoPanel.clear();
        if (warnings.size() > 0) {
            Lg.register(new LgElement<JButton>(posteriorRatingCurve.runBamButton) {
                @Override
                public void setTranslatedText() {
                    object.setText(Lg.getText("ui", "recompute_posterior_rc", true));
                }
            });
            posteriorRatingCurve.runBamButton.setForeground(App.INVALID_COLOR);
            for (WarningAndActions w : warnings) {
                outdatedInfoPanel.appendChild(w);
            }
            posteriorRatingCurve.outdatedPanel.appendChild(outdatedInfoPanel);
        } else {
            Lg.register(new LgElement<JButton>(posteriorRatingCurve.runBamButton) {
                @Override
                public void setTranslatedText() {
                    object.setText(Lg.getText("ui", "compute_posterior_rc", true));
                }
            });
            posteriorRatingCurve.runBamButton.setForeground(new JButton().getForeground());
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

        json.put("jsonStringBackup", jsonStringBackup);
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

        if (json.has("jsonStringBackup")) {
            jsonStringBackup = json.getString("jsonStringBackup");
        }

    }

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {

        hydrauConfParent.updateCombobox(bamItemList);
        gaugingsParent.updateCombobox(bamItemList);
        structErrorParent.updateCombobox(bamItemList);

    }

    @Override
    public BamItem clone(String uuid) {
        RatingCurve cloned = new RatingCurve(uuid, (BaratinProject) PROJECT);
        cloned.fromFullJSON(toFullJSON());
        return cloned;
    }

}
