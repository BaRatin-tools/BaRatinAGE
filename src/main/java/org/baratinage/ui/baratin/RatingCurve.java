package org.baratinage.ui.baratin;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemCombobox;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.ICalibratedModel;
import org.baratinage.ui.bam.IMcmc;
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

    public RatingCurve(String uuid) {
        super(ITEM_TYPE.RATING_CURVE, uuid);
        nInstance++;
        setName(String.format(
                defaultNameTemplate,
                nInstance + ""));
        setDescription("");

        setNameFieldLabel("Nom de la courbe de tarage");
        setDescriptionFieldLabel("Description de la courbe de tarage");

        RowColPanel content = new RowColPanel(AXIS.COL);

        RowColPanel mainConfigPanel = new RowColPanel(AXIS.ROW, ALIGN.START);
        RowColPanel mainContentPanel = new RowColPanel();

        content.appendChild(mainConfigPanel, 0);
        content.appendChild(new JSeparator(), 0);
        content.appendChild(mainContentPanel, 1);

        // **********************************************************
        // Hydraulic configuration
        // **********************************************************
        RowColPanel hydraulicConfigPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        hydraulicConfigPanel.setGap(5);
        hydraulicConfigPanel.setPadding(5);

        JLabel hydraulicConfigLabel = new JLabel("Configuration hydraulique");
        hydraulicConfigPanel.appendChild(hydraulicConfigLabel);
        hydraulicConfigComboBox = new BamItemCombobox("Selectionner une configuration hydraulique");
        hydraulicConfigPanel.appendChild(hydraulicConfigComboBox, 0);

        hydraulicConfigComboBox.addActionListener(e -> {
            BamItem selectedBamItem = (BamItem) hydraulicConfigComboBox.getSelectedItem();

            if (selectedBamItem == null) {
                if (hydraulicConfig != null) {
                    hydraulicConfig.removeBamItemChild(this);
                }
                posteriorRatingCurve.setModelDefintion(null);
                posteriorRatingCurve.setPriors(null);
                hydraulicConfig = null;
                return;
            }

            hydraulicConfig = (HydraulicConfiguration) selectedBamItem;
            ;
            hydraulicConfig.addBamItemChild(this);
            posteriorRatingCurve.setModelDefintion(hydraulicConfig);
            posteriorRatingCurve.setPriors(hydraulicConfig);
        });

        // **********************************************************
        // Gaugings
        // **********************************************************
        RowColPanel gaugingsPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        gaugingsPanel.setGap(5);
        gaugingsPanel.setPadding(5);

        JLabel gaugingsLabel = new JLabel("Jeu de jaugeages");
        gaugingsPanel.appendChild(gaugingsLabel);
        gaugingsComboBox = new BamItemCombobox("Selectionner un jeu de jaugeages");
        gaugingsPanel.appendChild(gaugingsComboBox, 0);

        gaugingsComboBox.addActionListener(e -> {
            BamItem selectedBamItem = (BamItem) gaugingsComboBox.getSelectedItem();

            if (selectedBamItem == null) {
                if (gaugings != null) {
                    gaugings.removeBamItemChild(this);
                }
                posteriorRatingCurve.setCalibrationData(null);
                gaugings = null;
                return;
            }

            gaugings = (Gaugings) selectedBamItem;
            gaugings.addBamItemChild(this);
            posteriorRatingCurve.setCalibrationData(gaugings);
        });

        // **********************************************************
        // Structural error
        // **********************************************************
        RowColPanel structErrorPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        structErrorPanel.setGap(5);
        structErrorPanel.setPadding(5);
        JLabel structErrorLabel = new JLabel("Modèle d'erreur structurelle");
        structErrorPanel.appendChild(structErrorLabel);
        structErrorComboBox = new BamItemCombobox("Selectionner un modèle d'erreur structurelle");
        structErrorPanel.appendChild(structErrorComboBox, 0);
        structErrorComboBox.addActionListener(e -> {
            BamItem selectedBamItem = (BamItem) structErrorComboBox.getSelectedItem();

            if (selectedBamItem == null) {
                if (structError != null) {
                    structError.removeBamItemChild(this);
                }
                posteriorRatingCurve.setStructuralErrorModel(null);
                structError = null;
                return;
            }

            structError = (StructuralError) selectedBamItem;
            structError.addBamItemChild(this);
            posteriorRatingCurve.setStructuralErrorModel(structError);

        });
        // **********************************************************

        mainConfigPanel.appendChild(hydraulicConfigPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(gaugingsPanel, 0);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL));
        mainConfigPanel.appendChild(structErrorPanel, 0);

        JTabbedPane ratingCurves = new JTabbedPane();
        mainContentPanel.appendChild(ratingCurves);

        posteriorRatingCurve = new PosteriorRatingCurve();
        ratingCurves.add("<html>Courbe de tarage <i>a posteriori</i>&nbsp;&nbsp;</html>", posteriorRatingCurve);

        setContent(content);
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
        if (parent.equals(hydraulicConfig)) {
            System.out.println("HYDRAULIC CONFIF '" + parent + "'' IS THE PARENT THAT HAS CHANGED");

        }
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
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
        System.out.println("RATING CURVE === " + json.getString("name"));
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
    }

}
