package org.baratinage.ui.baratin;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ImageIcon;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamConfigRecord;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.RunBam;
import org.baratinage.ui.baratin.hydraulic_configuration.PriorRatingCurve;
import org.baratinage.ui.baratin.hydraulic_control.ControlMatrix;
import org.baratinage.ui.baratin.hydraulic_control.HydraulicControlPanels;
import org.baratinage.ui.component.Title;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.perf.TimedActions;
import org.json.JSONObject;

public class HydraulicConfiguration
        extends BamItem
        implements IModelDefinition, IPriors {

    private final Title controlMatrixTitle;
    private final Title priorRCplotTitle;
    private final Title priorSpecificationTitle;

    private final ControlMatrix controlMatrix;
    private final HydraulicControlPanels hydraulicControls;

    private final PriorRatingCurve<HydraulicConfiguration> priorRatingCurve;

    private final TabContainer mainContainerTab;
    private boolean isTabView = false;

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
            // updateHydraulicControls(controlMatrix.getControlMatrix());
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

        Dimension dimPref = new Dimension(500, 300);
        controlMatrix.setPreferredSize(dimPref);
        Dimension dimMin = new Dimension(250, 150);
        controlMatrix.setMinimumSize(dimMin);

        // **********************************************************************
        // SPECIFIC TO SPLIT PANE / TAB SYSTEM APPROACHES

        controlMatrixTitle = new Title(controlMatrixIcon, "");
        priorRCplotTitle = new Title(priorRatingCurveIcon, "");
        priorSpecificationTitle = new Title(priorSpecificationIcon, "");

        mainContainerTab = new TabContainer();

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                TimedActions.throttle(
                        "hydraulic_config_resize_action",
                        250,
                        HydraulicConfiguration.this::setPanelView);
            }
        });

        setSplitPaneView();

        // **********************************************************************

        boolean[][] mat = controlMatrix.getControlMatrix();
        hydraulicControls.setHydraulicControls(mat.length);
        // updateHydraulicControls(mat);

        T.updateHierarchy(this, controlMatrix);
        T.updateHierarchy(this, hydraulicControls);
        T.updateHierarchy(this, priorRatingCurve);

        T.t(this, () -> {
            controlMatrixTitle.setText(T.html("control_matrix"));
            priorRCplotTitle.setText(T.html("prior_rating_curve"));
            priorSpecificationTitle.setText(T.html("prior_parameter_specification"));
            if (mainContainerTab.getTabCount() > 2) {
                mainContainerTab.setTitleAt(0, T.html("control_matrix"));
                mainContainerTab.setTitleAt(1, T.html("prior_parameter_specification"));
                mainContainerTab.setTitleAt(2, T.html("prior_rating_curve"));
            }
        });

    }

    private void setPanelView() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        ConsoleLogger.log("panel size is : " + panelWidth + " x " + panelHeight);
        if (panelWidth == 0 || panelHeight == 0) {
            return;
        }
        if (panelWidth < 1100 || panelHeight < 800) {
            if (!isTabView) {
                setTabView();
            }
            isTabView = true;
        } else {
            if (isTabView) {
                setSplitPaneView();
            }
            isTabView = false;
        }
    }

    private void setSplitPaneView() {

        RowColPanel controlMatrixContainer = new RowColPanel(RowColPanel.AXIS.COL);

        controlMatrixContainer.appendChild(controlMatrixTitle, 0);
        controlMatrixContainer.appendChild(controlMatrix, 1);

        RowColPanel priorRCplotPanel = new RowColPanel(RowColPanel.AXIS.COL);

        priorRCplotTitle.setIcon(priorRatingCurveIcon);
        priorRCplotPanel.appendChild(priorRCplotTitle, 0);
        priorRCplotPanel.appendChild(priorRatingCurve, 1);

        RowColPanel priorSepecificationPanel = new RowColPanel(RowColPanel.AXIS.COL);

        priorSpecificationTitle.setIcon(priorSpecificationIcon);
        priorSepecificationPanel.appendChild(priorSpecificationTitle, 0);
        priorSepecificationPanel.appendChild(hydraulicControls, 1);

        SplitContainer mainContainer = SplitContainer.build2Left1RightSplitContainer(
                controlMatrixContainer,
                priorRCplotPanel,
                priorSepecificationPanel);
        setContent(mainContainer);
        T.updateTranslation(this);
        updateUI();
    }

    private void setTabView() {

        mainContainerTab.removeAll();
        mainContainerTab.addTab("control_matrix", controlMatrixIcon, controlMatrix);
        mainContainerTab.addTab("prior_parameter_specification",
                priorSpecificationIcon,
                hydraulicControls);
        mainContainerTab.addTab("prior_rating_curve", priorRatingCurveIcon,
                priorRatingCurve);

        setContent(mainContainerTab);
        T.updateTranslation(this);
        updateUI();
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
    public BamConfigRecord save(boolean writeFiles) {
        JSONObject json = new JSONObject();

        // **********************************************************
        // Control matrix
        json.put("controlMatrix", controlMatrix.toJSON());

        // **********************************************************
        // Hydraulic controls

        json.put("hydraulicControls", hydraulicControls.toJSON());

        // **********************************************************
        // prior rating curve configuration
        BamConfig priorRatingCurveConfig = priorRatingCurve.saveConfig();
        json.put("priorRatingCurve", priorRatingCurveConfig.JSON);
        if (priorRatingCurveConfig.FILE_PATHS.size() > 0) {
            String bamRunZipPath = priorRatingCurveConfig.FILE_PATHS.get(0);
            return new BamConfigRecord(json, bamRunZipPath);
        }
        return new BamConfigRecord(json);
    }

    @Override
    public void load(BamConfigRecord bamItemBackup) {

        JSONObject json = bamItemBackup.jsonObject();

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
            priorRatingCurve.runBam.runAsync(() -> {
                ConsoleLogger.log("Re-running prior prediction done");
            }, () -> {
                ConsoleLogger.log("Re-running prior prediction failed");
            });
        }

    }

}
