package org.baratinage.ui.baratin;

import javax.swing.Icon;
import javax.swing.JButton;
import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.baratin_spd.PeriodSetsPanel;
import org.baratinage.ui.baratin.hydraulic_configuration.PriorRatingCurve;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.HydraulicControlPanels;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.container.TitledPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.json.JSONFilter;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONObject;

public class HydraulicConfiguration
        extends BamItem
        implements IModelDefinition, IPriors {

    private final TitledPanel controlMatrixTitledPanel;
    private final TitledPanel periodSetTitledPanel;
    private final TitledPanel hydraulicControlsTitledPanel;
    private final TitledPanel priorRatingCurveTitledPanel;

    private boolean priorRatingCurveMode = false;

    public final PeriodSetsPanel periodSetsPanel;
    public final ControlMatrix controlMatrix;
    public final HydraulicControlPanels hydraulicControls;
    public final PriorRatingCurve<HydraulicConfiguration> priorRatingCurve;

    public final RunBam runBam;

    public static final Icon controlMatrixIcon = AppSetup.ICONS
            .getCustomAppImageIcon("control_matrix.svg");
    public static final Icon priorSpecificationIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_densities.svg");
    public static final Icon priorRatingCurveIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_rating_curve.svg");
    public static final Icon periodSetIcon = AppSetup.ICONS
            .getCustomAppImageIcon("period_set.svg");

    public HydraulicConfiguration(String uuid, BaratinProject project) {
        super(BamItemType.HYDRAULIC_CONFIG, uuid, project);

        periodSetsPanel = new PeriodSetsPanel();

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

        controlMatrixTitledPanel = new TitledPanel(controlMatrix);
        controlMatrixTitledPanel.setIcon(controlMatrixIcon);
        periodSetTitledPanel = new TitledPanel(periodSetsPanel);
        periodSetTitledPanel.setIcon(periodSetIcon);
        hydraulicControlsTitledPanel = new TitledPanel(hydraulicControls);
        hydraulicControlsTitledPanel.setIcon(priorSpecificationIcon);
        priorRatingCurveTitledPanel = new TitledPanel(priorRatingCurve);
        priorRatingCurveTitledPanel.setIcon(priorRatingCurveIcon);

        SimpleFlowPanel leftSide = new SimpleFlowPanel(true);
        leftSide.addChild(controlMatrixTitledPanel.getTitledPanel(), true);
        leftSide.addChild(periodSetTitledPanel.getTitledPanel(), true);

        SplitContainer modelAndPriorsPanel = new SplitContainer(
                leftSide,
                hydraulicControlsTitledPanel.getTitledPanel(),
                true,
                0.30f);

        SimpleFlowPanel priorRatingCurvePanel = priorRatingCurveTitledPanel.getTitledPanel();
        priorRatingCurvePanel.setVisible(false);

        SimpleFlowPanel contentPanel = new SimpleFlowPanel(true);
        contentPanel.addChild(modelAndPriorsPanel, true);
        contentPanel.addChild(priorRatingCurvePanel, true);

        JButton modeToggleButton = new JButton();
        modeToggleButton.addActionListener((e) -> {
            priorRatingCurveMode = !priorRatingCurveMode;
            modelAndPriorsPanel.setVisible(!priorRatingCurveMode);
            priorRatingCurvePanel.setVisible(priorRatingCurveMode);
            updateModeToggleButton(modeToggleButton);
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        SimpleFlowPanel mainContainer = new SimpleFlowPanel(true);
        mainContainer.addChild(modeToggleButton, 0, 5, 5, SimpleFlowPanel.ALIGN.START);
        mainContainer.addChild(contentPanel, true);
        setContent(mainContainer);

        boolean[][] mat = controlMatrix.getControlMatrix();
        hydraulicControls.setHydraulicControls(mat.length);

        T.updateHierarchy(this, controlMatrix);
        T.updateHierarchy(this, periodSetsPanel);
        T.updateHierarchy(this, hydraulicControls);
        T.updateHierarchy(this, priorRatingCurve);
        T.t(this, () -> {
            controlMatrixTitledPanel.setText(T.html("control_matrix"));
            periodSetTitledPanel.setText(T.html("period_sets_title"));
            hydraulicControlsTitledPanel.setText(T.html("prior_parameter_specification"));
            priorRatingCurveTitledPanel.setText(T.html("prior_rating_curve"));
            updateModeToggleButton(modeToggleButton);
        });
    }

    private void updateModeToggleButton(JButton button) {
        if (priorRatingCurveMode) {
            button.setText(T.html("hydraulic_config"));
            button.setIcon(BamItemType.HYDRAULIC_CONFIG.getIcon());
        } else {
            button.setText(T.html("prior_rating_curve"));
            button.setIcon(priorRatingCurveIcon);
        }
    }

    @Override
    public String getModelId() {
        return "BaRatin";
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
        return ControlMatrix.toXtra(matrix);
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
                    "stageGridConfig", "bamRunId");

            JSONObject priorRatingCurveBackup = config.JSON.optJSONObject("backup");
            if (priorRatingCurveBackup != null) {
                try {
                    priorRatingCurveBackup = priorRatingCurveBackup.getJSONObject("jsonObject");
                    // model definition:
                    JSONObject modelDefinitionJson = BamConfig.getConfig((IModelDefinition) this);
                    int nParameters = priorRatingCurveBackup.getJSONObject("hydraulicControls").getJSONArray("controls")
                            .length() * 3;
                    modelDefinitionJson.put("nParameters", nParameters);
                    String xTraJsonString = priorRatingCurveBackup.getJSONObject("controlMatrix")
                            .getString("controlMatrixString");
                    boolean[][] ctrlMatrix = ControlMatrix.fromXtraJsonString(xTraJsonString);
                    String xTra = ControlMatrix.toXtra(ctrlMatrix);
                    modelDefinitionJson.put("xTra", xTra);
                    // priors:
                    JSONObject hydraulicControlsJson = priorRatingCurveBackup.getJSONObject("hydraulicControls");
                    HydraulicControlPanels hcp = new HydraulicControlPanels();
                    hcp.fromJSON(hydraulicControlsJson);
                    JSONObject priorsJson = BamConfig.getConfig(hcp);
                    // stage gris config
                    JSONObject stageGridConfigJson = priorRatingCurveBackup.getJSONObject("stageGridConfig");
                    // fixing priorRatingCurveJson
                    JSONObject priorRatingCurveBackupJson = new JSONObject();
                    priorRatingCurveBackupJson.put("modelDefinition", modelDefinitionJson);
                    priorRatingCurveBackupJson.put("priors", priorsJson);
                    priorRatingCurveBackupJson.put("stageGridConfig", stageGridConfigJson);
                    priorRatingCurveJson.put("backup", priorRatingCurveBackupJson);
                } catch (Exception e) {
                    ConsoleLogger.error(e);
                }
            }
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

        TimedActions.throttle(ID, AppSetup.CONFIG.THROTTLED_DELAY_MS, priorRatingCurve::checkSync);
    }

}
