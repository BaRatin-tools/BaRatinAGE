package org.baratinage.ui.baratin;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.BamProjectType;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.commons.StructuralErrorModelBamItem;
import org.baratinage.ui.component.NameSymbolUnit;

import javax.swing.JButton;
import javax.swing.JMenuItem;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class BaratinProject extends BamProject {

    public BaratinProject() {
        super(BamProjectType.BARATIN);

        initBamItemType(
                BamItemType.HYDRAULIC_CONFIG,
                "hydraulic_config",
                (String uuid) -> {
                    return new HydraulicConfiguration(uuid, this);
                });

        initBamItemType(
                BamItemType.HYDRAULIC_CONFIG_BAC,
                "hydraulic_config",
                (String uuid) -> {
                    return new HydraulicConfigurationBAC(uuid, this);
                });

        initBamItemType(
                BamItemType.HYDRAULIC_CONFIG_QFH,
                "hydraulic_config",
                (String uuid) -> {
                    return new HydraulicConfigurationQFH(uuid, this);
                });

        initBamItemType(
                BamItemType.GAUGINGS,
                "gaugings",
                (String uuid) -> {
                    return new Gaugings(uuid, this);
                });

        initBamItemType(
                BamItemType.STRUCTURAL_ERROR,
                "structural_error_model",
                (String uuid) -> {

                    return new StructuralErrorModelBamItem(
                            uuid,
                            this,
                            new NameSymbolUnit("Débit", "Q", "m<sup>3</sup>.s<sup>-1</sup>"));

                });

        initBamItemType(
                BamItemType.RATING_CURVE,
                "rating_curve",
                (String uuid) -> {
                    return new RatingCurve(uuid, this);
                });

        initBamItemType(
                BamItemType.LIMNIGRAPH,
                "limnigraph",
                (String uuid) -> {
                    return new Limnigraph(uuid, this);
                });

        initBamItemType(
                BamItemType.HYDROGRAPH,
                "hydrograph",
                (String uuid) -> {
                    return new Hydrograph(uuid, this);
                });

        initBamItemType(
                BamItemType.COMPARING_RATING_CURVES,
                "tools",
                (String uuid) -> {
                    return new RatingCurveCompare(uuid, this);
                });

        // resetting toolbar and component menu (where "add bam item" buttons are)
        AppSetup.MAIN_FRAME.mainMenuBar.componentMenu.removeAll();
        AppSetup.MAIN_FRAME.mainToolBars.clearBamItemTools();

        addAddBamItemBtns(BamItemType.HYDRAULIC_CONFIG);
        addAddBamItemBtns(BamItemType.HYDRAULIC_CONFIG_BAC);
        addAddBamItemBtns(BamItemType.HYDRAULIC_CONFIG_QFH);
        addAddBamItemBtns(BamItemType.GAUGINGS);
        addAddBamItemBtns(BamItemType.STRUCTURAL_ERROR);
        addAddBamItemBtns(BamItemType.RATING_CURVE);
        addAddBamItemBtns(BamItemType.LIMNIGRAPH);
        addAddBamItemBtns(BamItemType.HYDROGRAPH);
        addAddBamItemBtns(BamItemType.COMPARING_RATING_CURVES);

        T.t(this, () -> {
            BamItemList strucErrBamItems = BAM_ITEMS.filterByType(BamItemType.STRUCTURAL_ERROR);
            for (BamItem item : strucErrBamItems) {
                ((StructuralErrorModelBamItem) item).updateOutputNames(T.text("discharge"));
            }
        });
    }

    private void addAddBamItemBtns(BamItemType itemType) {
        AppSetup.MAIN_FRAME.mainToolBars.addBamItemTool(
                BamItem.getAddBamItemBtn(
                        new JButton(), this,
                        itemType,
                        false, true));
        AppSetup.MAIN_FRAME.mainMenuBar.componentMenu.add(BamItem.getAddBamItemBtn(
                new JMenuItem(), this,
                itemType,
                true, true));

        for (ExplorerItem explorerItem : EXPLORER.rootNode.getChildrenExplorerItems()) {
            explorerItem.contextMenu.add(BamItem.getAddBamItemBtn(
                    new JMenuItem(), this,
                    itemType,
                    true, true));
        }
    }

    public void addDefaultBamItems() {
        BamItem selectedItem = addBamItem(BamItemType.HYDRAULIC_CONFIG);
        addBamItem(BamItemType.GAUGINGS);
        addBamItem(BamItemType.STRUCTURAL_ERROR);
        addBamItem(BamItemType.RATING_CURVE);
        addBamItem(BamItemType.LIMNIGRAPH);
        addBamItem(BamItemType.HYDROGRAPH);
        setCurrentBamItem(selectedItem);
    }

    @Override
    public BamItemList getOrderedBamItemList() {

        return BAM_ITEMS.getOrderedCopy(
                BamItemType.HYDRAULIC_CONFIG,
                BamItemType.HYDRAULIC_CONFIG_BAC,
                BamItemType.HYDRAULIC_CONFIG_QFH,
                BamItemType.GAUGINGS,
                BamItemType.STRUCTURAL_ERROR,
                BamItemType.LIMNIGRAPH,
                BamItemType.RATING_CURVE,
                BamItemType.HYDROGRAPH,
                BamItemType.COMPARING_RATING_CURVES);
    }

}
