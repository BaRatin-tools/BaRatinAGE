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

public class RatingCurve extends BaRatinItem implements ICalibratedModel, IMcmc {

    static private final String defaultNameTemplate = "Courbe de tarage #%s";
    static private int nInstance = 0;

    BamItemCombobox hydraulicConfigComboBox;
    HydraulicConfiguration hydraulicConfig;

    RatingCurveStageGrid ratingCurveGrid;

    PriorRatingCurve priorRatingCurve;
    PosteriorRatingCurve posteriorRatingCurve;

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

        RowColPanel content = new RowColPanel(RowColPanel.AXIS.COL);

        RowColPanel mainConfigPanel = new RowColPanel();

        RowColPanel hydraulicConfigPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        hydraulicConfigPanel.setGap(5);
        hydraulicConfigPanel.setPadding(5);

        hydraulicConfigPanel.appendChild(new JLabel("Configuration hydraulique"));
        hydraulicConfigComboBox = new BamItemCombobox();
        hydraulicConfigPanel.appendChild(hydraulicConfigComboBox, 0);
        hydraulicConfigComboBox.addActionListener(e -> {
            BamItem selectedHydraulicConf = (BamItem) hydraulicConfigComboBox.getSelectedItem();
            if (hydraulicConfig != null) {
                if (!hydraulicConfig.equals(selectedHydraulicConf)) {
                    setHydraulicConfig((HydraulicConfiguration) selectedHydraulicConf);
                }
            } else {
                setHydraulicConfig((HydraulicConfiguration) selectedHydraulicConf);
            }
        });

        ratingCurveGrid = new RatingCurveStageGrid();
        mainConfigPanel.appendChild(hydraulicConfigPanel);
        mainConfigPanel.appendChild(new JSeparator(JSeparator.VERTICAL), 0);
        mainConfigPanel.appendChild(ratingCurveGrid);

        priorRatingCurve = new PriorRatingCurve();
        priorRatingCurve.setPredictionDataProvider(ratingCurveGrid);

        posteriorRatingCurve = new PosteriorRatingCurve();

        JTabbedPane ratingCurves = new JTabbedPane();
        ratingCurves.add("<html><i>a priori</i>&nbsp;&nbsp;</html>", priorRatingCurve);
        ratingCurves.add("<html><i>a posteriori</i>&nbsp;&nbsp;</html>", posteriorRatingCurve);

        content.appendChild(mainConfigPanel, 0);
        content.appendChild(new JSeparator(), 0);
        content.appendChild(ratingCurves, 1, 5);

        setContent(content);

        BamItemList siblings = getSiblings();
        siblings.addChangeListener(s -> {
            updateHydraulicConfigCombobox();
        });
        updateHydraulicConfigCombobox();

    }

    private void setHydraulicConfig(HydraulicConfiguration newHydraulicConfig) {
        if (hydraulicConfig != null) {
            hydraulicConfig.removeChild(this);
        }
        if (newHydraulicConfig == null) {
            hydraulicConfig = null;
            priorRatingCurve.setModelDefintionProvider(hydraulicConfig);
            priorRatingCurve.setPriorsProvider(hydraulicConfig);
            return;
        }
        hydraulicConfig = newHydraulicConfig;
        priorRatingCurve.setModelDefintionProvider(hydraulicConfig);
        priorRatingCurve.setPriorsProvider(hydraulicConfig);
        hydraulicConfig.addChild(this);

    }

    public void updateHydraulicConfigCombobox() {
        BamItemList siblings = getSiblings();
        if (siblings != null) {
            hydraulicConfigComboBox.syncWithBamItemList(
                    siblings
                            .filterByType(HydraulicConfiguration.TYPE));
        }
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
        System.out.println("PARENT HAS CHANGED - " + parent);
        if (parent.type == HydraulicConfiguration.TYPE) {

        }
    }

    @Override
    public JSONObject toJSON() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJSON'");
    }

    @Override
    public void fromJSON(JSONObject jsonString) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    }

}
