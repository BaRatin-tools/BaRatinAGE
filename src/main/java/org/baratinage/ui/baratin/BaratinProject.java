package org.baratinage.ui.baratin;

import java.nio.file.Path;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.lg.Lg;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * - _datasets_
 * ----- imported data (**D**)
 * ----- gaugings (**CD**)
 * ----- limnigraphs (**PD**)
 * - structural errors (**SE**)
 * - hydraulic configuration (**MD**)
 * ----- priors (**MP**) and prior rating curve (**PD**, **PPE**) and densities
 * (**\***)
 * - rating curve (**MCMC**, **CM**)
 * ----- posterior rating curve (**PD**) and posterior parameters (**\***) + as
 * many items as relevant result exploration possibilities such as comparing
 * prior and posterior parameters / rating curve, visualizing MCMC traces, etc.
 * - hydrographs (**PE** or **PPE**)
 * 
 */

public class BaratinProject extends BamProject {

    private ExplorerItem hydraulicConfig;
    private ExplorerItem gaugings;
    private ExplorerItem structuralError;
    private ExplorerItem ratingCurve;

    static private ImageIcon buildIcon(String iconName, int iconSize) {
        return SvgIcon
                .buildNoScalingIcon(Path.of(
                        AppConfig.AC.ICONS_RESOURCES_DIR,
                        "custom", iconName).toString(), iconSize);
    }

    // FIXME: best iconSize varies depending on OS scaling
    static private final int iconSize = 30;

    static private final ImageIcon hydraulicConfigIcon = buildIcon("hydraulic_config.svg", iconSize);
    static private final ImageIcon gaugingsIcon = buildIcon("gaugings.svg", iconSize);
    static private final ImageIcon structuralErrorIcon = buildIcon("structural_error.svg", iconSize);
    static private final ImageIcon ratingCurveIcon = buildIcon("rating_curve.svg", iconSize);
    static private final ImageIcon addHydraulicConfigIcon = buildIcon("hydraulic_config_add.svg", iconSize);
    static private final ImageIcon addGaugingsIcon = buildIcon("gaugings_add.svg", iconSize);
    static private final ImageIcon addStructuralErrorIcon = buildIcon("structural_error_add.svg", iconSize);
    static private final ImageIcon addRatingCurveIcon = buildIcon("rating_curve_add.svg", iconSize);

    public BaratinProject() {
        super();

        if (AppConfig.AC.APP_MAIN_FRAME.baratinMenu == null) {
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu = new JMenu();
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu.setText("BaRatin");
            AppConfig.AC.APP_MAIN_FRAME.mainMenuBar.add(AppConfig.AC.APP_MAIN_FRAME.baratinMenu);
        } else {
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu.removeAll();
        }
        JMenu baratinMenu = AppConfig.AC.APP_MAIN_FRAME.baratinMenu;

        JMenuItem menuBtnNewHydraulicConfig = new JMenuItem();
        Lg.register(menuBtnNewHydraulicConfig, "create_hydraulic_config");
        menuBtnNewHydraulicConfig.setIcon(addHydraulicConfigIcon);
        menuBtnNewHydraulicConfig.addActionListener(e -> {
            addHydraulicConfig();
        });
        baratinMenu.add(menuBtnNewHydraulicConfig);

        JButton btnNewHydraulicConfig = new JButton(addHydraulicConfigIcon);
        btnNewHydraulicConfig.addActionListener(e -> {
            addHydraulicConfig();
        });
        toolBar.add(btnNewHydraulicConfig);

        JMenuItem menuBtnNewGaugings = new JMenuItem();
        Lg.register(menuBtnNewGaugings, "create_gaugings");
        menuBtnNewGaugings.setIcon(addGaugingsIcon);
        menuBtnNewGaugings.addActionListener(e -> {
            addGaugings();
        });
        baratinMenu.add(menuBtnNewGaugings);

        JButton btnNewGaugings = new JButton(addGaugingsIcon);
        btnNewGaugings.addActionListener(e -> {
            addGaugings();
        });
        toolBar.add(btnNewGaugings);

        JMenuItem menuBtnNewStructErrorModel = new JMenuItem();
        Lg.register(menuBtnNewStructErrorModel, "create_structural_error_model");
        menuBtnNewStructErrorModel.setIcon(addStructuralErrorIcon);
        menuBtnNewStructErrorModel.addActionListener(e -> {
            addStructuralErrorModel();
        });
        baratinMenu.add(menuBtnNewStructErrorModel);

        JButton btnNewStructErrorModel = new JButton(addStructuralErrorIcon);
        btnNewStructErrorModel.addActionListener(e -> {
            addStructuralErrorModel();
        });
        toolBar.add(btnNewStructErrorModel);

        JMenuItem menuBtnNewRatingCurve = new JMenuItem();
        Lg.register(menuBtnNewRatingCurve, "create_rating_curve");
        menuBtnNewRatingCurve.setIcon(addRatingCurveIcon);
        menuBtnNewRatingCurve.addActionListener(e -> {
            addRatingCurve();
        });
        baratinMenu.add(menuBtnNewRatingCurve);

        JButton btnNewRatingCurve = new JButton(addRatingCurveIcon);
        btnNewRatingCurve.addActionListener(e -> {
            addRatingCurve();
        });
        toolBar.add(btnNewRatingCurve);

        setupExplorer();

    }

