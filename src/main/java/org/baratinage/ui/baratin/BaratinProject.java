package org.baratinage.ui.baratin;

import java.util.UUID;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.baratinage.App;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.NoScalingIcon;
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

    static private final String hydraulicConfigIconPath = "./resources/icons/Hydraulic_icon.png";
    static private final String gaugingsIconPath = "./resources/icons/Gauging_icon.png";
    static private final String structuralErrIconPath = "./resources/icons/Error_icon.png";
    static private final String ratingCurveIconPath = "./resources/icons/RC_icon.png";

    public BaratinProject() {
        super();

        if (App.MAIN_FRAME.baratinMenu == null) {
            App.MAIN_FRAME.baratinMenu = new JMenu();
            App.MAIN_FRAME.baratinMenu.setText("BaRatin");
            App.MAIN_FRAME.mainMenuBar.add(App.MAIN_FRAME.baratinMenu);
        } else {
            App.MAIN_FRAME.baratinMenu.removeAll();
        }
        JMenu baratinMenu = App.MAIN_FRAME.baratinMenu;

        JMenuItem btnNewHydraulicConfig = new JMenuItem();
        btnNewHydraulicConfig.setText("Créer une nouvelle configuration hydraulique");
        btnNewHydraulicConfig.setIcon(new NoScalingIcon(hydraulicConfigIconPath));
        btnNewHydraulicConfig.addActionListener(e -> {
            addHydraulicConfig();
        });
        baratinMenu.add(btnNewHydraulicConfig);

        JMenuItem btnNewGaugings = new JMenuItem();
        btnNewGaugings.setText("Créer un nouveau jeu de jaugeages");
        btnNewGaugings.setIcon(new NoScalingIcon(gaugingsIconPath));
        btnNewGaugings.addActionListener(e -> {
            addGaugings();
        });
        baratinMenu.add(btnNewGaugings);

        JMenuItem btnNewStructErrorModel = new JMenuItem();
        btnNewStructErrorModel.setText("Créer un nouveau modèle d'erreur structurelle");
        btnNewStructErrorModel.setIcon(new NoScalingIcon(structuralErrIconPath));
        btnNewStructErrorModel.addActionListener(e -> {
            addStructuralErrorModel();
        });
        baratinMenu.add(btnNewStructErrorModel);

        JMenuItem btnNewRatingCurve = new JMenuItem();
        btnNewRatingCurve.setText("Créer une nouvelle courbe de tarage");
        btnNewRatingCurve.setIcon(new NoScalingIcon(ratingCurveIconPath));
        btnNewRatingCurve.addActionListener(e -> {
            addRatingCurve();
        });
        baratinMenu.add(btnNewRatingCurve);

        setupExplorer();

    }

    public void addDefaultItems() {
        addStructuralErrorModel();
        addHydraulicConfig();
    }

    // FIXME: this method is typically something that should be set in a parent
    // class that represents BaM project (as an abstract method...)
    private void setupExplorer() {

        hydraulicConfig = new ExplorerItem(
                "hc",
                "Configurations hydrauliques",
                hydraulicConfigIconPath);
        this.explorer.appendItem(hydraulicConfig);

        gaugings = new ExplorerItem(
                "g",
                "Jeux de jaugeages",
                gaugingsIconPath);
        this.explorer.appendItem(gaugings);

        structuralError = new ExplorerItem(
                "se",
                "Modèles d'erreur structurelle",
                structuralErrIconPath);
        this.explorer.appendItem(structuralError);

        ratingCurve = new ExplorerItem(
                "rc",
                "Courbes de tarage",
                ratingCurveIconPath);
        this.explorer.appendItem(ratingCurve);

    }

    @Override
    public void deleteItem(BamItem bamItem, ExplorerItem explorerItem) {
        if (bamItem instanceof RatingCurve) {
            RatingCurve rc = (RatingCurve) bamItem;
            this.getBamItems().removeChangeListener(rc);
        }
        super.deleteItem(bamItem, explorerItem);
    }

    public HydraulicConfiguration addHydraulicConfig(HydraulicConfiguration hc) {
        hc.cloneButton.addActionListener((e) -> {
            HydraulicConfiguration newHc = (HydraulicConfiguration) hc.clone();
            newHc.addTimeStampToName();
            addHydraulicConfig(newHc);
        });
        ExplorerItem explorerItem = new ExplorerItem(
                hc.ID,
                hc.getName(),
                hydraulicConfigIconPath,
                hydraulicConfig);
        addItem(hc, explorerItem);
        return hc;
    }

    public HydraulicConfiguration addHydraulicConfig(String uuid) {
        HydraulicConfiguration hc = new HydraulicConfiguration(uuid, this);
        return addHydraulicConfig(hc);
    }

    public HydraulicConfiguration addHydraulicConfig() {
        return addHydraulicConfig(UUID.randomUUID().toString());
    }

    public Gaugings addGaugings(Gaugings g) {
        g.cloneButton.addActionListener((e) -> {
            Gaugings newG = (Gaugings) g.clone();
            newG.addTimeStampToName();
            addGaugings(newG);
        });
        ExplorerItem explorerItem = new ExplorerItem(
                g.ID,
                g.getName(),
                gaugingsIconPath,
                gaugings);
        addItem(g, explorerItem);
        return g;
    }

    public Gaugings addGaugings(String uuid) {
        Gaugings g = new Gaugings(uuid, this);
        return addGaugings(g);
    }

    public Gaugings addGaugings() {
        return addGaugings(UUID.randomUUID().toString());
    }

    public StructuralError addStructuralErrorModel(StructuralError se) {
        se.cloneButton.addActionListener((e) -> {
            StructuralError newSe = (StructuralError) se.clone();
            newSe.addTimeStampToName();
            addStructuralErrorModel(newSe);
        });
        ExplorerItem explorerItem = new ExplorerItem(
                se.ID,
                se.getName(),
                structuralErrIconPath,
                structuralError);
        addItem(se, explorerItem);
        return se;
    }

    public StructuralError addStructuralErrorModel(String uuid) {
        StructuralError se = new StructuralError(uuid, this);
        return addStructuralErrorModel(se);
    }

    public StructuralError addStructuralErrorModel() {
        return addStructuralErrorModel(UUID.randomUUID().toString());
    }

    public RatingCurve addRatingCurve(RatingCurve rc) {
        getBamItems().addChangeListener(rc);
        rc.cloneButton.addActionListener((e) -> {
            RatingCurve newRc = (RatingCurve) rc.clone();
            newRc.addTimeStampToName();
            addRatingCurve(newRc);
        });
        ExplorerItem explorerItem = new ExplorerItem(
                rc.ID,
                rc.getName(),
                ratingCurveIconPath,
                ratingCurve);
        addItem(rc, explorerItem);
        return rc;
    }

    public RatingCurve addRatingCurve(String uuid) {
        RatingCurve rc = new RatingCurve(uuid, this);
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
}
