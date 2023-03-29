package org.baratinage.ui.baratin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.component.Explorer;
import org.baratinage.ui.component.ExplorerItem;
// import org.baratinage.ui.component.ImportedData;
import org.baratinage.ui.component.NoScalingIcon;
import org.baratinage.ui.container.RowColPanel;

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

// public class ProjectBaratin extends FlexPanel {
public class BaratinProject extends RowColPanel {

    private record ProjectBamItem(BamItem bamItem, ExplorerItem explorerItem) {
    }

    private List<ProjectBamItem> hydraulicConfigs;
    // private List<ImportedData> importedData;
    // private List<Gaugings> gaugings;
    // private List<Limnigraph> limnigraphs;
    // private List<StructuralError> structuralErrors;
    // private List<PosteriorRatingCurve> posteriorRatingCurve;
    // private List<Hydrograph> hydrographs;
    // private List<Ui

    private ExplorerItem hydraulicConfig;
    private ExplorerItem gaugings;
    private ExplorerItem structuralError;
    private ExplorerItem posteriorRatingCurve;

    static private final String hydroConfigIconPath = "./resources/icons/Hydraulic_icon.png";
    static private final String gaugingsIconPath = "./resources/icons/Gauging_icon.png";
    static private final String structuralErrIconPath = "./resources/icons/Error_icon.png";
    static private final String ratingCurveIconPath = "./resources/icons/RC_icon.png";

    RowColPanel actionBar;
    JSplitPane content;

    Explorer explorer;
    RowColPanel currentPanel;
    RowColPanel currentPanelHeader;
    JLabel currentPanelHeaderLabel;
    RowColPanel currentPanelContent;

    public BaratinProject() {
        super(AXIS.COL);

        this.hydraulicConfigs = new ArrayList<>();

        this.actionBar = new RowColPanel(AXIS.ROW, ALIGN.START, ALIGN.STRETCH);
        this.appendChild(this.actionBar, 0);
        JButton btnNewHydraulicConfig = new JButton();
        btnNewHydraulicConfig.setText("Nouvelle configuration hydraulique");
        btnNewHydraulicConfig.setIcon(new NoScalingIcon("./resources/icons/Hydraulic_icon.png"));
        btnNewHydraulicConfig.addActionListener(e -> {
            System.out.println("new hydrau config");
            addHydraulicConfig();
        });

        this.actionBar.appendChild(btnNewHydraulicConfig);
        this.actionBar.appendChild(new JButton("action 2"));

        this.content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.appendChild(this.content, 1);

        this.explorer = new Explorer("Explorateur");
        this.setupExplorer();

        this.currentPanel = new RowColPanel(AXIS.COL);
        this.currentPanel.setPadding(5);
        this.currentPanel.setGap(5);
        this.currentPanelHeader = new RowColPanel(AXIS.ROW);
        this.currentPanelContent = new RowColPanel(AXIS.ROW);
        this.currentPanel.appendChild(this.currentPanelHeader, 0);
        this.currentPanel.appendChild(this.currentPanelContent, 1);

        // this.currentPanel.appendChild(new JLabel("current panel"));
        // this.currentPanel.appendChild(new JLabel("with various things in it"));

        this.content.setLeftComponent(this.explorer);
        this.content.setRightComponent(this.currentPanel);
        this.content.setResizeWeight(0);

        // this.appendChild(explorer);

        // this.appendChild(new JLabel("main"));

        addHydraulicConfig();
        // addHydraulicConfig();

    }

    private void setupExplorer() {

        // this.explorer.setMinimumSize(getMinimumSize());
        this.explorer.addTreeSelectionListener(e -> {
            ExplorerItem item = explorer.getLastSelectedPathComponent();
            if (item != null) {
                System.out.println(item.id);
                ProjectBamItem projBamItem = findProjectBamItem(item.id);
                if (projBamItem != null) {

                    this.currentPanelHeader.clear();
                    this.currentPanelHeaderLabel = new JLabel(projBamItem.bamItem.getName());
                    JButton deleteButton = new JButton("Delete");
                    deleteButton.addActionListener(ale -> {
                        System.out.println("Delete " + projBamItem.bamItem.getName());
                        deleteHydraulicConfig(projBamItem.bamItem.getUUID());
                    });
                    this.currentPanelHeader.appendChild(this.currentPanelHeaderLabel, 1);
                    this.currentPanelHeader.appendChild(deleteButton, 0);

                    this.currentPanelContent.clear();
                    this.currentPanelContent.appendChild(projBamItem.bamItem, 1);

                    this.currentPanel.clear();
                    this.currentPanel.appendChild(this.currentPanelHeader, 0);
                    this.currentPanel.appendChild(this.currentPanelContent, 1);

                } else {
                    this.currentPanel.clear();
                }
                this.updateUI();
            }
        });

        hydraulicConfig = new ExplorerItem(
                "hc",
                "Configurations hydrauliques",
                hydroConfigIconPath);
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

        posteriorRatingCurve = new ExplorerItem(
                "rc",
                "<html>Courbes de tarage <i>(a posteriori)</i></html>",
                ratingCurveIconPath);
        this.explorer.appendItem(posteriorRatingCurve);

    }

    private void addHydraulicConfig() {
        HydraulicConfiguration bamItem = new HydraulicConfiguration();
        bamItem.addChangeListener((i) -> {
            ProjectBamItem pbi = findProjectBamItem(i.getUUID());
            String newName = pbi.bamItem.getName();
            if (newName.equals("")) {
                newName = "<html><div style='color: red; font-style: italic'>Sans nom</div></html>";
            }
            System.out.println("HAS CHANGE >>> " + newName);
            currentPanelHeaderLabel.setText(newName);
            pbi.explorerItem.label = newName;
            explorer.updateItemView(pbi.explorerItem);
            // explorerItem
        });
        ExplorerItem explorerItem = new ExplorerItem(
                bamItem.getUUID(),
                bamItem.getName(),
                hydroConfigIconPath,
                hydraulicConfig);
        hydraulicConfigs.add(new ProjectBamItem(bamItem, explorerItem));
        this.explorer.appendItem(explorerItem);
        this.explorer.expandItem(hydraulicConfig);
        this.explorer.selectItem(explorerItem);
    }

    private void deleteHydraulicConfig(String id) {
        ProjectBamItem item = findProjectBamItem(id);
        hydraulicConfigs.remove(item);
        this.explorer.removeItem(item.explorerItem);
        this.explorer.selectItem(item.explorerItem.parentItem);
    }

    private ProjectBamItem findProjectBamItem(String id) {
        for (ProjectBamItem pbi : this.hydraulicConfigs) {
            if (pbi.bamItem.getUUID().equals(id)) {
                return pbi;
            }
        }
        return null;
    }

}
