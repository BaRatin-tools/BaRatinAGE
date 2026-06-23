package org.baratinage.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.baratinage.AppSetup;
import org.baratinage.report_exporter.ReportExporter;
import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.config.ConfigItem;
import org.baratinage.utils.ConsoleLogger;

public class MainMenuBar extends JMenuBar {

    private final JMenu fileMenu;
    public final JMenu componentMenu;
    private final JMenu optionMenu;
    private final JMenu helpMenu;

    private final Set<JMenuItem> projectOnlyMenuItems = new HashSet<>();

    private final Map<String, JCheckBoxMenuItem> translationMenuItems;

    private DebugMenu debugMenu;

    public MainMenuBar() {

        fileMenu = new JMenu();

        add(fileMenu);

        componentMenu = new JMenu("Components");
        add(componentMenu);

        optionMenu = new JMenu("Options");
        add(optionMenu);

        helpMenu = new JMenu("Help");
        add(helpMenu);

        if (!AppSetup.IS_PACKAGED) {
            toggleDebugMenu();
        }

        translationMenuItems = new HashMap<>();

        initFileMenu();
        initOptionMenu();
        initHelpMenu();

        projectOnlyMenuItems.add(componentMenu);

        T.t(this, fileMenu, false, "file");
        T.t(this, componentMenu, false, "components");
        T.t(this, optionMenu, false, "options");
        T.t(this, helpMenu, false, "help");

    }

    public void toggleDebugMenu() {
        if (debugMenu == null) {
            debugMenu = new DebugMenu();
            add(debugMenu);
        } else {
            remove(debugMenu);
            debugMenu = null;
        }
        updateUI();
    }

    public void setEnableForProjectOnlyItem(boolean enabled) {
        for (JMenuItem item : projectOnlyMenuItems) {
            item.setEnabled(enabled);
        }
    }

    private void initFileMenu() {

        AppSetup.SHORTCUTS.setBindingAction("global.new_project", () -> {
            AppSetup.MAIN_FRAME.newProject();
        });
        JMenuItem newProjectMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.new_project");
        T.t(this, newProjectMenuItem, false, "create_baratin_project");
        fileMenu.add(newProjectMenuItem);

        AppSetup.SHORTCUTS.setBindingAction("global.open_project", () -> {
            AppSetup.MAIN_FRAME.loadProject();
        });
        JMenuItem openProjectMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.open_project");
        T.t(this, openProjectMenuItem, false, "open_project");
        fileMenu.add(openProjectMenuItem);

        AppSetup.SHORTCUTS.setBindingAction("global.save_project", () -> {
            AppSetup.MAIN_FRAME.saveProject(false);
        });
        JMenuItem saveProjectMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.save_project");
        T.t(this, saveProjectMenuItem, false, "save_project");
        fileMenu.add(saveProjectMenuItem);

        AppSetup.SHORTCUTS.setBindingAction("global.save_project_as", () -> {
            AppSetup.MAIN_FRAME.saveProject(true);
        });
        JMenuItem saveProjectAsMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.save_project_as");
        T.t(this, saveProjectAsMenuItem, false, "save_project_as");
        fileMenu.add(saveProjectAsMenuItem);

        AppSetup.SHORTCUTS.setBindingAction("global.close_project", () -> {
            AppSetup.MAIN_FRAME.closeProject();
        });
        JMenuItem closeProjectMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.close_project");
        T.t(this, closeProjectMenuItem, false, "close_project");
        fileMenu.add(closeProjectMenuItem);

        fileMenu.addSeparator();

        AppSetup.SHORTCUTS.setBindingAction("global.import_v2_project", () -> {
            AppSetup.MAIN_FRAME.importV2Project();
        });
        JMenuItem importBaratinageV2projectMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.import_v2_project");
        T.t(this, importBaratinageV2projectMenuItem, false, "import_baratinage_v2_project");
        fileMenu.add(importBaratinageV2projectMenuItem);

        fileMenu.addSeparator();

        AppSetup.SHORTCUTS.setBindingAction("global.export_report", () -> {
            if (AppSetup.MAIN_FRAME.currentProject == null) {
                ConsoleLogger.log("No project");
                return;
            }
            ReportExporter reportExport = new ReportExporter(AppSetup.MAIN_FRAME.currentProject);
            reportExport.showDialog();
        });
        JMenuItem exportReportMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.export_report");
        T.t(this, exportReportMenuItem, false, "report_exporter");
        fileMenu.add(exportReportMenuItem);

        fileMenu.addSeparator();

        AppSetup.SHORTCUTS.setBindingAction("global.exit", () -> {
            AppSetup.MAIN_FRAME.close();
        });
        JMenuItem closeMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.exit");
        T.t(this, closeMenuItem, false, "exit");
        fileMenu.add(closeMenuItem);

        projectOnlyMenuItems.add(saveProjectMenuItem);
        projectOnlyMenuItems.add(saveProjectAsMenuItem);
        projectOnlyMenuItems.add(closeProjectMenuItem);
        projectOnlyMenuItems.add(exportReportMenuItem);
    }

