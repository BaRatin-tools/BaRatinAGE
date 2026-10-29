package org.baratinage.ui.baratin;

import javax.swing.Icon;
import javax.swing.JButton;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.baratin.baratin_qfh.QFHModelDefinition;
import org.baratinage.ui.baratin.baratin_qfh.QFHPriors;
import org.baratinage.ui.baratin.hydraulic_configuration.PriorRatingCurve;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.container.TitledPanel;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONObject;

public class HydraulicConfigurationQFH extends BamItem
        implements IModelDefinition, IPriors {

    public static final Icon equationQFHIcon = AppSetup.ICONS
            .getCustomAppImageIcon("rating_curve_equation.svg");
    public static final Icon priorSpecificationIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_densities.svg");
    public static final Icon priorRatingCurveIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_rating_curve.svg");

    public final PriorRatingCurve<HydraulicConfigurationQFH> priorRatingCurve;

    private final SimpleFlowPanel priorsPanel;

    private final QFHModelDefinition modelDefinition;
    private QFHPriors priors;

    private boolean priorRatingCurveMode = false;

    public HydraulicConfigurationQFH(String uuid, BamProject project) {
        super(BamItemType.HYDRAULIC_CONFIG_QFH, uuid, project);

        modelDefinition = new QFHModelDefinition();
        priorRatingCurve = new PriorRatingCurve<>(this);

        priorsPanel = new SimpleFlowPanel(true);

        modelDefinition.addChangeListener(l -> {
            fireChangeListeners();
            if (!modelDefinition.isModelDefinitionValid()) {
                priors = null;
                priorsPanel.removeAll();
                priorRatingCurve.checkSync();
                return;
            }
            if (!modelDefinition.priorsPanel.equals(priors)) {
                priors = modelDefinition.priorsPanel;
                priorsPanel.removeAll();
                priorsPanel.addChild(priors, true);
            }
            priorRatingCurve.checkSync();
        });
        modelDefinition.fireChangeListeners();

        TitledPanel eqTitledPanel = new TitledPanel(modelDefinition);
        eqTitledPanel.setIcon(equationQFHIcon);

        TitledPanel priorRatingCurveTitledPanel = new TitledPanel(priorRatingCurve);
        priorRatingCurveTitledPanel.setIcon(priorRatingCurveIcon);

        TitledPanel priorSpecificationTitledPanel = new TitledPanel(priorsPanel);
        priorSpecificationTitledPanel.setIcon(priorSpecificationIcon);

        SplitContainer modelAndPriorsPanel = new SplitContainer(
                eqTitledPanel.getTitledPanel(),
                priorSpecificationTitledPanel.getTitledPanel(),
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

        T.updateHierarchy(this, priors);
        T.updateHierarchy(this, modelDefinition);
        T.updateHierarchy(this, priorRatingCurve);
        T.t(this, () -> {
            eqTitledPanel.setText(T.html("rating_curve_equation"));
            priorSpecificationTitledPanel.setText(T.html("prior_parameter_specification"));
            priorRatingCurveTitledPanel.setText(T.html("prior_rating_curve"));
            updateModeToggleButton(modeToggleButton);
        });

    }

    private void updateModeToggleButton(JButton button) {
        if (priorRatingCurveMode) {
            button.setText(T.html("hydraulic_config_qfh"));
            button.setIcon(BamItemType.HYDRAULIC_CONFIG_QFH.getIcon());
        } else {
            button.setText(T.html("prior_rating_curve"));
            button.setIcon(priorRatingCurveIcon);
        }
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
