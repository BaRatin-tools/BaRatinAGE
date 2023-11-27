package org.baratinage.ui;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;

public class DebugMenu extends JMenu {

    public DebugMenu() {
        super("DEBUG / DEV");

        JMenuItem clearConsoleBtn = new JMenuItem("Clear console");
        add(clearConsoleBtn);
        clearConsoleBtn.addActionListener((e) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        });

        JMenuItem gcBtn = new JMenuItem("Garbage collection");
        add(gcBtn);
        gcBtn.addActionListener((e) -> {
            System.gc();
        });

        JMenuItem tPrintStateBtn = new JMenuItem("Print Translatables stats");
        add(tPrintStateBtn);
        tPrintStateBtn.addActionListener((e) -> {
            T.printStats(false);
        });

        JMenuItem tPrintStateAllBtn = new JMenuItem("Print Translatables stats details");
        add(tPrintStateAllBtn);
        tPrintStateAllBtn.addActionListener((e) -> {
            T.printStats(true);
        });

        JMenuItem cleanupTranslationBtn = new JMenuItem("Cleanup translations");
        add(cleanupTranslationBtn);
        cleanupTranslationBtn.addActionListener((e) -> {
            // T.cleanup();
        });

        JMenuItem remTranslatorsBtn = new JMenuItem("Remove all registered Translatables");
        add(remTranslatorsBtn);
        remTranslatorsBtn.addActionListener((e) -> {
            T.reset();
        });

        JMenuItem lgResetBtn = new JMenuItem("Reload T resources");
        add(lgResetBtn);
        lgResetBtn.addActionListener((e) -> {
            T.reloadResources();
        });

        JMenuItem modifyAllIconsBtn = new JMenuItem("Update all icons");
        add(modifyAllIconsBtn);
        modifyAllIconsBtn.addActionListener((e) -> {
            AppSetup.ICONS.updateAllIcons();
        });

        JMenuItem updateCompTreeBtn = new JMenuItem("Update component tree UI");
        add(updateCompTreeBtn);
        updateCompTreeBtn.addActionListener((e) -> {
            SwingUtilities.updateComponentTreeUI(AppSetup.MAIN_FRAME);
        });

    }
}
