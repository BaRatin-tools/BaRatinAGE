package org.baratinage;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
// import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

import org.baratinage.ui.MainFrame;
import org.baratinage.ui.lg.Lg;
import org.baratinage.utils.Misc;

public class App {

    // FIXME: should have subdirectory for each instance of BaRatinAGE to avoid
    // conflit if multiple instances are run in parallel. They could be named using
    // a time stamp which would in turn be used to clean up the temp directory after
    // some time has passed.
    public final static String TIME_STAMP = Misc.getTimeStampedId();
    public final static String TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"),
            "baratinage", TIME_STAMP).toString();
    // public static String LAST_USED_DIR = System.getProperty("user.home");
    public static String LAST_USED_DIR = "test";
    // FIXME: can't use time stamp because of relative path management in jbam
    public final static String BAM_WORKSPACE = Path.of("exe", "bam_workspace").toString();

    public static MainFrame MAIN_FRAME;

    public static Color INVALID_COLOR = new Color(200, 50, 40);

    public static void main(String[] args) {

        // TODO: implement opening .bam file from OS file explorer
        for (String arg : args) {
            System.out.println(arg);
        }

        System.out.println("App.TIME_STAMP = " + App.TIME_STAMP);
        System.out.println("App.TEMP_DIR = " + App.TEMP_DIR);
        System.out.println("App.BAM_WORKSPACE = " + App.BAM_WORKSPACE);
        System.out.println("App.LAST_USED_DIR = " + App.LAST_USED_DIR);

        Misc.createDir(App.TEMP_DIR);
        Misc.createDir(App.BAM_WORKSPACE);

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
                    Lg.init();
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

    public static void cleanup() {
        Misc.deleteDir(BAM_WORKSPACE);
    }

}
