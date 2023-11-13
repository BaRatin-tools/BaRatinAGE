package org.baratinage.ui.baratin;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.BamProjectType;
import org.baratinage.ui.commons.StructuralErrorModelBamItem;
import org.baratinage.ui.component.NameSymbolUnit;
import org.baratinage.translation.T;

public class BaratinProject extends BamProject {

    public BaratinProject() {
        super(BamProjectType.BARATIN);

        initBamItemType(
                BamItemType.HYDRAULIC_CONFIG,
                (String uuid) -> {
                    return new HydraulicConfiguration(uuid, this);
                });

        initBamItemType(
                BamItemType.GAUGINGS,
                (String uuid) -> {
                    return new Gaugings(uuid, this);
                });

        initBamItemType(
                BamItemType.STRUCTURAL_ERROR,
                (String uuid) -> {

                    return new StructuralErrorModelBamItem(
                            uuid,
                            this,
                            new NameSymbolUnit("Débit", "Q", "m<sup>3</sup>.s<sup>-1</sup>"));

                });

        initBamItemType(
                BamItemType.RATING_CURVE,
                (String uuid) -> {
                    return new RatingCurve(uuid, this);
                });

        initBamItemType(
                BamItemType.LIMNIGRAPH,
                (String uuid) -> {
                    return new Limnigraph(uuid, this);
                });

        initBamItemType(
                BamItemType.HYDROGRAPH,
                (String uuid) -> {
                    return new Hydrograph(uuid, this);
                });

        JMenu componentMenu = AppConfig.AC.APP_MAIN_FRAME.mainMenuBar.componentMenu;
        componentMenu.removeAll();
        componentMenu.add(BamItemType.HYDRAULIC_CONFIG.getAddMenuItem());
        componentMenu.add(BamItemType.GAUGINGS.getAddMenuItem());
        componentMenu.add(BamItemType.STRUCTURAL_ERROR.getAddMenuItem());
        componentMenu.add(BamItemType.RATING_CURVE.getAddMenuItem());
        componentMenu.add(BamItemType.LIMNIGRAPH.getAddMenuItem());
        componentMenu.add(BamItemType.HYDROGRAPH.getAddMenuItem());
        componentMenu.updateUI();

        JToolBar projectToolbar = AppConfig.AC.APP_MAIN_FRAME.projectToolbar;
        projectToolbar.removeAll();
        projectToolbar.add(BamItemType.HYDRAULIC_CONFIG.getAddToolbarButton());
        projectToolbar.add(BamItemType.GAUGINGS.getAddToolbarButton());
        projectToolbar.add(BamItemType.STRUCTURAL_ERROR.getAddToolbarButton());
        projectToolbar.add(BamItemType.RATING_CURVE.getAddToolbarButton());
        projectToolbar.add(BamItemType.LIMNIGRAPH.getAddToolbarButton());
        projectToolbar.add(BamItemType.HYDROGRAPH.getAddToolbarButton());
        projectToolbar.updateUI();

        T.t(this, () -> {
            BamItemList strucErrBamItems = BAM_ITEMS.filterByType(BamItemType.STRUCTURAL_ERROR);
            for (BamItem item : strucErrBamItems) {
                ((StructuralErrorModelBamItem) item).updateOutputNames(T.text("discharge"));
            }
        });
    }

    public void addDefaultBamItems() {
        addBamItem(BamItemType.STRUCTURAL_ERROR);
        addBamItem(BamItemType.HYDRAULIC_CONFIG);
    }

    @Override
    public BamItemList getOrderedBamItemList() {

        return BAM_ITEMS.getOrderedCopy(
                BamItemType.HYDRAULIC_CONFIG,
                BamItemType.GAUGINGS,
                BamItemType.STRUCTURAL_ERROR,
                BamItemType.LIMNIGRAPH,
                BamItemType.RATING_CURVE,
                BamItemType.HYDROGRAPH);
    }

}
