package org.baratinage.ui;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.baratinage.utils.Misc;

public class AppConfig {
    public static AppConfig AC;

    public final String APP_NAME;
    public final String APP_ROOT_DIR;
    public final String APP_INSTANCE_ID;
    public final String APP_TEMP_DIR;
    public final String BAM_WORKSPACE_ROOT;
    public final String I18N_RESOURCES_DIR;
    public final String ICONS_RESOURCES_DIR;
    public final MainFrame APP_MAIN_FRAME;

    public final int THROTTLED_DELAY_MS = 250;

    public final String DEFAULT_RESSOURCE_FILE_KEY = "ui";
    public final String DEFAULT_RESSOURCE_FILE_LOCALE_KEY = "en";

    public Color INVALID_DISABLED_COLOR_BG = new Color(245, 230, 230);
    public Color INVALID_COLOR_BG = new Color(245, 210, 210);
    public Color INVALID_COLOR_FG = new Color(200, 50, 40);
    public Color DANGER_COLOR = INVALID_COLOR_FG;
    public Color WARNING_COLOR = new Color(230, 149, 0);
    public Color INFO_COLOR = new Color(0, 63, 179);

    public Color PLOT_LINE_COLOR = new Color(50, 50, 100);
    public Color PLOT_ENVELOP_COLOR = new Color(200, 200, 255);

    public Color PRIOR_LINE_COLOR = new Color(50, 50, 255);
    public Color PRIOR_ENVELOP_COLOR = new Color(200, 200, 255);

    public Color POSTERIOR_LINE_COLOR = new Color(253, 55, 50);
    public Color POSTERIOR_ENVELOP_COLOR = new Color(253, 195, 188);

    public Color RATING_CURVE_COLOR = PLOT_LINE_COLOR;
    public Color RATING_CURVE_TOTAL_UNCERTAINTY_COLOR = new Color(182, 0, 4);
    public Color RATING_CURVE_PARAM_UNCERTAINTY_COLOR = new Color(253, 195, 188);
    public Color GAUGING_COLOR = new Color(0, 128, 255);
    public Color DISCARDED_GAUGING_COLOR = new Color(255, 80, 83);
    public Color STAGE_TRANSITION_VALUE_COLOR = new Color(4, 182, 0);
    public Color STAGE_TRANSITION_UNCERTAINTY_COLOR = new Color(164, 255, 162);

    public String lastUsedDir;

    public final AppIcons ICONS;

    public AppConfig(MainFrame mainFrame) {

        AC = this;

        APP_NAME = "BaRatinAGE";

        String exePath = System.getProperty("jpackage.app-path");
        if (exePath == null) {
            APP_ROOT_DIR = Paths.get("").toAbsolutePath().toString();
        } else {
            APP_ROOT_DIR = Path.of(exePath).getParent().toString();
        }

        APP_INSTANCE_ID = Misc.getTimeStampedId();

        APP_TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"), "baratinage", APP_INSTANCE_ID).toString();

        BAM_WORKSPACE_ROOT = Path.of(APP_ROOT_DIR, "exe", "bam_workspace", APP_INSTANCE_ID).toString();

        I18N_RESOURCES_DIR = Path.of(APP_ROOT_DIR, "resources", "i18n").toString();
        ICONS_RESOURCES_DIR = Path.of(APP_ROOT_DIR, "resources", "icons").toString();

        APP_MAIN_FRAME = mainFrame;

        ICONS = new AppIcons();

        init();
    }

    public void init() {
        Misc.createDir(APP_TEMP_DIR);
        Misc.createDir(BAM_WORKSPACE_ROOT);

        lastUsedDir = System.getProperty("user.home");
    }

    public void clearTempDirectory() {
        // Clear Temp Directory!
        for (File file : new File(APP_TEMP_DIR).listFiles()) {
            if (!file.isDirectory())
                file.delete();
        }
    }

    public void cleanup() {
        Misc.deleteDir(BAM_WORKSPACE_ROOT);
        Misc.deleteDir(APP_TEMP_DIR);
    }
}
