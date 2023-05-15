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
    // private StructuralError structError;

    private RatingCurveStageGrid ratingCurveGrid;
    private PosteriorRatingCurve posteriorRatingCurve;

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    public RatingCurve() {
        super(TYPE);
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

        RowColPanel hydraulicConfigPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        hydraulicConfigPanel.setGap(5);
        hydraulicConfigPanel.setPadding(5);

        JLabel hydraulicConfigLabel = new JLabel("Configuration hydraulique");
        hydraulicConfigPanel.appendChild(hydraulicConfigLabel);
        hydraulicConfigComboBox = new BamItemCombobox("Selectionner une configuration hydraulique");
        hydraulicConfigPanel.appendChild(hydraulicConfigComboBox, 0);

        hydraulicConfigComboBox.addActionListener(e -> {
            BamItem selectedHydraulicConf = (BamItem) hydraulicConfigComboBox.getSelectedItem();
            if (selectedHydraulicConf == null) {
                setHydraulicConfig(null);
                return;
            }
            setHydraulicConfig((HydraulicConfiguration) selectedHydraulicConf);
        });

        RowColPanel gaugingsPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        gaugingsPanel.setGap(5);
        gaugingsPanel.setPadding(5);

        JLabel gaugingsLabel = new JLabel("Jeu de jaugeages");
        gaugingsPanel.appendChild(gaugingsLabel);
        gaugingsComboBox = new BamItemCombobox("Selectionner un jeu de jaugeages");
        gaugingsPanel.appendChild(gaugingsComboBox, 0);

        gaugingsComboBox.addActionListener(e -> {
            BamItem selectedHydraulicConf = (BamItem) gaugingsComboBox.getSelectedItem();
            if (selectedHydraulicConf == null) {
                setHydraulicConfig(null);
                return;
            }
            setGaugings((Gaugings) selectedHydraulicConf);
        });

        RowColPanel structErrorPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        structErrorPanel.setGap(5);
        structErrorPanel.setPadding(5);

        JLabel structErrorLabel = new JLabel("Modèle d'erreur structurelle");
        structErrorPanel.appendChild(structErrorLabel);
        structErrorComboBox = new BamItemCombobox("Selectionner un modèle d'erreur structurelle");
        structErrorPanel.appendChild(structErrorComboBox, 0);

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

    private void setHydraulicConfig(HydraulicConfiguration newHydraulicConfig) {
        if (hydraulicConfig != null) {
            hydraulicConfig.removeBamItemChild(this);
        }
        if (newHydraulicConfig == null) {
            hydraulicConfig = null;
            return;
        }
        hydraulicConfig = newHydraulicConfig;
        hydraulicConfig.addBamItemChild(this);
    }

    private void setGaugings(Gaugings newGaugings) {
        if (gaugings != null) {
            gaugings.removeBamItemChild(this);
        }
        if (newGaugings == null) {
            gaugings = null;
            return;
        }
        gaugings = newGaugings;
        gaugings.addBamItemChild(this);
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
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        json.put("name", getName());
        json.put("description", getName());
        json.put("hydraulicConfigurationId", hydraulicConfig != null ? hydraulicConfig.getUUID() : null);

        RatingCurveStageGrid.StageGridConfig stageGridConfig = ratingCurveGrid.getStageGridConfig();
        JSONObject jsonStageGridConfig = new JSONObject();
        jsonStageGridConfig.put("min", stageGridConfig.min());
        jsonStageGridConfig.put("max", stageGridConfig.max());
        jsonStageGridConfig.put("step", stageGridConfig.step());

        json.put("stageGridConfig", jsonStageGridConfig);

        // JSONObject jsonPriorRatingCurve = new JSONObject();
        // jsonPriorRatingCurve.put("zipFile", priorRatingCurve.getBamRunUUID());
        // json.put("priorRatingCurve", jsonPriorRatingCurve);

        return json;
    }

    @Override
    public void fromJSON(JSONObject jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    }

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {
        System.out.println("UPDATING COMBOBOXES ==> " + this);
        BamItemList listOfHydraulicConfigs = bamItemList.filterByType(HydraulicConfiguration.TYPE);
        hydraulicConfigComboBox.syncWithBamItemList(listOfHydraulicConfigs);
        BamItemList listOfGaugingsDataset = bamItemList.filterByType(Gaugings.TYPE);
        gaugingsComboBox.syncWithBamItemList(listOfGaugingsDataset);
    }

    @Override
    public String[] getZipUUIDS() {
        // return new String[] { priorRatingCurve.getBamRunUUID() };
        return new String[] {};
    }
}
