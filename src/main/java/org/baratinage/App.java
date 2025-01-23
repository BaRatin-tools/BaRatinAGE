package org.baratinage;

import java.awt.EventQueue;

import org.baratinage.ui.MainFrame;
import org.baratinage.utils.ConsoleLogger;

public class App {

    public static void main(String[] args) {

        ConsoleLogger.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConsoleLogger.log("Cleaning up...");
            AppSetup.cleanup();
        }));

        new App(args);
    }

    public static void restart() {
        String projectPath = null;
        if (AppSetup.MAIN_FRAME != null) {
            if (AppSetup.MAIN_FRAME.currentProject != null) {
                projectPath = AppSetup.MAIN_FRAME.currentProject.getProjectPath();
                if (!AppSetup.MAIN_FRAME.closeProject()) {
                    return;
                }
            }
            AppSetup.MAIN_FRAME.dispose();
        }
        AppSetup.cleanup();
        if (projectPath != null) {
            new App(projectPath);
        } else {
            new App();
        }
    }

    private App(String... args) {

        ConsoleLogger.log("Starting app...");

        AppSetup.setup();

        EventQueue.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                if (args.length > 0) {
                    mainFrame.loadProject(args[0]);
                }
            } catch (Exception e) {
                ConsoleLogger.error(e);
            }
        });
    }

}
