package org.baratinage.ui.baratin;

import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.baratinage.jbam.utils.Write;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.component.ExplorerItem;
// import org.baratinage.ui.component.ImportedData;
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

        JButton btnNewHydraulicConfig = new JButton();
        btnNewHydraulicConfig.setText("Nouvelle configuration hydraulique");
        btnNewHydraulicConfig.setIcon(new NoScalingIcon(hydraulicConfigIconPath));
        btnNewHydraulicConfig.addActionListener(e -> {
            addHydraulicConfig();
        });
        this.actionBar.appendChild(btnNewHydraulicConfig);

        JButton btnNewRatingCurve = new JButton();
        btnNewRatingCurve.setText("Nouvelle courbe de tarage");
        btnNewRatingCurve.setIcon(new NoScalingIcon(ratingCurveIconPath));
        btnNewRatingCurve.addActionListener(e -> {
            addRatingCurve();
        });
        this.actionBar.appendChild(btnNewRatingCurve);

        JButton btnSaveProject = new JButton();
        btnSaveProject.setText("Sauvegarder le projet");
        btnSaveProject.setIcon(new NoScalingIcon("./resources/icons/save_32x32.png"));
        btnSaveProject.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    if (f.getName().endsWith(".bam")) {
                        return true;
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return "Fichier BaRatinAGE (.bam)";
                }

            });
            fileChooser.setDialogTitle("Sauvegarder le projet");
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                JSONObject json = new JSONObject();
                JSONArray jsonItems = new JSONArray();
                for (BamItem item : items) {
                    jsonItems.put(item.toFullJSON());
                }
                json.put("items", jsonItems);
                String fullFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    Write.writeLines(new File(fullFilePath), new String[] { json.toString(4) });
                } catch (IOException saveError) {
                    System.err.println("Failed to save file");
                    saveError.printStackTrace();
                }
            }

        });
        this.actionBar.appendChild(btnSaveProject);

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
