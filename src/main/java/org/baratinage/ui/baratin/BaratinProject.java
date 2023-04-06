package org.baratinage.ui.baratin;

import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileFilter;

import org.baratinage.jbam.utils.Write;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.component.Explorer;
import org.baratinage.ui.component.ExplorerItem;
// import org.baratinage.ui.component.ImportedData;
import org.baratinage.ui.component.NoScalingIcon;
import org.baratinage.ui.container.RowColPanel;
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

public class BaratinProject extends RowColPanel {

    // private record ProjectBamItem(BamItem bamItem, ExplorerItem explorerItem) {
    // }

    // private List<BamItem> items;
    private BamItemList items;

    private ExplorerItem hydraulicConfig;
    private ExplorerItem gaugings;
    private ExplorerItem structuralError;
    private ExplorerItem ratingCurve;

    static private final String hydraulicConfigIconPath = "./resources/icons/Hydraulic_icon.png";
    static private final String gaugingsIconPath = "./resources/icons/Gauging_icon.png";
    static private final String structuralErrIconPath = "./resources/icons/Error_icon.png";
    static private final String ratingCurveIconPath = "./resources/icons/RC_icon.png";

    private RowColPanel actionBar;
    JSplitPane content;

    private Explorer explorer;
    private RowColPanel currentPanel;

    public BaratinProject() {
        super(AXIS.COL);

        this.items = new BamItemList();
        this.actionBar = new RowColPanel(AXIS.ROW, ALIGN.START, ALIGN.STRETCH);
        this.actionBar.setPadding(5);
        this.actionBar.setGap(5);
        this.appendChild(this.actionBar, 0);
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

        this.content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.content.setBorder(BorderFactory.createEmptyBorder());
        this.appendChild(new JSeparator(), 0);
        this.appendChild(this.content, 1);

        this.explorer = new Explorer("Explorateur");
        this.setupExplorer();

        this.currentPanel = new RowColPanel(AXIS.COL);
        this.currentPanel.setGap(5);

        this.content.setLeftComponent(this.explorer);
        this.content.setRightComponent(this.currentPanel);
        this.content.setResizeWeight(0);

        addHydraulicConfig(); // FIXME: feels like default should be empty to be able to set a default
                              // elsewhere and import content from a file

    }

    // FIXME: this method is typically something that should be set in a parent
    // class that represents BaM project (as an abstract method...)
    private void setupExplorer() {

        this.explorer.addTreeSelectionListener(e -> {
            ExplorerItem explorerItem = explorer.getLastSelectedPathComponent();
            if (explorerItem != null) {
                BamItem bamItem = findBamItem(explorerItem.id);
                if (bamItem != null) {
                    this.currentPanel.clear();
                    this.currentPanel.appendChild(bamItem, 1);

                } else {
                    this.currentPanel.clear();
                }
                this.updateUI();
            }
        });

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

    private void addItem(BamItem bamItem, ExplorerItem explorerItem) {

        items.add(bamItem);
        bamItem.updateSiblings(items);
        // bamItem.setSiblings(items);

        bamItem.addFollower(o -> {
            String newName = bamItem.getName();
            if (newName.equals("")) {
                newName = "<html><div style='color: red; font-style: italic'>Sans nom</div></html>";
            }
            explorerItem.label = newName;
            explorer.updateItemView(explorerItem);
        });
        bamItem.addDeleteAction(e -> {
            deleteItem(bamItem, explorerItem);
        });

        this.explorer.appendItem(explorerItem);
        this.explorer.expandItem(hydraulicConfig);
        this.explorer.selectItem(explorerItem);

    }

    // private void deleteItem(String id) {
    private void deleteItem(BamItem bamItem, ExplorerItem explorerItem) {
        // BamItem item = findBamItem(id);
        items.remove(bamItem);
        this.explorer.removeItem(explorerItem);
        this.explorer.selectItem(explorerItem.parentItem);
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
        ExplorerItem explorerItem = new ExplorerItem(
                ratingCurveItem.getUUID(),
                ratingCurveItem.getName(),
                ratingCurveIconPath,
                ratingCurve);
        addItem(ratingCurveItem, explorerItem);

    }

    private BamItem findBamItem(String id) {
        for (BamItem item : this.items) {
            if (item.getUUID().equals(id)) {
                return item;
            }
        }
        return null;
    }

    // private ExplorerItem findExplorerItem(String id) {

    // }

}
