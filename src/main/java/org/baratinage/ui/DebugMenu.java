package org.baratinage.ui;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.baratinage.translation.T;

public class DebugMenu extends JMenu {

    private List<WritableRaster> rasters = new ArrayList<>();

    public DebugMenu() {
        super("DEBUG / DEV");

        JMenuItem gcBtn = new JMenuItem("Garbage collection");
        add(gcBtn);
        gcBtn.addActionListener((e) -> {
            System.gc();
        });

        JMenuItem tPrintStateBtn = new JMenuItem("T print state");
        add(tPrintStateBtn);
        tPrintStateBtn.addActionListener((e) -> {
            T.printStats(false);
        });

        JMenuItem tPrintStateAllBtn = new JMenuItem("T print state complete");
        add(tPrintStateAllBtn);
        tPrintStateAllBtn.addActionListener((e) -> {
            T.printStats(true);
        });

        JMenuItem cleanupTranslationBtn = new JMenuItem("Cleanup translations");
        add(cleanupTranslationBtn);
        cleanupTranslationBtn.addActionListener((e) -> {
            // T.cleanup();
        });

        JMenuItem remTranslatorsBtn = new JMenuItem("Remove all registered translators");
        add(remTranslatorsBtn);
        remTranslatorsBtn.addActionListener((e) -> {
            T.reset();
        });

        JMenuItem lgResetBtn = new JMenuItem("Reload T resources");
        add(lgResetBtn);
        lgResetBtn.addActionListener((e) -> {
            T.reloadResources();
        });

        JMenuItem modifyAllIconsBtn = new JMenuItem("modify all icons");
        add(modifyAllIconsBtn);
        modifyAllIconsBtn.addActionListener((e) -> {
            AppConfig.AC.ICONS.updateAllIcons();
        });

        JMenuItem updateCompTreeBtn = new JMenuItem("update component tree UI");
        add(updateCompTreeBtn);
        updateCompTreeBtn.addActionListener((e) -> {
            SwingUtilities.updateComponentTreeUI(AppConfig.AC.APP_MAIN_FRAME);
        });

        JMenuItem allocateHugeRasterBtn = new JMenuItem("Allocate huge Raster");
        add(allocateHugeRasterBtn);
        allocateHugeRasterBtn.addActionListener((e) -> {
            int size = 10000;
            WritableRaster raster = Raster.createBandedRaster(DataBuffer.TYPE_INT, size, size, 3, new Point(0, 0));
            rasters.add(raster);
        });

        JMenuItem clearRasters = new JMenuItem("Clear Rasters");
        add(clearRasters);
        clearRasters.addActionListener((e) -> {
            rasters.clear();
        });
    }
}