    public JMenu createLanguageSwitcherMenu() {

        JMenu switchLanguageMenuItem = new JMenu();
        T.t(this, switchLanguageMenuItem, false, "change_language");
        switchLanguageMenuItem.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                updateLanguageSwitcherMenu();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        Locale[] allLocales = T.getAvailableLocales();
        for (Locale targetLocale : allLocales) {
            String targetLocaleKey = targetLocale.getLanguage();
            JCheckBoxMenuItem item = new JCheckBoxMenuItem();
            translationMenuItems.put(targetLocaleKey, item);
            if (targetLocaleKey.equals("ko")) {
                item.setFont(AppSetup.KO_FONT);
            }
            T.t(this, () -> {
                item.setText(T.getLocaleLabelString(targetLocale));
            });

            item.addActionListener((e) -> {
                ConsoleLogger.log("swtiching language to '" + targetLocaleKey + "'");
                if (targetLocaleKey.equals("ko")) {
                    // korean needs a restart since a different font must be used
                    CommonDialog.infoDialog(T.text("restart_needed_msg"));
                }
                T.setLocale(targetLocaleKey);
                AppSetup.CONFIG.LANGUAGE_KEY.set(targetLocaleKey, ConfigItem.SCOPE.GLOBAL);
                AppSetup.CONFIG.saveConfig();
                DebugMenu.resetPlotLegendsAndAxis(); // FIXME: this is a temporary fix
            });
            switchLanguageMenuItem.add(item);
        }
        return switchLanguageMenuItem;
    }

    public void updateLanguageSwitcherMenu() {
        String currentLocalKey = T.getLocaleKey();
        for (String key : translationMenuItems.keySet()) {
            translationMenuItems.get(key).setSelected(key.equals(currentLocalKey));
        }
    }

    private void initOptionMenu() {
        JMenu lgSwitcherMenu = createLanguageSwitcherMenu();
        optionMenu.add(lgSwitcherMenu);
        JMenuItem preferenceMenuItem = new JMenuItem();
        T.t(this, preferenceMenuItem, false, "preferences");
        preferenceMenuItem.addActionListener(l -> {
            AppSetup.CONFIG.openConfigDialog();
        });
        optionMenu.add(preferenceMenuItem);
    }

    private void initHelpMenu() {

        AppSetup.SHORTCUTS.setBindingAction("global.help", () -> {
            try {
                Desktop.getDesktop().browse(new URI("https://baratin-tools.github.io/"));
            } catch (IOException | URISyntaxException err) {
                ConsoleLogger.error(err);
            }
        });
        JMenuItem helpMenuItem = AppSetup.SHORTCUTS.createMenuItem("global.help");
        T.t(this, helpMenuItem, false, "help");
        helpMenu.add(helpMenuItem);

        JMenuItem aboutMenuItem = new JMenuItem();
        T.t(this, aboutMenuItem, false, "about");
        aboutMenuItem.addActionListener((e) -> {
            new AppAbout().showAboutDialog();
        });
        helpMenu.add(aboutMenuItem);
    }

}
