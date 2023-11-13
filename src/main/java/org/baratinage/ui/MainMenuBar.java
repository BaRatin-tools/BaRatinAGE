package org.baratinage.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

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

    public MainMenuBar() {

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

        initFileMenu();
        initOptionMenu();

        T.t(this, fileMenu, false, "files");
        T.t(this, componentMenu, false, "components");
        T.t(this, optionMenu, false, "options");
        T.t(this, helpMenu, false, "help");

    }

    private void initFileMenu() {

        JMenuItem newProjectMenuItem = new JMenuItem();
        T.t(this, newProjectMenuItem, false, "create_baratin_project");
        newProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newProjectMenuItem.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.newProject();
        });
        fileMenu.add(newProjectMenuItem);

        JMenuItem openProjectMenuItem = new JMenuItem();
        T.t(this, openProjectMenuItem, false, "open_project");
        openProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        openProjectMenuItem.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.loadProject();
        });
        fileMenu.add(openProjectMenuItem);

        T.t(this, saveProjectAsMenuItem, false, "save_project_as");
        saveProjectAsMenuItem
                .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
        saveProjectAsMenuItem.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.saveProject(true);
        });
        fileMenu.add(saveProjectAsMenuItem);

        T.t(this, saveProjectMenuItem, false, "save_project");
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.saveProject(false);
        });
        fileMenu.add(saveProjectMenuItem);

        T.t(this, closeProjectMenuItem, false, "close_project");
        closeProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
        closeProjectMenuItem.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.closeProject();
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
                projConver.importProject(
                        f.getAbsolutePath(),
                        (project) -> {
                            ConsoleLogger.log(project);
                            AppConfig.AC.APP_MAIN_FRAME.setCurrentProject(project);
                        });

            }
        });
        fileMenu.add(importBaratinageV2projectMenuItem);

        fileMenu.addSeparator();

        JMenuItem closeMenuItem = new JMenuItem();
        T.t(this, closeMenuItem, false, "exit");
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        closeMenuItem.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.close();
        });
        fileMenu.add(closeMenuItem);
    }

    private void initOptionMenu() {
        JMenu lgSwitcherMenu = AppConfig.AC.APP_MAIN_FRAME.createLanguageSwitcherMenu();
        optionMenu.add(lgSwitcherMenu);
    }

}
