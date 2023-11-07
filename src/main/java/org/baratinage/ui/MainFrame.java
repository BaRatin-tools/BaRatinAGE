package org.baratinage.ui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.baratinage.project_importer.BaratinageV2Importer;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainFrame extends JFrame {

    private RowColPanel projectPanel;
    private BamProject currentProject;

    public JMenuBar mainMenuBar;
    public JMenu baratinMenu;

    public JMenuItem saveProjectAsMenuItem;
    public JMenuItem saveProjectMenuItem;
    public JMenuItem closeProjectMenuItem;

    public MainFrame() {

        new AppConfig(this);

        T.init();
        SimpleNumberField.init();
        CommonDialog.init();

        setIconImage(AppConfig.AC.ICONS.BARATINAGE_ICON_LARGE.getImage());
        setTitle(AppConfig.AC.APP_NAME);

        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Misc.showOnScreen(0, this);

        mainMenuBar = new JMenuBar();

        // DebugMenu debugMenu = new DebugMenu();
        // mainMenuBar.add(debugMenu);

        JMenu fileMenu = new JMenu();
        T.t(this, fileMenu, false, "files");
        mainMenuBar.add(fileMenu);

        JMenuItem newProjectMenuItem = new JMenuItem();
        T.t(this, newProjectMenuItem, false, "create_baratin_project");
        newProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newProjectMenuItem.addActionListener((e) -> {
            newProject();
        });
        fileMenu.add(newProjectMenuItem);

        JMenuItem openProjectMenuItem = new JMenuItem();
        T.t(this, openProjectMenuItem, false, "open_project");
        openProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        openProjectMenuItem.addActionListener((e) -> {
            loadProject();
        });
        fileMenu.add(openProjectMenuItem);

        saveProjectAsMenuItem = new JMenuItem();
        T.t(this, saveProjectAsMenuItem, false, "save_project_as");
        saveProjectAsMenuItem
                .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
        saveProjectAsMenuItem.addActionListener((e) -> {
            saveProject(true);
        });
        fileMenu.add(saveProjectAsMenuItem);

        saveProjectMenuItem = new JMenuItem();
        T.t(this, saveProjectMenuItem, false, "save_project");
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.addActionListener((e) -> {
            saveProject(false);
        });
        fileMenu.add(saveProjectMenuItem);

        closeProjectMenuItem = new JMenuItem();
        T.t(this, closeProjectMenuItem, false, "close_project");
        closeProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
        closeProjectMenuItem.addActionListener((e) -> {
            closeProject();
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
                            setCurrentProject(project);
                        });

            }
        });
        fileMenu.add(importBaratinageV2projectMenuItem);

        fileMenu.addSeparator();

        JMenuItem closeMenuItem = new JMenuItem();
        T.t(this, closeMenuItem, false, "exit");
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        closeMenuItem.addActionListener((e) -> {
            close();
        });
        fileMenu.add(closeMenuItem);

        JMenu optionMenu = new JMenu("Options");
        mainMenuBar.add(optionMenu);

        JMenu lgSwitcherMenu = createLanguageSwitcherMenu();
        optionMenu.add(lgSwitcherMenu);

        setJMenuBar(mainMenuBar);

        projectPanel = new RowColPanel(RowColPanel.AXIS.COL);
        add(projectPanel);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                // Dimension dim = MainFrame.this.getSize();
            }

            public void componentMoved(ComponentEvent e) {
                // Point loc = MainFrame.this.getLocation();
            }
        });

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MainFrame.this.close();
            }
        });

        addWindowStateListener((e) -> {
            if (MainFrame.this.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                ConsoleLogger.log("maximized window");
            } else if (MainFrame.this.getExtendedState() == JFrame.NORMAL) {
                ConsoleLogger.log("normal window");
            } else if (MainFrame.this.getExtendedState() == JFrame.ICONIFIED) {
                ConsoleLogger.log("iconified window");
            } else {
                if (MainFrame.this.getExtendedState() == 7) {
                    ConsoleLogger.log("iconified window?");
                } else {
                    ConsoleLogger.log("iconified/unknown window?");
                }
            }
        });

        setVisible(true);
        setCurrentProject(null);
    }

    Map<String, JCheckBoxMenuItem> translationMenuItems = new HashMap<>();

    public JMenu createLanguageSwitcherMenu() {

        JMenu switchLanguageMenuItem = new JMenu();
        T.t(this, switchLanguageMenuItem, false, "change_language");

        List<String> tKeys = T.getAvailableLocales();
        for (String tKey : tKeys) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem();
            translationMenuItems.put(tKey, item);
            T.t(this, () -> {
                Locale currentLocale = T.getLocale();
                Locale targetLocale = Locale.forLanguageTag(tKey);
                String currentLocaleText = targetLocale.getDisplayName(targetLocale);
                String targetLocaleText = targetLocale.getDisplayName(currentLocale);
                item.setText(currentLocaleText + " - " + targetLocaleText);
            });

            item.addActionListener((e) -> {
                ConsoleLogger.log("swtiching language to '" + tKey + "'");
                T.setLocale(tKey);
                updateLanguageSwitcherMenu();
                // FIXME: how to recursively update the whole Frame?
            });
            switchLanguageMenuItem.add(item);
        }
        updateLanguageSwitcherMenu();
        return switchLanguageMenuItem;
    }

    public void updateUI() {
        projectPanel.updateUI();
        if (currentProject != null) {
            currentProject.updateUI();
        }
        mainMenuBar.updateUI();
    }

    public void updateLanguageSwitcherMenu() {
        String currentLocalKey = T.getLocaleKey();
        for (String key : translationMenuItems.keySet()) {
            translationMenuItems.get(key).setSelected(key.equals(currentLocalKey));
        }
    }

    public void checkIfUnsavedChanges() {
        if (currentProject != null) {
            if (currentProject.checkUnsavedChange()) {
                updateFrameTitle();
            }
        }
    }

    public void setCurrentProject(BamProject project) {
        if (currentProject != null) {
            T.clear(currentProject);
        }
        currentProject = project;
        projectPanel.clear();

        boolean projectIsNull = project == null;
        if (!projectIsNull) {
            projectPanel.appendChild(project);
        }
        saveProjectAsMenuItem.setEnabled(!projectIsNull);
        saveProjectMenuItem.setEnabled(!projectIsNull);
        closeProjectMenuItem.setEnabled(!projectIsNull);

        updateFrameTitle();
        T.updateTranslations();
        projectPanel.updateUI();
    }

    public boolean confirmLoosingUnsavedChanges() {
        if (currentProject != null) {
            currentProject.checkUnsavedChange();
            if (currentProject.hasUnsavedChange()) {
                return CommonDialog.confirmDialog(
                        T.text("unsaved_changes_will_be_lost") + "\n" +
                                T.text("proceed_question"),
                        T.text("are_you_sure"));

            }
        }
        return true;
    }

    public void closeProject() {
        if (confirmLoosingUnsavedChanges()) {
            setCurrentProject(null);
        }
    }

    public void updateFrameTitle() {
        setTitle(AppConfig.AC.APP_NAME);
        if (currentProject != null) {
            String projectPath = currentProject.getProjectPath();
            if (projectPath != null) {
                String projectName = Path.of(projectPath).getFileName().toString();
                String unsavedString = currentProject.hasUnsavedChange() ? "*" : "";
                setTitle(AppConfig.AC.APP_NAME + " - " + projectName + " - " + projectPath + unsavedString);
            }
        }
    }

    public void newProject() {
        AppConfig.AC.clearTempDirectory();
        BaratinProject newProject = new BaratinProject();
        newProject.addDefaultBamItems();
        setCurrentProject(newProject);
    }

    public void loadProject() {
        File f = CommonDialog.openFileDialog(
                null,
                T.text("baratinage_file"),
                "bam", "BAM");

        if (f == null) {
            ConsoleLogger.error("loading project failed! Selected file is null.");
            return;
        }
        String fullFilePath = f.getAbsolutePath();
        loadProject(fullFilePath);
    }

    public void loadProject(String projectFilePath) {
        if (projectFilePath != null) {
            BamProject.loadProject(projectFilePath, (bamProject) -> {
                bamProject.setProjectPath(projectFilePath);
                setCurrentProject(bamProject);
            },
                    () -> {
                        CommonDialog.errorDialog(T.text("error_opening_project"));
                    });
        }
    }

    public void saveProject(boolean saveAs) {

        if (currentProject == null) {
            ConsoleLogger.error("no project to save.");
            return;
        }
        if (!saveAs) {
            String pp = currentProject.getProjectPath();
            if (pp != null) {
                saveProject(pp);
                return;
            }
        }
        File f = CommonDialog.saveFileDialog(
                null,
                T.text("baratinage_file"),
                "bam", "BAM");

        if (f == null) {
            ConsoleLogger.error("saving project failed! Selected file is null.");
            return;
        }
        // currentProject.saveProject();
        saveProject(f.getAbsolutePath());
    }

    public void saveProject(String projectFilePath) {
        currentProject.saveProject(projectFilePath);
        currentProject.setProjectPath(projectFilePath);
        updateFrameTitle();
    }

    private void close() {
        AppConfig.AC.cleanup();
        System.exit(0);
    }
}