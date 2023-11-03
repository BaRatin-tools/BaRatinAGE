package org.baratinage;

import javax.swing.UIManager;

import java.awt.EventQueue;
import java.awt.Font;
import java.util.Set;

import org.baratinage.ui.MainFrame;
import org.baratinage.utils.ConsoleLogger;

import com.formdev.flatlaf.FlatLightLaf;

public class App {

    public static void main(String[] args) {

        ConsoleLogger.init();
        ConsoleLogger.log("Starting app...");

        try {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e1) {
                ConsoleLogger.error(e1);
                String sysLookAndFeel = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(sysLookAndFeel);

                System.err.println("Failed to initialize LaF");
            }
        } catch (Exception e2) {
            ConsoleLogger.error(e2);
        }
        setDefaultSize(14);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame mainFrame = new MainFrame();
                    if (args.length > 0) {
                        mainFrame.loadProject(args[0]);
                    } else {
                        // mainFrame.loadProject("C:\\Users\\Ivan\\Documents\\test_48.bam");
                        // mainFrame.newProject();
                    }

                } catch (Exception e3) {
                    ConsoleLogger.error(e3);
                }
            }
        });

    }

    public static void setDefaultSize(int size) {
        // Many thanks to: https://stackoverflow.com/a/26877737
        Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
        Object[] keys = keySet.toArray(new Object[keySet.size()]);
        for (Object key : keys) {
            if (key != null && key.toString().toLowerCase().contains("font")) {
                Font font = UIManager.getDefaults().getFont(key);
                if (font != null) {
                    font = font.deriveFont((float) size);
                    UIManager.put(key, font);
                }
            }
        }
    }

}
