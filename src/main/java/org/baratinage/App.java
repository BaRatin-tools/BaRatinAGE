package org.baratinage;

import javax.swing.UIManager;

import java.awt.EventQueue;
import java.awt.Font;
import java.util.Set;

import org.baratinage.ui.MainFrame;

public class App {

    public static void main(String[] args) {
        try {
            String sysLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(sysLookAndFeel);
            setDefaultSize(14);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame mainFrame = new MainFrame();
                    if (args.length > 0) {
                        mainFrame.loadProject(args[0]);
                    } else {
                        mainFrame.loadProject("C:\\Users\\Ivan\\Documents\\test_31.bam");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
