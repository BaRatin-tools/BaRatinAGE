package org.baratinage.ui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.Misc;

import java.awt.Dimension;
import java.awt.Point;
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

        DebugMenu debugMenu = new DebugMenu();
        mainMenuBar.add(debugMenu);

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

        JMenuItem saveProjectAsMenuItem = new JMenuItem();
        T.t(this, saveProjectAsMenuItem, false, "save_project_as");
        saveProjectAsMenuItem
                .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
        saveProjectAsMenuItem.addActionListener((e) -> {
            saveProject(true);
        });
        fileMenu.add(saveProjectAsMenuItem);

        JMenuItem saveProjectMenuItem = new JMenuItem();
        T.t(this, saveProjectMenuItem, false, "save_project");
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.addActionListener((e) -> {
            saveProject(false);
        });
        fileMenu.add(saveProjectMenuItem);

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
                Dimension dim = MainFrame.this.getSize();
                System.out.println(String.format("MainFrame: Resized: %d x %d", dim.width, dim.height));
            }

            public void componentMoved(ComponentEvent e) {
                Point loc = MainFrame.this.getLocation();
                System.out.println(String.format("MainFrame: Relocated: %d, %d", loc.x, loc.y));
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
                System.out.println("MainFrame: maximized window");
            } else if (MainFrame.this.getExtendedState() == JFrame.NORMAL) {
                System.out.println("MainFrame: normal window");
            } else if (MainFrame.this.getExtendedState() == JFrame.ICONIFIED) {
                System.out.println("MainFrame: iconified window");
            } else {
                if (MainFrame.this.getExtendedState() == 7) {
                    System.out.println("MainFrame: iconified window?");
                } else {
                    System.out.println("MainFrame: iconified/unknown window?");
                }
            }
        });

        setVisible(true);
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
                System.out.println("MainFrame: swtiching language to '" + tKey + "'");
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

    public void setCurrentProject(BamProject project) {
        if (currentProject != null) {
            // currentProject.BAM_ITEMS.forEach(item -> {
            // T.clear(item);
            // });
            T.clear(currentProject);
        }
        currentProject = project;
        projectPanel.clear();
        projectPanel.appendChild(project);
        updateFrameTitle();
        // Lg.printInfo();
        T.updateTranslations();
    }

    public void updateFrameTitle() {
        setTitle(AppConfig.AC.APP_NAME);
        if (currentProject != null) {
            String projectPath = currentProject.getProjectPath();
            if (projectPath != null) {
                String projectName = Path.of(projectPath).getFileName().toString();
                setTitle(AppConfig.AC.APP_NAME + " - " + projectName + " - " + projectPath);
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
            System.err.println("MainFrame Error: loading project failed! Selected file is null.");
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
            });
        }
    }

    public void saveProject(boolean saveAs) {

        if (currentProject == null) {
            System.err.println("MainFrame Error: no project to save.");
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
            System.err.println("MainFrame Error: saving project failed! Selected file is null.");
            return;
        }
        // currentProject.saveProject();
        saveProject(f.getAbsolutePath());
    }

    public void saveProject(String projectFilePath) {
        currentProject.saveProject(projectFilePath);
        updateFrameTitle();
    }

    private void close() {
        AppConfig.AC.cleanup();
        System.exit(0);
    }
}