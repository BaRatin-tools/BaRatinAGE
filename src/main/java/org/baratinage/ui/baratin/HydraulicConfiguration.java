package org.baratinage.ui.baratin;

import javax.swing.ImageIcon;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.RunBam;

import org.baratinage.ui.baratin.hydraulic_configuration.PriorRatingCurve;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.HydraulicControlPanels;

import org.baratinage.ui.container.TitledPanel;
import org.baratinage.ui.container.TitledPanelSplitTabContainer;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.json.JSONFilter;

import org.json.JSONObject;

public class HydraulicConfiguration
        extends BamItem
        implements IModelDefinition, IPriors {
    private final TitledPanel controlMatrixTitledPanel;
    private final TitledPanel hydraulicControlsTitledPanel;
    private final TitledPanel priorRatingCurveTitledPanel;

    private final ControlMatrix controlMatrix;
    private final HydraulicControlPanels hydraulicControls;

    private final PriorRatingCurve<HydraulicConfiguration> priorRatingCurve;

    public final RunBam runBam;

    public static final ImageIcon controlMatrixIcon = AppSetup.ICONS.getCustomAppImageIcon("control_matrix.svg");
    public static final ImageIcon priorSpecificationIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_densities.svg");
    public static final ImageIcon priorRatingCurveIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_rating_curve.svg");

    public HydraulicConfiguration(String uuid, BaratinProject project) {
        super(BamItemType.HYDRAULIC_CONFIG, uuid, project);

        hydraulicControls = new HydraulicControlPanels();
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

        controlMatrix.addChangeListener((e) -> {
            priorRatingCurve.checkSync();
        });
        hydraulicControls.addChangeListener((e) -> {
            priorRatingCurve.checkSync();
        });

        Misc.setCompSize(
                controlMatrix,
                250, 500,
                150, 300);

        controlMatrixTitledPanel = new TitledPanel(controlMatrix);
        controlMatrixTitledPanel.setIcon(controlMatrixIcon);
        hydraulicControlsTitledPanel = new TitledPanel(hydraulicControls);
        hydraulicControlsTitledPanel.setIcon(priorSpecificationIcon);
        priorRatingCurveTitledPanel = new TitledPanel(priorRatingCurve);
        priorRatingCurveTitledPanel.setIcon(priorRatingCurveIcon);

        TitledPanelSplitTabContainer mainContainer = TitledPanelSplitTabContainer.build2Left1Right(this,
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
    public String getModelId() {
        return "BaRatin";
    }

    @Override
    public String[] getParameterNames() {
        Parameter[] parameters = getParameters();
        if (parameters == null) {
            return null;
        }
        String[] parameterNames = new String[parameters.length];
        for (int k = 0; k < parameters.length; k++) {
            parameterNames[k] = parameters[k].name;
        }
        return parameterNames;
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
        return xtra;
    }

    @Override
    public Parameter[] getParameters() {
        return hydraulicControls.getParameters();
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        BamConfig config = new BamConfig(0);

        // **********************************************************
        // Control matrix
        config.JSON.put("controlMatrix", controlMatrix.toJSON());

        // **********************************************************
        // Hydraulic controls

        config.JSON.put("hydraulicControls", hydraulicControls.toJSON());

        // **********************************************************
        // prior rating curve configuration
        BamConfig priorRatingCurveConfig = priorRatingCurve.saveConfig();
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
