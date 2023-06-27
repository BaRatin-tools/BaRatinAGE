package org.baratinage.ui.baratin;

import java.util.UUID;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.baratinage.App;
import org.baratinage.ui.bam.BamItem;
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

    public HydraulicConfiguration addHydraulicConfig(HydraulicConfiguration hydroConf) {
        ExplorerItem explorerItem = new ExplorerItem(
                hydroConf.ID,
                hydroConf.getName(),
                hydraulicConfigIconPath,
                hydraulicConfig);
        addItem(hydroConf, explorerItem);
        return hydroConf;
    }

    public HydraulicConfiguration addHydraulicConfig() {
        return addHydraulicConfig(UUID.randomUUID().toString());
    }

    public HydraulicConfiguration addHydraulicConfig(String uuid) {
        HydraulicConfiguration hydroConf = new HydraulicConfiguration(uuid);
        ExplorerItem explorerItem = new ExplorerItem(
                uuid,
                hydroConf.getName(),
                hydraulicConfigIconPath,
                hydraulicConfig);
        addItem(hydroConf, explorerItem);
        return hydroConf;
    }

    public Gaugings addGaugings(Gaugings gaugingsItem) {
        ExplorerItem explorerItem = new ExplorerItem(
                gaugingsItem.ID,
                gaugingsItem.getName(),
                gaugingsIconPath,
                gaugings);
        addItem(gaugingsItem, explorerItem);
        return gaugingsItem;
    }

    public Gaugings addGaugings() {
        return addGaugings(UUID.randomUUID().toString());
    }

    public Gaugings addGaugings(String uuid) {
        Gaugings gaugingsItem = new Gaugings(uuid);
        this.getBamItems().addChangeListener(gaugingsItem);
        ExplorerItem explorerItem = new ExplorerItem(
                uuid,
                gaugingsItem.getName(),
                gaugingsIconPath,
                gaugings);
        addItem(gaugingsItem, explorerItem);
        return gaugingsItem;
    }

    private StructuralError addStructuralErrorModel() {
        return addStructuralErrorModel(UUID.randomUUID().toString());
    }

    private StructuralError addStructuralErrorModel(String uuid) {
        StructuralError structuralErrorItem = new StructuralError(uuid);
        this.getBamItems().addChangeListener(structuralErrorItem);
        ExplorerItem explorerItem = new ExplorerItem(
                uuid,
                structuralErrorItem.getName(),
                structuralErrIconPath,
                structuralError);
        addItem(structuralErrorItem, explorerItem);
        return structuralErrorItem;
    }

    private RatingCurve addRatingCurve() {
        return addRatingCurve(UUID.randomUUID().toString());
    }

    private RatingCurve addRatingCurve(String uuid) {
        RatingCurve ratingCurveItem = new RatingCurve(uuid);
        this.getBamItems().addChangeListener(ratingCurveItem);
        ExplorerItem explorerItem = new ExplorerItem(
                uuid,
                ratingCurveItem.getName(),
                ratingCurveIconPath,
                ratingCurve);
        addItem(ratingCurveItem, explorerItem);
        return ratingCurveItem;
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
            BamItem.ITEM_TYPE itemType = BamItem.ITEM_TYPE.valueOf(jsonObj.getString("type"));
            String uuid = jsonObj.getString("uuid");

            BamItem bamItem;
            if (itemType == BamItem.ITEM_TYPE.HYRAULIC_CONFIG) {
                bamItem = addHydraulicConfig(uuid);
            } else if (itemType == BamItem.ITEM_TYPE.GAUGINGS) {
                bamItem = addGaugings(uuid);
            } else if (itemType == BamItem.ITEM_TYPE.STRUCTURAL_ERROR) {
                bamItem = addStructuralErrorModel(uuid);
            } else if (itemType == BamItem.ITEM_TYPE.RATING_CURVE) {
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
            BamItem.ITEM_TYPE itemType = BamItem.ITEM_TYPE.valueOf(jsonObj.getString("type"));
            String uuid = jsonObj.getString("uuid");

            BamItem bamItem;
            if (itemType == BamItem.ITEM_TYPE.RATING_CURVE) {
                bamItem = addRatingCurve(uuid);
            } else {
                continue;
            }
            bamItem.fromFullJSON(jsonObj);
        }
    }
}
