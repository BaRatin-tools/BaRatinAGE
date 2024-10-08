package org.baratinage.ui.baratin.baratin_qfh;

import javax.swing.ImageIcon;

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
import org.baratinage.ui.container.RowColPanel.ALIGN;
import org.baratinage.ui.container.RowColPanel.AXIS;

public class HydraulicConfigurationQFH extends BamItem
        implements IModelDefinition, IPriors {

    public static final ImageIcon equationQFHIcon = AppSetup.ICONS
            .getCustomAppImageIcon("rating_curve_equation.svg");
    public static final ImageIcon priorSpecificationIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_densities.svg");
    public static final ImageIcon priorRatingCurveIcon = AppSetup.ICONS
            .getCustomAppImageIcon("prior_rating_curve.svg");

    private final PriorRatingCurve<HydraulicConfigurationQFH> priorRatingCurve;

    private final RowColPanel priorsPanel;

    private final QFHModelDefinition modelDefinition;
    private QFHPriors priors;

    public HydraulicConfigurationQFH(String uuid, BamProject project) {
        super(BamItemType.HYDRAULIC_CONFIG_QFH, uuid, project);

        modelDefinition = new QFHModelDefinition();

        priorRatingCurve = new PriorRatingCurve<>(this);

        priorsPanel = new RowColPanel(AXIS.COL, ALIGN.START);
        priorsPanel.setPadding(5);

        modelDefinition.addChangeListener(l -> {
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

        // T.updateHierarchy(this, controlMatrix);
        // T.updateHierarchy(this, hydraulicControls);
        T.updateHierarchy(this, priorRatingCurve);
        T.t(this, () -> {
            eqTitledPanel.setText(T.html("rating_curve_equation"));
            priorSpecificationTitledPanel.setText(T.html("prior_parameter_specification"));
            priorRatingCurveTitledPanel.setText(T.html("prior_rating_curve"));
        });

        /**
         * rename RatingCurveTextFileEquation into RatingCurveQFHConfiguration
         * rename RatingCurveTextFileCustomEquation into RatingCurveTextFileEquation
         * and make it a IPrior, IModelDefinition
         * and make it supply the RatingCurveQFHPriors panel (one for custom and one for
         * each preset)
         * make RatingCurveQFHPriors be set either from a HashSet of string or
         * RatingCurveQFHPreset
         * rename all classes RatingCurve* into QFHRC* done
         */
    }

    @Override
    public Parameter[] getParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParameters'");
    }

    @Override
    public String getModelId() {
        return "TextFile";
    }

    @Override
    public int getNumberOfParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberOfParameters'");
    }

    @Override
    public String[] getInputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInputNames'");
    }

    @Override
    public String[] getOutputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputNames'");
    }

    @Override
    public String getXtra(String workspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getXtra'");
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void load(BamConfig config) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'load'");
    }

}
