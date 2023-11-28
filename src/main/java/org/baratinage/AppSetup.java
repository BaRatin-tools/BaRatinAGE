package org.baratinage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.UIManager;

import org.baratinage.translation.T;
import org.baratinage.ui.MainFrame;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.DirUtils;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class AppSetup {

    // setting up all main paths to directories and files
    private static String getAppRootDir() {
        if (IS_PACKAGED) {
            return Path.of(System.getProperty("jpackage.app-path")).getParent().toString();
        } else {
            return Paths.get("").toAbsolutePath().toString();
        }
    }

    public static final boolean IS_PACKAGED = System.getProperty("jpackage.app-path") != null;
    public static final String APP_NAME = "BaRatinAGE";
    public static final String APP_INSTANCE_ID = Misc.getTimeStampedId();
    public static final String PATH_APP_ROOT_DIR = getAppRootDir();
    public static final String PATH_APP_TEMP_DIR = Path
            .of(System.getProperty("java.io.tmpdir"),
                    "baratinage", APP_INSTANCE_ID)
            .toString();

    public static final String PATH_BAM_WORKSPACE_DIR = Path
            .of(PATH_APP_ROOT_DIR, "exe", "bam_workspace", APP_INSTANCE_ID)
            .toString();

    public static final String PATH_ICONS_RESOURCES_DIR = Path.of(PATH_APP_ROOT_DIR, "resources", "icons").toString();
    public static final String PATH_FONTS_RESOURCES_DIR = Path.of(PATH_APP_ROOT_DIR, "resources", "fonts").toString();

    public static final String PATH_CONFIGURATION_FILE = Path.of(PATH_APP_ROOT_DIR, "resources", "config.json")
            .toString();
    public static final String PATH_TRANSLATIONS_FILE = Path
            .of(PATH_APP_ROOT_DIR, "resources", "i18n", "translations.csv").toString();

    // setting up colors and icons (order matters)
    public static final AppColors COLORS = new AppColors();
    public static final AppIcons ICONS = new AppIcons();
    public static final AppConfig CONFIG = new AppConfig();

    // setting up variable
    public static final Font KO_FONT = getKoreanFont();

    public static MainFrame MAIN_FRAME;

    public static void setup() {

        DirUtils.createDir(PATH_APP_TEMP_DIR);
        DirUtils.createDir(PATH_BAM_WORKSPACE_DIR);

        setupLookAndFeel();

        if (CONFIG.LANGUAGE_KEY.get().equals("ko")) {
            setDefaultFont(KO_FONT, CONFIG.FONT_SIZE.get());
        } else {
            setDefaultFont(CONFIG.FONT_SIZE.get());
        }

        T.init();
        SimpleNumberField.init();
        CommonDialog.init();
    }

    public static void clearTempDir() {
        DirUtils.deleteDirContent(PATH_APP_TEMP_DIR);
    }

    public static void cleanup() {
        CONFIG.saveConfiguration();
        DirUtils.deleteDir(PATH_APP_TEMP_DIR);
        DirUtils.deleteDir(PATH_BAM_WORKSPACE_DIR);
    }

    private static void setupLookAndFeel() {
        try {
            try {
                if (CONFIG.THEME_KEY.equals("FlatMacLightLaf")) {
                    UIManager.setLookAndFeel(new FlatMacLightLaf());
                } else if (CONFIG.THEME_KEY.equals("FlatDarkLaf")) {
                    // FIXME: cannot use dark themes because of some custom colors and icons
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                } else if (CONFIG.THEME_KEY.equals("FlatIntelliJLaf")) {
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                } else {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                }
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
    }

    public static Font getKoreanFont() {
        return registerCustomFont("Hahmlet-VariableFont_wght.ttf", 14);
    }

    public static Font registerCustomFont(String fontFileName, int size) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT,
                    new File(PATH_FONTS_RESOURCES_DIR, fontFileName));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            font = font.deriveFont((float) size);
            ge.registerFont(font);
            return font;
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return new JLabel().getFont();
        }
    }

    private static void setDefaultFont(int size) {
        setDefaultFont(null, size);
    }

    private static void setDefaultFont(Font defaultFont, int size) {
        Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
        Object[] keys = keySet.toArray(new Object[keySet.size()]);
        for (Object key : keys) {
            if (key != null && key.toString().toLowerCase().contains("font")) {
                Font font = UIManager.getDefaults().getFont(key);
                if (font != null) {
                    if (defaultFont != null) {
                        font = defaultFont;
                    }
                    font = font.deriveFont((float) size);
                    UIManager.put(key, font);
                }
            }
        }
    }
}
