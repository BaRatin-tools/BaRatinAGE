package org.baratinage;

import javax.swing.UIManager;

import java.awt.Color;
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
                // FIXME: cannot use dark themes because of some custom colors
                // such as invalid background color... see AppConfig class
                // UIManager.setLookAndFeel(new FlatDarkLaf());
                // UIManager.setLookAndFeel(new FlatIntelliJLaf());
                // UIManager.setLookAndFeel(new FlatMacLightLaf());
                UIManager.setLookAndFeel(new FlatLightLaf());
                UIManager.put("SplitPane.background", new Color(247, 247, 247));
                UIManager.put("SplitPaneDivider.gripColor", new Color(211, 211, 211));
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
                        mainFrame.loadProject("D:\\Programming\\java\\V3\\BaRatinAGE\\test\\test_bam.bam");
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
