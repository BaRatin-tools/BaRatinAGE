package org.baratinage.ui.baratin;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.hydraulic_configuration.PriorRatingCurve;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.HydraulicControlPanels;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TitledPanel;
import org.baratinage.ui.container.TitledPanelSplitTabContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONFilter;
import org.json.JSONObject;

public class HydraulicConfigurationBAC extends BamItem
        implements IModelDefinition, IPriors {

    private final TitledPanel controlMatrixTitledPanel;
    private final TitledPanel hydraulicControlsTitledPanel;
    private final TitledPanel priorRatingCurveTitledPanel;

    private final ControlMatrix controlMatrix;
    private final SimpleNumberField maxStageField;
    private final HydraulicControlPanels hydraulicControls;
    public final PriorRatingCurve<HydraulicConfigurationBAC> priorRatingCurve;

    public final RunBam runBam;

    public static final Icon controlMatrixIcon = AppSetup.ICONS
            .getCustomAppImageIcon("control_matrix.svg");
    public static final Icon priorSpecificationIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_densities.svg");
    public static final Icon priorRatingCurveIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_rating_curve.svg");

    public HydraulicConfigurationBAC(String uuid, BaratinProject project) {
        super(BamItemType.HYDRAULIC_CONFIG_BAC, uuid, project); // DIFFERENT

        hydraulicControls = new HydraulicControlPanels(false); // DIFFERENT
        hydraulicControls.addChangeListener((e) -> {
            fireChangeListeners();
        });

        controlMatrix = new ControlMatrix();
        controlMatrix.addChangeListener((e) -> {
            fireChangeListeners();
            hydraulicControls.setHydraulicControls(controlMatrix.getControlMatrix().length);
        });

        priorRatingCurve = new PriorRatingCurve<>(this);
        runBam = priorRatingCurve.runBam;

        JLabel maxStageLabel = new JLabel();
        T.t(this, maxStageLabel, false, "rc_validity_maximum_stage");
        maxStageField = new SimpleNumberField();
        maxStageField.addChangeListener((e) -> {
            fireChangeListeners();
            priorRatingCurve.checkSync();
        });
        RowColPanel maxStagePanel = new RowColPanel();
        maxStagePanel.setGap(5);
        maxStagePanel.setPadding(5);
        maxStagePanel.appendChild(maxStageLabel, 0);
        maxStagePanel.appendChild(maxStageField, 1);

        RowColPanel modelDefPanel = new RowColPanel(RowColPanel.AXIS.COL);
        modelDefPanel.appendChild(controlMatrix, 1);
        modelDefPanel.appendChild(new SimpleSep(), 0);
        modelDefPanel.appendChild(maxStagePanel, 0);

        controlMatrix.addChangeListener((e) -> {
            priorRatingCurve.checkSync();
        });
        hydraulicControls.addChangeListener((e) -> {
            priorRatingCurve.checkSync();
        });

        controlMatrixTitledPanel = new TitledPanel(modelDefPanel);
        controlMatrixTitledPanel.setIcon(controlMatrixIcon);
        hydraulicControlsTitledPanel = new TitledPanel(hydraulicControls);
        hydraulicControlsTitledPanel.setIcon(priorSpecificationIcon);
        priorRatingCurveTitledPanel = new TitledPanel(priorRatingCurve);
        priorRatingCurveTitledPanel.setIcon(priorRatingCurveIcon);

        TitledPanelSplitTabContainer mainContainer = TitledPanelSplitTabContainer
                .build2Left1Right(this,
                        controlMatrixTitledPanel,
                        priorRatingCurveTitledPanel,
                        hydraulicControlsTitledPanel);
        mainContainer.setBreakpoints(1100, 800);
        setContent(mainContainer);

        boolean[][] mat = controlMatrix.getControlMatrix();
        hydraulicControls.setHydraulicControls(mat.length);

        T.updateHierarchy(this, controlMatrix);
        T.updateHierarchy(this, hydraulicControls);
        T.updateHierarchy(this, priorRatingCurve);
        T.t(this, () -> {
            controlMatrixTitledPanel.setText(T.html("control_matrix"));
            hydraulicControlsTitledPanel.setText(T.html("prior_parameter_specification"));
            priorRatingCurveTitledPanel.setText(T.html("prior_rating_curve"));
        });
    }

    @Override
    public Parameter[] getParameters() {
        return hydraulicControls.getParameters();
    }

    @Override
    public String getModelId() {
        return "BaRatinBAC";
    }

    @Override
    public int getNumberOfParameters() {
        int nCtrl = controlMatrix.getNumberOfControls();
        return nCtrl * 3;
    }

    @Override
    public String[] getInputNames() {
        return new String[] { "h" };
    }

    @Override
    public String[] getOutputNames() {
        return new String[] { "Q" };
    }

    @Override
    public String getXtra(String workspace) {
        boolean[][] matrix = this.controlMatrix.getControlMatrix();
        String xtra = "";
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                xtra += matrix[i][j] ? "1 " : "0 ";
            }
            if ((i + 1) != matrix.length) {
                xtra += "\n";
            }
        }
        xtra = xtra + "\n" + maxStageField.getDoubleValue() + " ! hmax";
        return xtra;
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        BamConfig config = new BamConfig(0, TYPE);

        // **********************************************************
        // Control matrix
        config.JSON.put("controlMatrix", controlMatrix.toJSON());

        config.JSON.put("hmax", maxStageField.getDoubleValue());// DIFFERENT

        // **********************************************************
        // Hydraulic controls

        config.JSON.put("hydraulicControls", hydraulicControls.toJSON());

        // **********************************************************
        // prior rating curve configuration
        BamConfig priorRatingCurveConfig = priorRatingCurve.saveConfig(writeFiles);
        config.JSON.put("priorRatingCurve", priorRatingCurveConfig.JSON);
        if (priorRatingCurveConfig.FILE_PATHS.size() > 0) {
            config.FILE_PATHS.addAll(priorRatingCurveConfig.FILE_PATHS);
        }
        return config;
    }

    @Override
    public void load(BamConfig config) {

        if (config.VERSION == -1) {
            JSONObject newJson = JSONFilter.filter(config.JSON,
                    false, false,
                    "controlMatrix", "hydraulicControls");
            JSONObject priorRatingCurveJson = JSONFilter.filter(config.JSON, false, false,
                    "stageGridConfig", "bamRunId", "backup");
            newJson.put("priorRatingCurve", priorRatingCurveJson);
            config = new BamConfig(0);
            for (String key : newJson.keySet()) {
                config.JSON.put(key, newJson.get(key));
            }
        }
        JSONObject json = config.JSON;

        // **********************************************************
        // Control matrix
        if (json.has("controlMatrix")) {
            controlMatrix.fromJSON(json.getJSONObject("controlMatrix"));
        } else {
            ConsoleLogger.log("missing 'controlMatrix'");
        }

        // DIFFERENT
        if (json.has("hmax")) {
            maxStageField.setValue(json.getDouble("hmax"));
        } else {
            ConsoleLogger.log("missing 'hmax'");
        }

        // **********************************************************
        // Hydraulic controls
        if (json.has("hydraulicControls")) {
            hydraulicControls.fromJSON(json.getJSONObject("hydraulicControls"));
        } else {
            ConsoleLogger.log("missing 'hydraulicControls'");
        }

        // **********************************************************
        // prior rating curve configuration
        if (json.has("priorRatingCurve")) {
            priorRatingCurve.loadConfig(json.getJSONObject("priorRatingCurve"));
        } else {
            ConsoleLogger.log("missing 'priorRatingCurve'");
        }

    }

}
