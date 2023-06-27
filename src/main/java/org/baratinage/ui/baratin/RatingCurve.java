package org.baratinage.ui.baratin;

import java.awt.Color;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.App;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemCombobox;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IMcmc;
import org.baratinage.ui.commons.OutOfSyncWarning;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

public class RatingCurve extends BaRatinItem implements ICalibratedModel, IMcmc, BamItemList.BamItemListChangeListener {

    static private final String defaultNameTemplate = "Courbe de tarage #%s";
    static private int nInstance = 0;

    private BamItemCombobox hydraulicConfigComboBox;
    private BamItemCombobox gaugingsComboBox;
    private BamItemCombobox structErrorComboBox;
    private HydraulicConfiguration hydraulicConfig;
    private Gaugings gaugings;
    private StructuralError structError;

    private PosteriorRatingCurve posteriorRatingCurve;

    private GridPanel outdatedInfoPanel;

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
        RowColPanel hydraulicConfigPanel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        hydraulicConfigPanel.setGap(5);
        hydraulicConfigPanel.setPadding(5);

        JLabel hydraulicConfigLabel = new JLabel("Configuration hydraulique");
        hydraulicConfigPanel.appendChild(hydraulicConfigLabel);
        hydraulicConfigComboBox = new BamItemCombobox("Selectionner une configuration hydraulique");
        hydraulicConfigPanel.appendChild(hydraulicConfigComboBox, 0);

        hydraulicConfigComboBox.addActionListener(e -> {
            BamItem selectedBamItem = hydraulicConfigComboBox.getSelectedItem();

            if (hydraulicConfig != null) {
                hydraulicConfig.removeBamItemChild(this);
            }

            if (selectedBamItem == null) {
                posteriorRatingCurve.setModelDefintion(null);
                posteriorRatingCurve.setPriors(null);
                hydraulicConfig = null;
                return;
            }

            hydraulicConfig = (HydraulicConfiguration) selectedBamItem;

            hydraulicConfig.addBamItemChild(this);
            posteriorRatingCurve.setModelDefintion(hydraulicConfig);
            posteriorRatingCurve.setPriors(hydraulicConfig);

            checkSynchronicity();
        });

        // **********************************************************
        // Gaugings
        // **********************************************************
        RowColPanel gaugingsPanel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        gaugingsPanel.setGap(5);
        gaugingsPanel.setPadding(5);

        JLabel gaugingsLabel = new JLabel("Jeu de jaugeages");
        gaugingsPanel.appendChild(gaugingsLabel);
        gaugingsComboBox = new BamItemCombobox("Selectionner un jeu de jaugeages");
        gaugingsPanel.appendChild(gaugingsComboBox, 0);

        gaugingsComboBox.addActionListener(e -> {
            BamItem selectedBamItem = gaugingsComboBox.getSelectedItem();

            if (gaugings != null) {
                gaugings.removeBamItemChild(this);
            }

            if (selectedBamItem == null) {
                posteriorRatingCurve.setCalibrationData(null);
                gaugings = null;
                return;
            }

            gaugings = (Gaugings) selectedBamItem;
            gaugings.addBamItemChild(this);
            posteriorRatingCurve.setCalibrationData(gaugings);

            checkSynchronicity();
        });

        // **********************************************************
        // Structural error
        // **********************************************************
        RowColPanel structErrorPanel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        structErrorPanel.setGap(5);
        structErrorPanel.setPadding(5);
        JLabel structErrorLabel = new JLabel("Modèle d'erreur structurelle");
        structErrorPanel.appendChild(structErrorLabel);
        structErrorComboBox = new BamItemCombobox("Selectionner un modèle d'erreur structurelle");
        structErrorPanel.appendChild(structErrorComboBox, 0);
        structErrorComboBox.addActionListener(e -> {
            BamItem selectedBamItem = structErrorComboBox.getSelectedItem();

            if (structError != null) {
                structError.removeBamItemChild(this);
            }

            if (selectedBamItem == null) {
                posteriorRatingCurve.setStructuralErrorModel(null);
                structError = null;
                return;
            }

            structError = (StructuralError) selectedBamItem;
            structError.addBamItemChild(this);
            posteriorRatingCurve.setStructuralErrorModel(structError);

            checkSynchronicity();
        });
        // **********************************************************

