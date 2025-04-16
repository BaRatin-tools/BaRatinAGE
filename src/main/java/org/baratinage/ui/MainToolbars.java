package org.baratinage.ui;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.RowColPanel;

public class MainToolbars extends RowColPanel {

    private final JToolBar fileTools;
    private final JToolBar bamItemTools;

    private final JButton newProjectButton;
    private final JButton openProjectButton;
    // private final JButton saveProjectAsButton;
    private final JButton saveProjectButton;
    private final JButton closeProjectButton;

    public MainToolbars() {
        super(AXIS.ROW, ALIGN.START);

        fileTools = new JToolBar();
        bamItemTools = new JToolBar();

        appendChild(fileTools, 0);
        appendChild(new SimpleSep(true), 0);
        appendChild(bamItemTools, 1);

        newProjectButton = new JButton();
        newProjectButton.setIcon(AppSetup.ICONS.NEW_FILE);
        newProjectButton.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.newProject();
        });
        fileTools.add(newProjectButton);

        openProjectButton = new JButton();
        openProjectButton.setIcon(AppSetup.ICONS.FOLDER);
        openProjectButton.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.loadProject();
        });
        fileTools.add(openProjectButton);

        // JButton saveProjectAsButton = new JButton();
        // saveProjectAsButton.setIcon(AppSetup.ICONS.SAVE);
        // saveProjectAsButton.addActionListener((e) -> {
        // AppSetup.MAIN_FRAME.saveProject(true);
        // });
        // fileTools.add(saveProjectAsButton);

        saveProjectButton = new JButton();
        saveProjectButton.setIcon(AppSetup.ICONS.SAVE);
        saveProjectButton.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.saveProject(false);
        });
        fileTools.add(saveProjectButton);

        closeProjectButton = new JButton();
        closeProjectButton.setIcon(AppSetup.ICONS.CLOSE);
        closeProjectButton.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.closeProject();
        });
        fileTools.add(closeProjectButton);

        T.t(this, () -> {
            newProjectButton.setToolTipText(T.text("create_baratin_project"));
            openProjectButton.setToolTipText(T.text("open_project"));
            // saveProjectAsButton.setToolTipText(T.text("save_project_as"));
            saveProjectButton.setToolTipText(T.text("save_project"));
            closeProjectButton.setToolTipText(T.text("close_project"));
        });

        JButton btn = new JButton();
        btn.setIcon(AppSetup.ICONS.CLOSE);
        btn.setText("test");
        bamItemTools.add(btn);

    }

    public void updateFileTools(boolean isProjectOpen) {
        fileTools.setVisible(isProjectOpen);
        // FIXME: bug detected with icon sizing when using a disabled button.
        // saveProjectButton.setEnabled(isProjectOpen);
        // closeProjectButton.setEnabled(isProjectOpen);
    }

    public void addBamItemTool(JButton tool) {
        bamItemTools.add(tool);
    }

    public void clearBamItemTools() {
        bamItemTools.removeAll();
    }

}
