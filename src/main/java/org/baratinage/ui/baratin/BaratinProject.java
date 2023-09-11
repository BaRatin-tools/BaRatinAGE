package org.baratinage.ui.baratin;

import java.awt.event.ActionListener;
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
    private ExplorerItem limnigraph;
    private ExplorerItem hydrograph;

    private JMenu baratinMenu;

    public BaratinProject() {
        super();

        if (AppConfig.AC.APP_MAIN_FRAME.baratinMenu == null) {
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu = new JMenu();
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu.setText("BaRatin");
            AppConfig.AC.APP_MAIN_FRAME.mainMenuBar.add(AppConfig.AC.APP_MAIN_FRAME.baratinMenu);
        } else {
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu.removeAll();
        }
        baratinMenu = AppConfig.AC.APP_MAIN_FRAME.baratinMenu;

        addAddButtons("create_hydraulic_config", BamItemType.HYDRAULIC_CONFIG.getAddIcon(), (e) -> {
            addHydraulicConfig();
        });
        addAddButtons("create_gaugings", BamItemType.GAUGINGS.getAddIcon(), (e) -> {
            addGaugings();
        });
        addAddButtons("create_structural_error_model", BamItemType.STRUCTURAL_ERROR.getAddIcon(), (e) -> {
            addStructuralErrorModel();
        });
        addAddButtons("create_rating_curve", BamItemType.RATING_CURVE.getAddIcon(), (e) -> {
            addRatingCurve();
        });
        addAddButtons("create_limnigraph", BamItemType.LIMNIGRAPH.getAddIcon(), (e) -> {
            addLimnigraph();
        });
        addAddButtons("create_hydrograph", BamItemType.HYDROGRAPH.getAddIcon(), (e) -> {
            addHydrograph();
        });

        setupExplorer();

    }

    private void addAddButtons(String lgCreateItemKey, ImageIcon addIcon, ActionListener onAdd) {
        JMenuItem menuButton = new JMenuItem();
        Lg.register(menuButton, lgCreateItemKey);
        menuButton.setIcon(addIcon);
        menuButton.addActionListener(onAdd);
        baratinMenu.add(menuButton);
        JButton toolbarButton = new JButton(addIcon);
        toolbarButton.addActionListener(onAdd);
        toolBar.add(toolbarButton);
    }

    public void addDefaultItems() {
        addStructuralErrorModel();
        addHydraulicConfig();
    }

    private void setupExplorer() {

        hydraulicConfig = new ExplorerItem(
                "hc",
                "Configurations hydrauliques",
                BamItemType.HYDRAULIC_CONFIG.getIcon());
        this.explorer.appendItem(hydraulicConfig);

        gaugings = new ExplorerItem(
                "g",
                "Jeux de jaugeages",
                BamItemType.GAUGINGS.getIcon());
        this.explorer.appendItem(gaugings);

        structuralError = new ExplorerItem(
                "se",
                "Modèles d'erreur structurelle",
                BamItemType.STRUCTURAL_ERROR.getIcon());
        this.explorer.appendItem(structuralError);

        ratingCurve = new ExplorerItem(
                "rc",
                "Courbes de tarage",
                BamItemType.RATING_CURVE.getIcon());
        this.explorer.appendItem(ratingCurve);

        limnigraph = new ExplorerItem("lts",
                "Limnigramme",
                BamItemType.LIMNIGRAPH.getIcon());
        this.explorer.appendItem(limnigraph);

        hydrograph = new ExplorerItem("hts",
                "Hydrogramme",
                BamItemType.HYDROGRAPH.getIcon());
        this.explorer.appendItem(hydrograph);

    }

    @Override
    public void deleteItem(BamItem bamItem, ExplorerItem explorerItem) {
        if (bamItem instanceof RatingCurve) {
            RatingCurve rc = (RatingCurve) bamItem;
            BAM_ITEMS.removeChangeListener(rc);
        }
        super.deleteItem(bamItem, explorerItem);
    }

    // --------------------------------------------------------------
    // Hydraulic Configuration --------------------------------------

    private HydraulicConfiguration addHydraulicConfig(HydraulicConfiguration hc) {

        ExplorerItem explorerItem = new ExplorerItem(
                hc.ID,
                hc.bamItemNameField.getText(),
                BamItemType.HYDRAULIC_CONFIG.getIcon(),
                hydraulicConfig);
        addItem(hc, explorerItem);
        Lg.register(hc.bamItemTypeLabel, "hydraulic_config");
        hc.bamItemTypeLabel.setIcon(BamItemType.HYDRAULIC_CONFIG.getIcon());
        hc.cloneButton.addActionListener((e) -> {
            HydraulicConfiguration newHc = (HydraulicConfiguration) hc.clone();
            newHc.setCopyName();
            addHydraulicConfig(newHc);
        });

        return hc;
    }

    private HydraulicConfiguration addHydraulicConfig(String uuid) {
        HydraulicConfiguration hc = new HydraulicConfiguration(uuid, this);
        hc.bamItemNameField.setText(BAM_ITEMS.getDefaultName(BamItemType.HYDRAULIC_CONFIG));
        return addHydraulicConfig(hc);
    }

    public HydraulicConfiguration addHydraulicConfig() {
        return addHydraulicConfig(UUID.randomUUID().toString());
    }

    // --------------------------------------------------------------
    // Gaugings -----------------------------------------------------

    public Gaugings addGaugings(Gaugings g) {
        ExplorerItem explorerItem = new ExplorerItem(
                g.ID,
                g.bamItemNameField.getText(),
                BamItemType.GAUGINGS.getIcon(),
                gaugings);
        addItem(g, explorerItem);
        Lg.register(g.bamItemTypeLabel, "gaugings");
        g.bamItemTypeLabel.setIcon(BamItemType.GAUGINGS.getIcon());
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

    // --------------------------------------------------------------
    // Structural Error ---------------------------------------------

    public StructuralError addStructuralErrorModel(StructuralError se) {
        ExplorerItem explorerItem = new ExplorerItem(
                se.ID,
                se.bamItemNameField.getText(),
                BamItemType.STRUCTURAL_ERROR.getIcon(),
                structuralError);
        addItem(se, explorerItem);
        Lg.register(se.bamItemTypeLabel, "structural_error_model");
        se.bamItemTypeLabel.setIcon(BamItemType.STRUCTURAL_ERROR.getIcon());
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

    // --------------------------------------------------------------
    // Rating Curve -------------------------------------------------

    public RatingCurve addRatingCurve(RatingCurve rc) {
        BAM_ITEMS.addChangeListener(rc); // needed because the component has parents
        ExplorerItem explorerItem = new ExplorerItem(
                rc.ID,
                rc.bamItemNameField.getText(),
                BamItemType.RATING_CURVE.getIcon(),
                ratingCurve);
        addItem(rc, explorerItem);
        Lg.register(rc.bamItemTypeLabel, "rating_curve");
        rc.bamItemTypeLabel.setIcon(BamItemType.RATING_CURVE.getIcon());
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

    // --------------------------------------------------------------
    // Limnigraph ---------------------------------------------------

    public Limnigraph addLimnigraph(Limnigraph l) {
        ExplorerItem explorerItem = new ExplorerItem(
                l.ID,
                l.bamItemNameField.getText(),
                BamItemType.LIMNIGRAPH.getIcon(),
                limnigraph);
        addItem(l, explorerItem);
        Lg.register(l.bamItemTypeLabel, "limnigraph");
        l.bamItemTypeLabel.setIcon(BamItemType.LIMNIGRAPH.getIcon());
        l.cloneButton.addActionListener((e) -> {
            Limnigraph newL = (Limnigraph) l.clone();
            newL.setCopyName();
            addLimnigraph(newL);
        });
        return l;
    }

    public Limnigraph addLimnigraph(String uuid) {
        Limnigraph l = new Limnigraph(uuid, this);
        l.bamItemNameField.setText(BAM_ITEMS.getDefaultName(BamItemType.LIMNIGRAPH));
        return addLimnigraph(l);
    }

    public Limnigraph addLimnigraph() {
        return addLimnigraph(UUID.randomUUID().toString());
    }

    // --------------------------------------------------------------
    // Hydrograph ---------------------------------------------------

    public Hydrograph addHydrograph(Hydrograph h) {
        ExplorerItem explorerItem = new ExplorerItem(
                h.ID,
                h.bamItemNameField.getText(),
                BamItemType.HYDROGRAPH.getIcon(),
                hydrograph);
        addItem(h, explorerItem);
        Lg.register(h.bamItemTypeLabel, "hydrograph");
        h.bamItemTypeLabel.setIcon(BamItemType.HYDROGRAPH.getIcon());
        h.cloneButton.addActionListener((e) -> {
            Hydrograph newH = (Hydrograph) h.clone();
            newH.setCopyName();
            addHydrograph(newH);
        });
        return h;
    }

    public Hydrograph addHydrograph(String uuid) {
        Hydrograph h = new Hydrograph(uuid, this);
        h.bamItemNameField.setText(BAM_ITEMS.getDefaultName(BamItemType.HYDROGRAPH));
        return addHydrograph(h);
    }

    public Hydrograph addHydrograph() {
        return addHydrograph(UUID.randomUUID().toString());
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
            } else if (itemType == BamItemType.LIMNIGRAPH) {
                bamItem = addLimnigraph(uuid);
            } else if (itemType == BamItemType.RATING_CURVE) {
                continue;
            } else {
                System.out.println("unknown bam item, skipping => " + itemType);
                continue;
            }
            bamItem.fromFullJSON(jsonObj);
        }

        // Dealing with children (rating curve, hydrographs);
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
