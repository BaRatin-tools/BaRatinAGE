package org.baratinage.ui;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
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

        Lg.init();
        Lg.setLocale("fr");
        Lg.setDefaultOwnerKey("main_frame");

        ImageIcon baratinageIcon = new SvgIcon(Path.of(
                AppConfig.AC.ICONS_RESOURCES_DIR,
                "icon.svg").toString(), 64, 64);

        setIconImage(baratinageIcon.getImage());
        setTitle(AppConfig.AC.APP_NAME);

        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Misc.showOnScreen(1, this);

        mainMenuBar = new JMenuBar();

        JMenu debugMenu = new JMenu("DEBUG / DEV");
        mainMenuBar.add(debugMenu);
        JMenuItem gcBtn = new JMenuItem("Garbage collection");
        debugMenu.add(gcBtn);
        gcBtn.addActionListener((e) -> {
            System.gc();
        });
        JMenuItem lgResetBtn = new JMenuItem("Reload Lg ressources");
        debugMenu.add(lgResetBtn);
        lgResetBtn.addActionListener((e) -> {
            Lg.reloadResources();
        });

        JMenu fileMenu = new JMenu();
        Lg.register(fileMenu, "files");
        mainMenuBar.add(fileMenu);

        JMenuItem newProjectMenuItem = new JMenuItem();
        Lg.register(newProjectMenuItem, "create_baratin_project");
        newProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        newProjectMenuItem.addActionListener((e) -> {
            newProject();
        });
        fileMenu.add(newProjectMenuItem);

        JMenuItem openProjectMenuItem = new JMenuItem();
        Lg.register(openProjectMenuItem, "open_project");
        openProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        openProjectMenuItem.addActionListener((e) -> {
            loadProject();
        });
        fileMenu.add(openProjectMenuItem);

        JMenuItem saveProjectAsMenuItem = new JMenuItem();
        Lg.register(saveProjectAsMenuItem, "save_project_as");
        saveProjectAsMenuItem
                .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK + ActionEvent.ALT_MASK));
        saveProjectAsMenuItem.addActionListener((e) -> {
            if (currentProject != null) {
                currentProject.saveProjectAs();
                updateFrameTitle();
            }
        });
        fileMenu.add(saveProjectAsMenuItem);

        JMenuItem saveProjectMenuItem = new JMenuItem();
        Lg.register(saveProjectMenuItem, "save_project");
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.addActionListener((e) -> {
            if (currentProject != null) {
                currentProject.saveProject();
                updateFrameTitle();
            }
        });
        fileMenu.add(saveProjectMenuItem);

        fileMenu.addSeparator();

        JMenuItem closeMenuItem = new JMenuItem();
        Lg.register(closeMenuItem, "exit");
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

    Map<String, JCheckBoxMenuItem> lgMenuItems = new HashMap<>();

    public JMenu createLanguageSwitcherMenu() {

        JMenu switchLanguageMenuItem = new JMenu();
        Lg.register(switchLanguageMenuItem, "change_language");

        List<String> lgKeys = Lg.getAvailableLocales();
        for (String lgKey : lgKeys) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem();
            lgMenuItems.put(lgKey, item);
            Lg.register(item, () -> {
                Locale currentLocale = Lg.getLocale();
                Locale targetLocale = Locale.forLanguageTag(lgKey);
                String currentLocaleText = targetLocale.getDisplayName(targetLocale);
                String targetLocaleText = targetLocale.getDisplayName(currentLocale);
                item.setText(currentLocaleText + " - " + targetLocaleText);
            });
            item.addActionListener((e) -> {
                System.out.println("MainFrame: swtiching language to '" + lgKey + "'");
                Lg.setLocale(lgKey);
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
        String currentLocalKey = Lg.getLocaleKey();
        for (String key : lgMenuItems.keySet()) {
            lgMenuItems.get(key).setSelected(key.equals(currentLocalKey));
        }
    }

    public void setCurrentProject(BamProject project) {
        currentProject = project;
        projectPanel.clear();
        projectPanel.appendChild(project);
        updateFrameTitle();
        // Lg.printInfo();
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

    public void resetLg() {
        String previousProjectOwnerKey = Lg.getDefaultOwnerKey();
        Lg.setDefaultOwnerKey(Misc.getTimeStampedId());
        if (!previousProjectOwnerKey.equals("main_frame")) {
            Lg.unregisterOwner(previousProjectOwnerKey);
        }
    }

    public void newProject() {
        resetLg();
        BaratinProject newProject = new BaratinProject();
        newProject.addDefaultBamItems();
        setCurrentProject(newProject);
    }

    public void loadProject() {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (f.getName().endsWith(".bam")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return Lg.text("baratinage_file");
            }

        });
        fileChooser.setDialogTitle(Lg.text("open_project"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fullFilePath = fileChooser.getSelectedFile().getAbsolutePath();
            loadProject(fullFilePath);
        }
    }

    public void loadProject(String projectFilePath) {
        if (projectFilePath != null) {
            resetLg();
            BamProject bamProject = BamProject.loadProject(projectFilePath);
            bamProject.setProjectPath(projectFilePath);
            setCurrentProject(bamProject);
            updateUI();
        }
    }

    private void close() {
        AppConfig.AC.cleanup();
        System.exit(0);
    }
}