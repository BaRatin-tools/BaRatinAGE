package org.baratinage.ui.baratin.baratin_qfh;

import javax.swing.Icon;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.baratin.hydraulic_configuration.PriorRatingCurve;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.TitledPanel;
import org.baratinage.ui.container.TitledPanelSplitTabContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.ui.container.RowColPanel.ALIGN;
import org.baratinage.ui.container.RowColPanel.AXIS;
import org.json.JSONObject;

public class HydraulicConfigurationQFH extends BamItem
        implements IModelDefinition, IPriors {

    public static final Icon equationQFHIcon = AppSetup.ICONS
            .getCustomAppImageIcon("rating_curve_equation.svg");
    public static final Icon priorSpecificationIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_densities.svg");
    public static final Icon priorRatingCurveIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_rating_curve.svg");

    private final PriorRatingCurve<HydraulicConfigurationQFH> priorRatingCurve;

    private final RowColPanel priorsPanel;

    private final QFHModelDefinition modelDefinition;
    private QFHPriors priors;

    public HydraulicConfigurationQFH(String uuid, BamProject project) {
        super(BamItemType.HYDRAULIC_CONFIG_QFH, uuid, project);

        modelDefinition = new QFHModelDefinition();
        priorRatingCurve = new PriorRatingCurve<>(this);

        priorsPanel = new RowColPanel(AXIS.COL, ALIGN.STRETCH);

        modelDefinition.addChangeListener(l -> {
            fireChangeListeners();
            priorRatingCurve.checkSync();
            if (!modelDefinition.isModelDefinitionValid()) {
                priors = null;
                priorsPanel.clear();
                priorsPanel.updateUI();
                return;
            }
            if (!modelDefinition.priorsPanel.equals(priors)) {
                priors = modelDefinition.priorsPanel;
                priorsPanel.clear();
                priorsPanel.appendChild(priors);
                priorsPanel.updateUI();
            }
        });
        modelDefinition.fireChangeListeners();

        TitledPanel eqTitledPanel = new TitledPanel(modelDefinition);
        eqTitledPanel.setIcon(equationQFHIcon);

        TitledPanel priorRatingCurveTitledPanel = new TitledPanel(priorRatingCurve);
        priorRatingCurveTitledPanel.setIcon(priorRatingCurveIcon);

        TitledPanel priorSpecificationTitledPanel = new TitledPanel(priorsPanel);
        priorSpecificationTitledPanel.setIcon(priorSpecificationIcon);

        TitledPanelSplitTabContainer mainContainer = TitledPanelSplitTabContainer
                .build2Left1Right(this,
                        eqTitledPanel,
                        priorRatingCurveTitledPanel,
                        priorSpecificationTitledPanel);
        mainContainer.setBreakpoints(1100, 800);
        setContent(mainContainer);

        T.updateHierarchy(this, priors);
        T.updateHierarchy(this, modelDefinition);
        T.updateHierarchy(this, priorRatingCurve);
        T.t(this, () -> {
            eqTitledPanel.setText(T.html("rating_curve_equation"));
            priorSpecificationTitledPanel.setText(T.html("prior_parameter_specification"));
            priorRatingCurveTitledPanel.setText(T.html("prior_rating_curve"));
        });

    }

    @Override
    public Parameter[] getParameters() {
        return priors.getParameters();
    }

    @Override
    public String getModelId() {
        return "TextFile";
    }

    @Override
    public int getNumberOfParameters() {
        return modelDefinition.getNumberOfParameters();
    }

    @Override
    public String[] getInputNames() {
        return modelDefinition.getInputNames();
    }

    @Override
    public String[] getOutputNames() {
        return modelDefinition.getOutputNames();
    }

    @Override
    public String getXtra(String workspace) {
        return modelDefinition.getXtra(workspace);
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        BamConfig config = new BamConfig(0, TYPE);

        // contains both the equation/preset config and priors configs
        config.JSON.put("modelDefinitionAndPriors", modelDefinition.toJSON());

        // rating curve
        BamConfig priorRatingCurveConfig = priorRatingCurve.saveConfig(writeFiles);
        config.JSON.put("priorRatingCurve", priorRatingCurveConfig.JSON);
        if (priorRatingCurveConfig.FILE_PATHS.size() > 0) {
            config.FILE_PATHS.addAll(priorRatingCurveConfig.FILE_PATHS);
        }

        return config;
    }

    @Override
    public void load(BamConfig config) {

        JSONObject modelDefinitionAndPriors = config.JSON.optJSONObject("modelDefinitionAndPriors");
        modelDefinition.fromJSON(modelDefinitionAndPriors);

        if (config.JSON.has("priorRatingCurve")) {
            priorRatingCurve.loadConfig(config.JSON.getJSONObject("priorRatingCurve"));
        } else {
            ConsoleLogger.log("missing 'priorRatingCurve'");
        }

    }

}
