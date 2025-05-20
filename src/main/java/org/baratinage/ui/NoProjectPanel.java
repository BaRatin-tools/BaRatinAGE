package org.baratinage.ui;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.SimpleFlowPanel;

public class NoProjectPanel extends GridPanel {
    public final JLabel mainLabel;
    public final JLabel welcomeLabel;
    public final JButton createProjectButton;
    public final JButton openProjectButton;
    public final JButton importV2ProjectButton;
    public final SimpleFlowPanel buttonsPanel;

    public NoProjectPanel() {
        setPadding(5, 5, 100, 5);
        setGap(10);

        mainLabel = new JLabel();
        mainLabel.setIcon(AppSetup.ICONS.BARATINAGE_LARGE);

        welcomeLabel = new JLabel();
        T.t(AppSetup.MAIN_FRAME, welcomeLabel, true, "welcome_to_baratinage");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(32f));

        createProjectButton = new JButton();
        T.t(AppSetup.MAIN_FRAME, createProjectButton, false, "create_baratin_project");
        createProjectButton.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.newProject();
        });

        openProjectButton = new JButton();
        T.t(AppSetup.MAIN_FRAME, openProjectButton, false, "open_project");
        openProjectButton.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.loadProject();
        });

        importV2ProjectButton = new JButton();
        T.t(AppSetup.MAIN_FRAME, importV2ProjectButton, false, "import_baratinage_v2_project");
        importV2ProjectButton.addActionListener((e) -> {
            AppSetup.MAIN_FRAME.importV2Project();
        });

        buttonsPanel = new SimpleFlowPanel(true);
        buttonsPanel.setGap(5);
        buttonsPanel.addChild(createProjectButton, false);
        buttonsPanel.addChild(openProjectButton, false);
        buttonsPanel.addChild(importV2ProjectButton, false);

        insertChild(mainLabel, 0, 0, ANCHOR.C, FILL.NONE);
        insertChild(welcomeLabel, 0, 1, ANCHOR.C, FILL.NONE);
        insertChild(buttonsPanel, 0, 2, ANCHOR.C, FILL.NONE);

    }
}
