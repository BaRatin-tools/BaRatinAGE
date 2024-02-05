package org.baratinage.ui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.baratinage.AppSetup;
import org.baratinage.project_importer.BaratinageV2Importer;
import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.utils.ConsoleLogger;

public class MainMenuBar extends JMenuBar {

    public final JMenu fileMenu;
    public final JMenu componentMenu;
    public final JMenu optionMenu;
    public final JMenu helpMenu;

    public final JMenuItem saveProjectAsMenuItem;
    public final JMenuItem saveProjectMenuItem;
    public final JMenuItem closeProjectMenuItem;

    public final Map<String, JCheckBoxMenuItem> translationMenuItems;

    public MainMenuBar() {

        if (!AppSetup.IS_PACKAGED) {
            add(new DebugMenu());
        }

        fileMenu = new JMenu();

        add(fileMenu);

        componentMenu = new JMenu("Components");
        add(componentMenu);

        optionMenu = new JMenu("Options");
        add(optionMenu);

        helpMenu = new JMenu("Help");
        add(helpMenu);

        saveProjectAsMenuItem = new JMenuItem();
        saveProjectMenuItem = new JMenuItem();
        closeProjectMenuItem = new JMenuItem();

        translationMenuItems = new HashMap<>();

        initFileMenu();
        initOptionMenu();
        initHelpMenu();

        T.t(this, fileMenu, false, "file");
        T.t(this, componentMenu, false, "components");
        T.t(this, optionMenu, false, "options");
        T.t(this, helpMenu, false, "help");

    }

    public void updateMenuEnableStates() {
        componentMenu.setEnabled(componentMenu.getMenuComponentCount() != 0);
    }

    private void initFileMenu() {

        JMenuItem newProjectMenuItem = new JMenuItem();
        T.t(this, newProjectMenuItem, false, "create_baratin_project");
        newProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newProjectMenuItem.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.newProject();
        });
        fileMenu.add(newProjectMenuItem);

        JMenuItem openProjectMenuItem = new JMenuItem();
        T.t(this, openProjectMenuItem, false, "open_project");
        openProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        openProjectMenuItem.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.loadProject();
        });
        fileMenu.add(openProjectMenuItem);

        T.t(this, saveProjectAsMenuItem, false, "save_project_as");
        saveProjectAsMenuItem
                .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
        saveProjectAsMenuItem.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.saveProject(true);
        });
        fileMenu.add(saveProjectAsMenuItem);

        T.t(this, saveProjectMenuItem, false, "save_project");
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.saveProject(false);
        });
        fileMenu.add(saveProjectMenuItem);

        T.t(this, closeProjectMenuItem, false, "close_project");
        closeProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
        closeProjectMenuItem.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.closeProject();
        });
        fileMenu.add(closeProjectMenuItem);

        fileMenu.addSeparator();
        JMenuItem importBaratinageV2projectMenuItem = new JMenuItem();
        T.t(this, importBaratinageV2projectMenuItem, false, "import_baratinage_v2_project");
        importBaratinageV2projectMenuItem.addActionListener((e) -> {
            File f = CommonDialog.openFileDialog(T.text("import_baratinage_v2_project"),
                    T.text("bar_zip_file_format"), "bar.zip", "BAR.ZIP");
            if (f != null) {
                BaratinageV2Importer projConver = new BaratinageV2Importer();
                try {
                    projConver.importProject(
                            f.getAbsolutePath(),
                            (project) -> {
                                ConsoleLogger.log(project);
                                AppSetup.MAIN_FRAME.setCurrentProject(project);
                            });
                } catch (Exception importError) {
                    ConsoleLogger.error(importError);
                    CommonDialog.errorDialog(T.text("import_v2_project_error"));
                }
            }
        });
        fileMenu.add(importBaratinageV2projectMenuItem);

        fileMenu.addSeparator();

        JMenuItem closeMenuItem = new JMenuItem();
        T.t(this, closeMenuItem, false, "exit");
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        closeMenuItem.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.close();
        });
        fileMenu.add(closeMenuItem);
    }

    public JMenu createLanguageSwitcherMenu() {

        JMenu switchLanguageMenuItem = new JMenu();
        T.t(this, switchLanguageMenuItem, false, "change_language");

        Locale[] allLocales = T.getAvailableLocales();
        for (Locale targetLocale : allLocales) {
            String targetLocaleKey = targetLocale.getLanguage();
            String nameInTargetLocale = targetLocale.getDisplayName(targetLocale);
            JCheckBoxMenuItem item = new JCheckBoxMenuItem();
            translationMenuItems.put(targetLocaleKey, item);
            if (targetLocaleKey.equals("ko")) {
                item.setFont(AppSetup.KO_FONT);
            }
            T.t(this, () -> {
                Locale currentLocale = T.getLocale();
                String nameInCurrentLocale = targetLocale.getDisplayName(currentLocale);
                item.setText(nameInCurrentLocale + " - " + nameInTargetLocale);
            });

            item.addActionListener((e) -> {
                ConsoleLogger.log("swtiching language to '" + targetLocaleKey + "'");
                if (targetLocaleKey.equals("ko")) {
                    // korean needs a restart since a different font must be used
                    CommonDialog.infoDialog(T.text("restart_needed_msg"));
                }
                T.setLocale(targetLocaleKey);
                updateLanguageSwitcherMenu();
            });
            switchLanguageMenuItem.add(item);
        }
        updateLanguageSwitcherMenu();
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
    }

    private void initHelpMenu() {
        JMenuItem helpMenuItem = new JMenuItem();
        T.t(this, helpMenuItem, false, "help");
        helpMenuItem.addActionListener((e) -> {
            if (CommonDialog.confirmDialog(
                    String.format("%s. %s",
                            T.text("no_help_availabel"),
                            T.text("open_v2_help_question")),
                    T.text("no_help_availabel"))) {
                String localKey = T.getLocaleKey();
                File f = new File("resources/help/v2/" + localKey + "/index.html");
                if (!f.exists()) {
                    localKey = "en";
                    f = new File("resources/help/v2" + localKey + "/index.html");
                }
                try {
                    Desktop.getDesktop().browse(f.toURI());
                } catch (IOException ex) {
                    ConsoleLogger.error(ex);
                }
            }
        });
        helpMenu.add(helpMenuItem);

        JMenuItem aboutMenuItem = new JMenuItem();
        T.t(this, aboutMenuItem, false, "about");
        aboutMenuItem.addActionListener((e) -> {
            new AppAbout().showAboutDialog();
        });
        helpMenu.add(aboutMenuItem);
    }

}