    public void addDefaultItems() {
        addStructuralErrorModel();
        addHydraulicConfig();
    }

    private void setupExplorer() {

        hydraulicConfig = new ExplorerItem(
                "hc",
                "Configurations hydrauliques",
                hydraulicConfigIcon);
        this.explorer.appendItem(hydraulicConfig);

        gaugings = new ExplorerItem(
                "g",
                "Jeux de jaugeages",
                gaugingsIcon);
        this.explorer.appendItem(gaugings);

        structuralError = new ExplorerItem(
                "se",
                "Modèles d'erreur structurelle",
                structuralErrorIcon);
        this.explorer.appendItem(structuralError);

        ratingCurve = new ExplorerItem(
                "rc",
                "Courbes de tarage",
                ratingCurveIcon);
        this.explorer.appendItem(ratingCurve);

    }

    @Override
    public void deleteItem(BamItem bamItem, ExplorerItem explorerItem) {
        if (bamItem instanceof RatingCurve) {
            RatingCurve rc = (RatingCurve) bamItem;
            BAM_ITEMS.removeChangeListener(rc);
        }
        super.deleteItem(bamItem, explorerItem);
    }

    public HydraulicConfiguration addHydraulicConfig(HydraulicConfiguration hc) {

        ExplorerItem explorerItem = new ExplorerItem(
                hc.ID,
                hc.bamItemNameField.getText(),
                hydraulicConfigIcon,
                hydraulicConfig);
        addItem(hc, explorerItem);
        Lg.register(hc.bamItemTypeLabel, "hydraulic_config");
        hc.bamItemTypeLabel.setIcon(hydraulicConfigIcon);
        hc.cloneButton.addActionListener((e) -> {
            HydraulicConfiguration newHc = (HydraulicConfiguration) hc.clone();
            newHc.setCopyName();
            addHydraulicConfig(newHc);
        });

        return hc;
    }

    public HydraulicConfiguration addHydraulicConfig(String uuid) {
        HydraulicConfiguration hc = new HydraulicConfiguration(uuid, this);
        hc.bamItemNameField.setText(BAM_ITEMS.getDefaultName(BamItemType.HYDRAULIC_CONFIG));
        return addHydraulicConfig(hc);
    }

    public HydraulicConfiguration addHydraulicConfig() {
        return addHydraulicConfig(UUID.randomUUID().toString());
    }

    public Gaugings addGaugings(Gaugings g) {
        ExplorerItem explorerItem = new ExplorerItem(
                g.ID,
                g.bamItemNameField.getText(),
                gaugingsIcon,
                gaugings);
        addItem(g, explorerItem);
        Lg.register(g.bamItemTypeLabel, "gaugings");
        g.bamItemTypeLabel.setIcon(gaugingsIcon);
        g.cloneButton.addActionListener((e) -> {
            Gaugings newG = (Gaugings) g.clone();
            newG.setCopyName();
            addGaugings(newG);
        });
        return g;
    }

