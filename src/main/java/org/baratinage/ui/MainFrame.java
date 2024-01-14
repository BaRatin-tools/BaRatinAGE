package org.baratinage.ui;

import javax.swing.JFrame;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.BamProjectLoader;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.perf.TimedActions;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;

public class MainFrame extends JFrame {

    private final RowColPanel projectPanel;
    private BamProject currentProject;

    public final MainMenuBar mainMenuBar;

    public final RowColPanel topPanel;

    public final MainToolbars mainToolBars;

    public final NoProjectPanel noProjectPanel;

    public MainFrame() {
        setIconImage(AppSetup.ICONS.BARATINAGE_LARGE.getImage());
        setTitle(AppSetup.APP_NAME);

        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        Misc.showOnScreen(0, this);

        mainMenuBar = new MainMenuBar();
        setJMenuBar(mainMenuBar);

        projectPanel = new RowColPanel();

        topPanel = new RowColPanel();

        mainToolBars = new MainToolbars();
        topPanel.appendChild(mainToolBars);

        RowColPanel framePanel = new RowColPanel(RowColPanel.AXIS.COL);
        framePanel.setGap(5);
        framePanel.appendChild(topPanel, 0);
        framePanel.appendChild(projectPanel, 1);
        add(framePanel);

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
            public void componentResized(ComponentEvent e) {
                TimedActions.debounce(
                        "rebuild_icons_if_needed",
                        250,
                        AppSetup.ICONS::updateAllIcons);
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

    // FIXME: disabled / currently unused
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

        mainToolBars.updateFileTools(!projectIsNull);

        if (!projectIsNull) {
            projectPanel.appendChild(project);
        } else {
            projectPanel.appendChild(noProjectPanel);
            mainMenuBar.componentMenu.removeAll();
            mainToolBars.clearBamItemTools();
        }
        mainMenuBar.saveProjectAsMenuItem.setEnabled(!projectIsNull);
        mainMenuBar.saveProjectMenuItem.setEnabled(!projectIsNull);
        mainMenuBar.closeProjectMenuItem.setEnabled(!projectIsNull);
        mainMenuBar.updateMenuEnableStates();

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
        setTitle(AppSetup.APP_NAME);
        if (currentProject != null) {
            String projectPath = currentProject.getProjectPath();
            if (projectPath != null) {
                String projectName = Path.of(projectPath).getFileName().toString();
                String unsavedString = currentProject.hasUnsavedChange() ? "*" : "";
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
                    T.text("baratinage_file"),
                    "bam", "BAM");

            if (f == null) {
                ConsoleLogger.error("loading project failed! Selected file is null.");
                return;
            }
            String fullFilePath = f.getAbsolutePath();
            loadProject(fullFilePath);
        }
    }

    public void loadProject(String projectFilePath) {
        if (projectFilePath != null) {
            BamProjectLoader.loadProject(
                    projectFilePath,
                    (bamProject) -> {
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

    public void close() {
        if (currentProject != null) {
            currentProject.checkUnsavedChange();
            if (currentProject.hasUnsavedChange()) {
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