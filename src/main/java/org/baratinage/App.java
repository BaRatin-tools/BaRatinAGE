package org.baratinage;

import java.awt.EventQueue;

import org.baratinage.ui.MainFrame;
import org.baratinage.utils.ConsoleLogger;

public class App {

    public static void main(String[] args) {

        ConsoleLogger.init();
        ConsoleLogger.log("Starting app...");

        AppSetup.setup();

        EventQueue.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                AppSetup.MAIN_FRAME = mainFrame;
                if (args.length > 0) {
                    mainFrame.loadProject(args[0]);
                }
            } catch (Exception e) {
                ConsoleLogger.error(e);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConsoleLogger.log("Cleaning up...");
            AppSetup.cleanup();
        }));

    }

}
