package org.baratinage.ui;

import java.awt.Color;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.baratinage.translation.T;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class NoProjectPanel extends GridPanel {
    public final JLabel mainLabel;
    public final JLabel welcomeLabel;
    public final JButton createProjectButton;
    public final JButton openProjectButton;
    public final RowColPanel buttonsPanel;

    public NoProjectPanel() {
        // super(AXIS.COL, ALIGN.START);
        setPadding(5, 5, 50, 5);
        setGap(10);

        mainLabel = new JLabel();
        mainLabel.setIcon(AppConfig.AC.ICONS.BARATINAGE_ICON_LARGE);

        welcomeLabel = new JLabel();
        T.t(AppConfig.AC.APP_MAIN_FRAME, welcomeLabel, true, "welcome_to_baratinage");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(32f));

        createProjectButton = new JButton();
        T.t(AppConfig.AC.APP_MAIN_FRAME, createProjectButton, false, "create_baratin_project");
        createProjectButton.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.newProject();
        });

        openProjectButton = new JButton();
        T.t(AppConfig.AC.APP_MAIN_FRAME, openProjectButton, false, "open_project");
        openProjectButton.addActionListener((e) -> {
            AppConfig.AC.APP_MAIN_FRAME.loadProject();
        });

        buttonsPanel = new RowColPanel(RowColPanel.AXIS.COL);
        buttonsPanel.setGap(5);
        buttonsPanel.appendChild(createProjectButton);
        buttonsPanel.appendChild(openProjectButton);

        insertChild(mainLabel, 0, 0, ANCHOR.C, FILL.NONE);
        insertChild(welcomeLabel, 0, 1, ANCHOR.C, FILL.NONE);
        insertChild(buttonsPanel, 0, 2, ANCHOR.C, FILL.NONE);

    }
}
