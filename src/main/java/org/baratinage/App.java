package org.baratinage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import java.awt.EventQueue;
import java.awt.Font;
import java.net.URL;
import java.util.Set;

import org.baratinage.ui.MainFrame;

public class App {
    public static void main(String[] args) {

        try {
            String sysLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            // System.out.println(sysLookAndFeel);
            UIManager.setLookAndFeel(sysLookAndFeel);

            setDefaultSize(14);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    JFrame maineFrame = new MainFrame();

                    maineFrame.setTitle("BaRatinAGE V3");

                    // FIXME: where should such resource files be stored?
                    // here it cannot be accessed after packaging wherease
                    // in the approach followed for i18n (and icons) enables modifying
                    // the resource files after packaging...
                    URL iconUrl = this.getClass().getResource("/icon/64x64.png");
                    ImageIcon iconImg = new ImageIcon(iconUrl);
                    maineFrame.setIconImage(iconImg.getImage());

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
                // System.out.println(key);
                Font font = UIManager.getDefaults().getFont(key);
                if (font != null) {
                    font = font.deriveFont((float) size);
                    UIManager.put(key, font);
                }
            }
        }
    }

}