        mainConfigPanel.appendChild(hydraulicConfigPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(gaugingsPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(structErrorPanel, 0);

        posteriorRatingCurve = new PosteriorRatingCurve();
        posteriorRatingCurve.addPropertyChangeListener("bamHasRun", (e) -> {
            hydraulicConfig.createBackup("post_rc_" + ID);
            gaugings.createBackup("post_rc_" + ID);
            structError.createBackup("post_rc_" + ID);
            createBackup("post_rc");
            checkSynchronicity();
        });
        posteriorRatingCurve.addPropertyChangeListener("stageGridConfigChanged", (e) -> {
            checkSynchronicity();
        });
        mainContentPanel.appendChild(posteriorRatingCurve);

        setContent(content);

        outdatedInfoPanel = new GridPanel();
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
        checkSynchronicity();
    }

    private HydraulicConfiguration getBackupHydraulicConfiguration() {
        JSONObject json = getBackup("post_rc");
        if (json.has("hydraulicConfigurationId")) {
            BamItem backupHydraulicConfig = hydraulicConfigComboBox.getBamItemWithId(
                    json.getString("hydraulicConfigurationId"));
            if (backupHydraulicConfig != null) {

                return (HydraulicConfiguration) backupHydraulicConfig;
            }
        }
        return null;
    }

    // FIXME: is there some refactoring possible with
    // getBackupHydraulicConfiguration()?
    private Gaugings getBackupGaugings() {
        JSONObject json = getBackup("post_rc");
        if (json.has("gaugingsId")) {
            BamItem backupGaugings = gaugingsComboBox.getBamItemWithId(
                    json.getString("gaugingsId"));
            if (backupGaugings != null) {
                return (Gaugings) backupGaugings;
            }
        }
        return null;
    }

    public void checkSynchronicity() {

        outdatedInfoPanel.clear();
        boolean isOutdated = false;
        int insertionIndex = 1;

        // ----------------------------------------------------------
        // check if comboboxes have changed
        boolean ignoreHydraulicConfigCheck = false;
        boolean ignoreGaugingsCheck = false;
        if (hasBackup("post_rc")) {
            if (!isBackupInSyncIncludingKeys("post_rc", new String[] { "hydraulicConfigurationId" })) {
                HydraulicConfiguration backupHydraulicConfig = getBackupHydraulicConfiguration();

                isOutdated = true;

                OutOfSyncWarning outdatedHydrauConf = new OutOfSyncWarning(true);
                String message = "Vous avez changé de configuration hydraulique!";

                outdatedHydrauConf.setMessageText(message);
                outdatedHydrauConf.setCancelButtonText("Annuler");
                outdatedHydrauConf.setCancelButtonEnable(backupHydraulicConfig != null);
                outdatedHydrauConf.addActionListener((e) -> {
                    if (backupHydraulicConfig != null) {
                        hydraulicConfigComboBox.setSelectedItem(backupHydraulicConfig);
                        createBackup("post_rc");
                    }
                });

                outdatedInfoPanel.insertChild(outdatedHydrauConf, 0, insertionIndex);
                insertionIndex++;

                ignoreHydraulicConfigCheck = true;
            }
            if (!isBackupInSyncIncludingKeys("post_rc", new String[] { "gaugingsId" })) {
                Gaugings backupGaugings = getBackupGaugings();

                isOutdated = true;

                OutOfSyncWarning outdatedGaugings = new OutOfSyncWarning(true);
                String message = "Vous avez changé de configuration hydraulique!";

                outdatedGaugings.setMessageText(message);
                outdatedGaugings.setCancelButtonText("Annuler");
                outdatedGaugings.setCancelButtonEnable(backupGaugings != null);
                outdatedGaugings.addActionListener((e) -> {
                    if (backupGaugings != null) {
                        gaugingsComboBox.setSelectedItem(backupGaugings);
                        createBackup("post_rc");
                    }
                });

                outdatedInfoPanel.insertChild(outdatedGaugings, 0, insertionIndex);
                insertionIndex++;

                ignoreGaugingsCheck = true;
            }
        }

        // synchronicity with hydraulic configuration
        if (hydraulicConfig != null && !ignoreHydraulicConfigCheck) {

            if (hydraulicConfig.hasBackup("post_rc_" + ID)) {
                String[] keysToIgnore = new String[] { "ui", "name", "description", "bamRunZipFileName" };
                if (!hydraulicConfig.isBackupInSyncIgnoringKeys("post_rc_" + ID, keysToIgnore)) {
                    isOutdated = true;
                    OutOfSyncWarning outdatedHydrauConf = new OutOfSyncWarning(true);
                    outdatedHydrauConf
                            .setMessageText("La configuration hydraulique a été modifiée!");
                    outdatedHydrauConf.setCancelButtonText(
                            "Annuler les modifications (duplique de la configuration hydraulique sans les modifications)");
                    outdatedHydrauConf.addActionListener((e) -> {
                        JSONObject backupJson = hydraulicConfig.getBackup("post_rc_" + ID);
                        if (backupJson != null) {
                            BaratinProject project = (BaratinProject) App.MAIN_FRAME.getCurrentProject();
                            HydraulicConfiguration duplicatedHydrauConf = hydraulicConfig
                                    .clone(UUID.randomUUID().toString());
                            project.addHydraulicConfig(duplicatedHydrauConf);

                            // FIXME: everything that has changed since bam has run that isn't
                            // part of the out of sync identification must be reset manually.
                            // Here this is shown with name, description and bamRunZipFileName
                            // fields, but it should also be
                            // the case of the UI component...

                            // A possible solution would be to:
                            // - in fromJSON(), make sure that has(key) is used for every item
                            // - filter out the json object beforehand
                            // see below.

                            String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
                            String name = duplicatedHydrauConf.getName() + " (copie " + timeStamp + ")";

                            JSONObject json = hydraulicConfig.getBackup("post_rc_" + ID);
                            json.remove("name");
                            json.remove("description");
                            json.remove("bamRunZipFileName");
                            duplicatedHydrauConf.fromJSON(json);
                            duplicatedHydrauConf.setName(name);

                            hydraulicConfigComboBox.setSelectedItem(duplicatedHydrauConf);

                            createBackup("post_rc");

                            project.setCurrentBamItem(this);
                            checkSynchronicity();
                        }
                    });
                    outdatedInfoPanel.insertChild(outdatedHydrauConf, 0, insertionIndex);
                    insertionIndex++;
                }

            }
        }

        // synchronicity with gaugings
        if (gaugings != null && !ignoreGaugingsCheck) {
            if (gaugings.hasBackup("post_rc_" + ID)) {

                // gaugings.
                String[] keysToIgnore = new String[] { "name", "description" };
                if (!gaugings.isBackupInSyncIgnoringKeys("post_rc_" + ID, keysToIgnore)) {
                    isOutdated = true;
                    OutOfSyncWarning outdatedGaugings = new OutOfSyncWarning(true);
                    outdatedGaugings
                            .setMessageText("Le jeu de jaugeages a été modifié!");
                    outdatedGaugings.setCancelButtonText(
                            "Annuler les modifications (duplique le jeu de jaugeages sans les modifications)");
                    outdatedGaugings.addActionListener((e) -> {
                        JSONObject backupJson = gaugings.getBackup("post_rc_" + ID);
                        if (backupJson != null) {
                            BaratinProject project = (BaratinProject) App.MAIN_FRAME.getCurrentProject();
                            Gaugings duplicatedGaugings = gaugings.clone(UUID.randomUUID().toString());
                            project.addGaugings(duplicatedGaugings);

                            // FIXME: same as above

                            String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
                            String name = duplicatedGaugings.getName() + " (copie " + timeStamp + ")";

                            JSONObject json = gaugings.getBackup("post_rc_" + ID);
                            json.remove("name");
                            json.remove("description");
                            duplicatedGaugings.fromJSON(json);
                            duplicatedGaugings.setName(name);

                            gaugingsComboBox.setSelectedItem(duplicatedGaugings);

                            createBackup("post_rc");

                            project.setCurrentBamItem(this);
                            checkSynchronicity();
                        }
                    });
                    outdatedInfoPanel.insertChild(outdatedGaugings, 0, insertionIndex);
                    insertionIndex++;
                }

            }
        }

        // synchronicity with stage grid
        if (hasBackup("post_rc")) {
            String[] keysToInclude = new String[] { "stageGridConfig" };
            if (!isBackupInSyncIncludingKeys("post_rc", keysToInclude)) {
                isOutdated = true;
                OutOfSyncWarning stageGridConfigWarning = new OutOfSyncWarning(true);
                stageGridConfigWarning.setMessageText("La grille de hauteur d'eau a été modifiée!");
                stageGridConfigWarning.setCancelButtonText("Annuler les modifications");
                stageGridConfigWarning.addActionListener((e) -> {
                    JSONObject backup = getBackup("post_rc");
                    JSONObject stageGridJson = backup.getJSONObject("stageGridConfig");
                    RatingCurveStageGrid ratingCurveGrid = posteriorRatingCurve.getRatingCurveStageGrid();
                    ratingCurveGrid.setMinValue(stageGridJson.getDouble("min"));
                    ratingCurveGrid.setMaxValue(stageGridJson.getDouble("max"));
                    ratingCurveGrid.setStepValue(stageGridJson.getDouble("step"));
                    checkSynchronicity();
                });
                outdatedInfoPanel.insertChild(stageGridConfigWarning, 0, insertionIndex);
                insertionIndex++;
            }
        }

        if (isOutdated) {
            JLabel outdatedMainLabel = new JLabel();
            outdatedMainLabel.setForeground(Color.RED);
            outdatedMainLabel.setFont(outdatedMainLabel.getFont().deriveFont(Font.BOLD));
            outdatedMainLabel.setText("Résultats obsolètes!");
            outdatedInfoPanel.insertChild(outdatedMainLabel, 0, 0);

            posteriorRatingCurve.outdatedPanel.clear();
            posteriorRatingCurve.outdatedPanel.appendChild(outdatedInfoPanel);
        }

        outdatedInfoPanel.updateUI();
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
        json.put("hydraulicConfigurationId", hydraulicConfig != null ? hydraulicConfig.ID : null);
        json.put("structuralErrorId", structError != null ? structError.ID : null);
        json.put("gaugingsId", gaugings != null ? gaugings.ID : null);

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

        setName(json.getString("name"));
        setDescription(json.getString("description"));

        hydraulicConfigComboBox.setSelectedItem(
                hydraulicConfigComboBox.getBamItemWithId(
                        json.getString("hydraulicConfigurationId")));
        gaugingsComboBox.setSelectedItem(
                gaugingsComboBox.getBamItemWithId(
                        json.getString("gaugingsId")));
        structErrorComboBox.setSelectedItem(
                structErrorComboBox.getBamItemWithId(
                        json.getString("structuralErrorId")));

        RatingCurveStageGrid ratingCurveGrid = posteriorRatingCurve.getRatingCurveStageGrid();
        JSONObject stageGridJson = json.getJSONObject("stageGridConfig");
        ratingCurveGrid.setMinValue(stageGridJson.getDouble("min"));
        ratingCurveGrid.setMaxValue(stageGridJson.getDouble("max"));
        ratingCurveGrid.setStepValue(stageGridJson.getDouble("step"));

        if (json.has("bamRunZipFileName")) {
            String bamRunZipFileName = json.getString("bamRunZipFileName");
            posteriorRatingCurve.setBamRunZipFileName(bamRunZipFileName);
        }

    }

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {
        System.out.println("UPDATING COMBOBOXES ==> " + this);
        BamItemList listOfHydraulicConfigs = bamItemList.filterByType(ITEM_TYPE.HYRAULIC_CONFIG);
        hydraulicConfigComboBox.syncWithBamItemList(listOfHydraulicConfigs);
        BamItemList listOfGaugingsDataset = bamItemList.filterByType(ITEM_TYPE.GAUGINGS);
        gaugingsComboBox.syncWithBamItemList(listOfGaugingsDataset);
        BamItemList listOfStructuralErrorModels = bamItemList.filterByType(ITEM_TYPE.STRUCTURAL_ERROR);
        structErrorComboBox.syncWithBamItemList(listOfStructuralErrorModels);

        checkSynchronicity();
    }

}