    public Gaugings addGaugings(String uuid) {
        Gaugings g = new Gaugings(uuid, this);
        g.bamItemNameField.setText(BAM_ITEMS.getDefaultName(BamItemType.GAUGINGS));
        return addGaugings(g);
    }

    public Gaugings addGaugings() {
        return addGaugings(UUID.randomUUID().toString());
    }

    public StructuralError addStructuralErrorModel(StructuralError se) {
        ExplorerItem explorerItem = new ExplorerItem(
                se.ID,
                se.bamItemNameField.getText(),
                structuralErrorIcon,
                structuralError);
        addItem(se, explorerItem);
        Lg.register(se.bamItemTypeLabel, "structural_error_model");
        se.bamItemTypeLabel.setIcon(structuralErrorIcon);
        se.cloneButton.addActionListener((e) -> {
            StructuralError newSe = (StructuralError) se.clone();
            newSe.setCopyName();
            addStructuralErrorModel(newSe);
        });
        return se;
    }

    public StructuralError addStructuralErrorModel(String uuid) {
        StructuralError se = new StructuralError(uuid, this);
        se.bamItemNameField.setText(BAM_ITEMS.getDefaultName(BamItemType.STRUCTURAL_ERROR));
        return addStructuralErrorModel(se);
    }

    public StructuralError addStructuralErrorModel() {
        return addStructuralErrorModel(UUID.randomUUID().toString());
    }

    public RatingCurve addRatingCurve(RatingCurve rc) {
        BAM_ITEMS.addChangeListener(rc); // needed because the component has parents
        ExplorerItem explorerItem = new ExplorerItem(
                rc.ID,
                rc.bamItemNameField.getText(),
                ratingCurveIcon,
                ratingCurve);
        addItem(rc, explorerItem);
        Lg.register(rc.bamItemTypeLabel, "rating_curve");
        rc.bamItemTypeLabel.setIcon(ratingCurveIcon);
        rc.cloneButton.addActionListener((e) -> {
            RatingCurve newRc = (RatingCurve) rc.clone();
            newRc.setCopyName();
            addRatingCurve(newRc);
        });
        return rc;
    }

    public RatingCurve addRatingCurve(String uuid) {
        RatingCurve rc = new RatingCurve(uuid, this);
        rc.bamItemNameField.setText(BAM_ITEMS.getDefaultName(BamItemType.RATING_CURVE));
        return addRatingCurve(rc);
    }

    public RatingCurve addRatingCurve() {
        return addRatingCurve(UUID.randomUUID().toString());
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("model", "baratin");
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        JSONArray items = json.getJSONArray("items");
        // FIXME: order matter! Children should come last!

        // Dealing with root items (items with no parent)

        for (Object item : items) {
            // System.out.println("---");
            JSONObject jsonObj = (JSONObject) item;
            BamItemType itemType = BamItemType.valueOf(jsonObj.getString("type"));
            String uuid = jsonObj.getString("uuid");

            BamItem bamItem;
            if (itemType == BamItemType.HYDRAULIC_CONFIG) {
                bamItem = addHydraulicConfig(uuid);
            } else if (itemType == BamItemType.GAUGINGS) {
                bamItem = addGaugings(uuid);
            } else if (itemType == BamItemType.STRUCTURAL_ERROR) {
                bamItem = addStructuralErrorModel(uuid);
            } else if (itemType == BamItemType.RATING_CURVE) {
                continue;
            } else {
                System.out.println("unknown bam item, skipping => " + itemType);
                continue;
            }
            bamItem.fromFullJSON(jsonObj);
        }

        // Dealing with children (rating curve);
        for (Object item : items) {
            JSONObject jsonObj = (JSONObject) item;
            BamItemType itemType = BamItemType.valueOf(jsonObj.getString("type"));
            String uuid = jsonObj.getString("uuid");

            BamItem bamItem;
            if (itemType == BamItemType.RATING_CURVE) {
                bamItem = addRatingCurve(uuid);
            } else {
                continue;
            }
            bamItem.fromFullJSON(jsonObj);
        }
    }

    private String projectPath = null;

    @Override
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public String getProjectPath() {
        return projectPath;
    }
}
