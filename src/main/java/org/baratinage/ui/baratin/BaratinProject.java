package org.baratinage.ui.baratin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.baratinage.App;
import org.baratinage.jbam.utils.Write;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.ExplorerItem;
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
        btnNewHydraulicConfig.setText("+");
        btnNewHydraulicConfig.setIcon(new NoScalingIcon(hydraulicConfigIconPath));
        btnNewHydraulicConfig.addActionListener(e -> {
            addHydraulicConfig();
        });
        this.actionBar.appendChild(btnNewHydraulicConfig);

        JButton btnNewGaugings = new JButton();
        btnNewGaugings.setText("+");
        btnNewGaugings.setIcon(new NoScalingIcon(gaugingsIconPath));
        btnNewGaugings.addActionListener(e -> {
            addGaugings();
        });
        this.actionBar.appendChild(btnNewGaugings);

        JButton btnNewStructErrorModel = new JButton();
        btnNewStructErrorModel.setText("+");
        btnNewStructErrorModel.setIcon(new NoScalingIcon(structuralErrIconPath));
        btnNewStructErrorModel.addActionListener(e -> {
            addStructuralErrorModel();
        });
        this.actionBar.appendChild(btnNewStructErrorModel);

        JButton btnNewRatingCurve = new JButton();
        btnNewRatingCurve.setText("+");
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

                String mainConfigFilePath = Path.of(App.TEMP_DIR, "main_config.json").toString();
                File mainConfigFile = new File(mainConfigFilePath);
                try {
                    Write.writeLines(mainConfigFile, new String[] { json.toString(4) });
                } catch (IOException saveError) {
                    System.err.println("Failed to save file");
                    saveError.printStackTrace();
                }

                try {
                    String fullFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                    fullFilePath = fullFilePath.endsWith(".bam") ? fullFilePath : fullFilePath + ".bam";
                    File zipFile = new File(fullFilePath);
                    FileOutputStream zipFileOutStream = new FileOutputStream(zipFile);

                    ZipOutputStream zipOutStream = new ZipOutputStream(zipFileOutStream);

                    System.out.println("File '" + mainConfigFile + "'.");
                    ZipEntry zipEntry = new ZipEntry(mainConfigFile.getName());

                    zipOutStream.putNextEntry(zipEntry);

                    Files.copy(mainConfigFile.toPath(), zipOutStream);

                    for (BamItem item : items) {
                        String[] dataFileNames = item.getTempDataFileNames();
                        for (String dfp : dataFileNames) {
                            File f = new File(Path.of(App.TEMP_DIR, dfp).toString());
                            System.out.println("Including file '" + f + "'...");
                            ZipEntry ze = new ZipEntry(f.getName());
                            zipOutStream.putNextEntry(ze);
                            Files.copy(f.toPath(), zipOutStream);
                        }
                    }

                    zipOutStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
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
