package org.baratinage.ui;

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
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;

import java.awt.Dimension;
import java.awt.Point;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainFrame extends JFrame {

    // public static MainFrame MAIN_FRAME;
    public static AppConfig APP_CONFIG;

    private RowColPanel projectPanel;
    private BamProject currentProject;

    public JMenuBar mainMenuBar;
    public JMenu baratinMenu;

    public MainFrame() {

        APP_CONFIG = new AppConfig(this);

        Lg.init();
        Lg.setLocale("fr");

        this.setSize(new Dimension(1200, 900));

        // this.setSize(new Dimension(1936, 1048));
        // this.setLocation(new Point(2512, -8));

        mainMenuBar = new JMenuBar();

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

        JMenuItem saveProjectMenuItem = new JMenuItem();
        Lg.register(saveProjectMenuItem, "save_project_as");
        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.addActionListener((e) -> {
            if (currentProject != null) {
                currentProject.saveProject();
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

        this.setJMenuBar(mainMenuBar);

        projectPanel = new RowColPanel(RowColPanel.AXIS.COL);
        this.add(projectPanel);

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension dim = MainFrame.this.getSize();
                System.out.println(String.format("Resized: %d x %d", dim.width, dim.height));
            }

            public void componentMoved(ComponentEvent e) {
                Point loc = MainFrame.this.getLocation();
                System.out.println(String.format("Relocated: %d, %d", loc.x, loc.y));
            }
        });

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                MainFrame.this.close();
            }
        });

        this.setVisible(true);
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
                System.out.println("Swtiching language to " + lgKey);
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
    }

    public void newProject() {
        BaratinProject newProject = new BaratinProject();
        newProject.addDefaultItems();
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
            BamProject bamProject = BamProject.loadProject(projectFilePath);
            if (bamProject != null) {
                setCurrentProject(bamProject);
            }
        }
    }

    private void close() {
        APP_CONFIG.cleanup();
        System.exit(0);
    }
}