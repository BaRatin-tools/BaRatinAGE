package org.baratinage.ui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.baratinage.AppSetup;
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
            AppSetup.MAIN_FRAME.importV2Project();
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
                AppSetup.CONFIG.LANGUAGE_KEY.set(targetLocaleKey);
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
        JMenuItem helpMenuItem = new JMenuItem();
        T.t(this, helpMenuItem, false, "help");
        helpMenuItem.addActionListener((e) -> {

            try {
                Desktop.getDesktop().browse(new URI("https://baratin-tools.github.io/"));
            } catch (IOException | URISyntaxException err) {
                ConsoleLogger.error(err);
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
