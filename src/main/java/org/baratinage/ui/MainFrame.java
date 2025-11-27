package org.baratinage.ui;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.baratinage.AppSetup;
import org.baratinage.project_importer.BaratinageV2Importer;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.bam.BamProjectSaver;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;

public class MainFrame extends JFrame {

    private final SimpleFlowPanel projectPanel;
    public BamProject currentProject;

    public final MainMenuBar mainMenuBar;

    public final SimpleFlowPanel topPanel;

    public final MainToolbars mainToolBars;

    public final NoProjectPanel noProjectPanel;

    public MainFrame() {
        AppSetup.MAIN_FRAME = this;

        setIconImage(AppSetup.ICONS.BARATINAGE_LARGE.getImage());
        setTitle(AppSetup.APP_NAME);

        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Misc.showOnScreen(0, this);

        mainMenuBar = new MainMenuBar();
        setJMenuBar(mainMenuBar);

        projectPanel = new SimpleFlowPanel();

        topPanel = new SimpleFlowPanel();

        mainToolBars = new MainToolbars();
        topPanel.addChild(mainToolBars, false);

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

        addComponentListener(new ComponentAdapter() {
            private static void updateIcons() {
                TimedActions.debounce(
                        "rebuild_icons_if_needed",
                        AppSetup.CONFIG.DEBOUNCED_DELAY_MS,
                        () -> {
                            if (!SvgIcon.scalesHaveChanged()) {
                                return;
                            }
                            AppSetup.ICONS.updateAllIcons();
                            SvgIcon.memorizeCurrentScales();
                        });
            }

            public void componentResized(ComponentEvent e) {
                updateIcons();
            }

            public void componentMoved(ComponentEvent e) {
                updateIcons();
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

        noProjectPanel = new NoProjectPanel();

        setAppWideShortcuts();

        setVisible(true);
        setCurrentProject(null);

        T.updateHierarchy(this, mainMenuBar);
        T.updateHierarchy(this, noProjectPanel);
        T.updateHierarchy(this, mainToolBars);
    }

    public void updateUI() {
        projectPanel.updateUI();
        if (currentProject != null) {
            currentProject.updateUI();
        }
        mainMenuBar.updateUI();
    }

    private void setAppWideShortcuts() {
        JRootPane rootPane = getRootPane();
        KeyStroke aKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(aKeyStroke, "toggle_debug_menu");
        rootPane.getActionMap().put("toggle_debug_menu", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenuBar.toggleDebugMenu();
            }

        });
    }

    public void setCurrentProject(BamProject project) {
        if (currentProject != null) {
            T.clear(currentProject);
        }
        currentProject = project;
        projectPanel.removeAll();

        boolean projectIsNull = project == null;

        mainToolBars.updateFileTools(!projectIsNull);

        if (!projectIsNull) {
            projectPanel.addChild(project, true);
        } else {
            projectPanel.addChild(noProjectPanel, true);
            mainMenuBar.componentMenu.removeAll();
            mainToolBars.clearBamItemTools();
        }
        mainMenuBar.saveProjectAsMenuItem.setEnabled(!projectIsNull);
        mainMenuBar.saveProjectMenuItem.setEnabled(!projectIsNull);
        mainMenuBar.closeProjectMenuItem.setEnabled(!projectIsNull);
        mainMenuBar.updateMenuEnableStates();

        updateFrameTitle();
        T.updateTranslations();

        SimpleFlowPanel framePanel = new SimpleFlowPanel(true);
        framePanel.setGap(5);
        if (!projectIsNull) {
            framePanel.addChild(topPanel, false);
        }
        framePanel.addChild(projectPanel, true);
        setContentPane(framePanel);
    }

    public boolean confirmLoosingUnsavedChanges() {
        if (currentProject != null) {
            if (currentProject.checkUnsavedChange()) {
                return CommonDialog.confirmDialog(
                        T.text("unsaved_changes_will_be_lost") + "\n" +
                                T.text("proceed_question"),
                        T.text("are_you_sure"));
            }
        }
        return true;
    }

    public boolean closeProject() {
        boolean confirmed = confirmLoosingUnsavedChanges();
        if (confirmed) {
            setCurrentProject(null);
        }
        return confirmed;
    }

    public void updateFrameTitle() {
        setTitle(AppSetup.APP_NAME);
        if (currentProject != null) {
            String projectPath = currentProject.getProjectPath();
            if (projectPath != null) {
                String projectName = Path.of(projectPath).getFileName().toString();
                String unsavedString = currentProject.checkUnsavedChange() ? "*" : "";
                setTitle(AppSetup.APP_NAME + " - " + projectName + " - " + projectPath + unsavedString);
            }
        }
    }

    public void newProject() {
        if (confirmLoosingUnsavedChanges()) {
            AppSetup.clearTempDir();
            BaratinProject newProject = new BaratinProject();
            newProject.addDefaultBamItems();
            setCurrentProject(newProject);
        }
    }

    public void loadProject() {
        if (confirmLoosingUnsavedChanges()) {
            File f = CommonDialog.openFileDialog(
                    null,
                    new CommonDialog.CustomFileFilter(
                            T.text("baratinage_file"),
                            "bam", "BAM"));

            if (f == null) {
                ConsoleLogger.error("loading project failed! Selected file is null.");
                return;
            }
            String fullFilePath = f.getAbsolutePath();
            loadProject(fullFilePath);
        }
    }

    public void loadProject(String projectFilePath) {
        ConsoleLogger.log(String.format("Opening file '%s' ...", projectFilePath));
        if (projectFilePath != null) {
            BamProjectLoader.loadProject(
                    projectFilePath,
                    (bamProject) -> {
                        bamProject.setProjectPath(projectFilePath);
                        bamProject.setLastSavedConfig();
                        setCurrentProject(bamProject);
                    },
                    () -> {
                        CommonDialog.errorDialog(T.text("error_opening_project"));
                    }, () -> {
                        ConsoleLogger.log("Project load canceled");
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
                "",
                null,
                new CommonDialog.CustomFileFilter(T.text("baratinage_file"),
                        "bam", "BAM"));

        if (f == null) {
            ConsoleLogger.error("saving project failed! Selected file is null.");
            return;
        }
        saveProject(f.getAbsolutePath());
    }

    public void saveProject(String projectFilePath) {

        BamProjectSaver.saveProject(currentProject, projectFilePath, (bamConfig) -> {
            currentProject.setLastSavedConfig();
            currentProject.setProjectPath(projectFilePath);
            updateFrameTitle();
        }, () -> {
            CommonDialog.errorDialog(T.text("error_saving_project"));
        }, () -> {
            ConsoleLogger.warn("Project saving canceled");
        });
    }

    public void importV2Project() {
        if (confirmLoosingUnsavedChanges()) {
            File f = CommonDialog.openFileDialog(T.text("import_baratinage_v2_project"),
                    new CommonDialog.CustomFileFilter(
                            T.text("bar_zip_file_format"),
                            "bar.zip", "BAR.ZIP"));
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
        }
    }

    public void close() {
        if (currentProject != null) {
            if (currentProject.checkUnsavedChange()) {
                String text = String.format("<html>%s<br>%s</html>",
                        T.text("confirm_closing_app"),
                        T.text("unsaved_changes_will_be_lost"));
                if (!CommonDialog.confirmDialog(text, T.text("are_you_sure"))) {
                    return;
                }
            }
        }
        System.exit(0);
    }
}