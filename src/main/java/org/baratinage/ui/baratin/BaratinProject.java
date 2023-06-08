package org.baratinage.ui.baratin;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.baratinage.App;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.NoScalingIcon;

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

        addHydraulicConfig(); // FIXME: feels like default should be empty to be able to set a default
                              // elsewhere and import content from a file

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

    private void addHydraulicConfig() {
        HydraulicConfiguration hydroConf = new HydraulicConfiguration();
        ExplorerItem explorerItem = new ExplorerItem(
                hydroConf.getUUID(),
                hydroConf.getName(),
                hydraulicConfigIconPath,
                hydraulicConfig);
        addItem(hydroConf, explorerItem);

    }

    private void addGaugings() {
        Gaugings gaugingsItem = new Gaugings();
        this.getBamItems().addChangeListener(gaugingsItem);
        ExplorerItem explorerItem = new ExplorerItem(
                gaugingsItem.getUUID(),
                gaugingsItem.getName(),
                gaugingsIconPath,
                gaugings);
        addItem(gaugingsItem, explorerItem);
    }

    private void addStructuralErrorModel() {
        StructuralError structuralErrorItem = new StructuralError();
        this.getBamItems().addChangeListener(structuralErrorItem);
        ExplorerItem explorerItem = new ExplorerItem(
                structuralErrorItem.getUUID(),
                structuralErrorItem.getName(),
                structuralErrIconPath,
                structuralError);
        addItem(structuralErrorItem, explorerItem);
    }

    private void addRatingCurve() {
        RatingCurve ratingCurveItem = new RatingCurve();
        this.getBamItems().addChangeListener(ratingCurveItem);
        ExplorerItem explorerItem = new ExplorerItem(
                ratingCurveItem.getUUID(),
                ratingCurveItem.getName(),
                ratingCurveIconPath,
                ratingCurve);
        addItem(ratingCurveItem, explorerItem);
    }

}
