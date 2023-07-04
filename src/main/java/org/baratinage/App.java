package org.baratinage;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
// import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

import org.baratinage.ui.MainFrame;
import org.baratinage.ui.lg.Lg;

public class App {

    public final static String TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"),
            "baratinage").toString();

    public final static String BAM_RUN_DIR = "test/newTestWS";

    public final static String TEST_DIR = "test";

    public static MainFrame MAIN_FRAME;

    public static Color INVALID_COLOR = new Color(255, 125, 125, 200);

    public static void main(String[] args) {

        // TODO: implement opening .bam file from OS file explorer
        for (String arg : args) {
            System.out.println(arg);
        }

        System.out.println("App.TEMP_DIR = " + App.TEMP_DIR);
        File tD = new File(App.TEMP_DIR);
        boolean created = tD.mkdirs();
        System.out.println("Temp dir created? " + (created ? "Yes" : "No"));

        try {
            String sysLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(sysLookAndFeel);
            // String crossPlatformLookAndFeel =
            // UIManager.getCrossPlatformLookAndFeelClassName();
            // UIManager.setLookAndFeel(crossPlatformLookAndFeel);
            // for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            // System.out.println(info);
            // }
            setDefaultSize(14);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Lg.setLocale("fr");
                    MAIN_FRAME = new MainFrame();

                    MAIN_FRAME.setTitle("BaRatinAGE V3");

                    // FIXME: where should such resource files be stored?
                    // here it cannot be accessed after packaging wherease
                    // in the approach followed for i18n (and icons) enables modifying
                    // the resource files after packaging...
                    URL iconUrl = this.getClass().getResource("/icon/64x64.png");
                    ImageIcon iconImg = new ImageIcon(iconUrl);
                    MAIN_FRAME.setIconImage(iconImg.getImage());

                    // MAIN_FRAME.setCurrentProject(new BaratinProject());

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
