package org.baratinage.ui;

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.baratinage.utils.Misc;

public class AppConfig {
    public final String APP_ROOT_DIR;
    public final String APP_INSTANCE_ID;
    public final String APP_TEMP_DIR;
    public final String BAM_WORKSPACE_ROOT;
    public final String I18N_RESOURCES_DIR;
    public final String ICONS_RESOURCES_DIR;
    public final MainFrame APP_MAIN_FRAME;

    public final Color INVALID_COLOR = new Color(200, 50, 40);

    public String lastUsedDir;

    public AppConfig(MainFrame mainFrame) {

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

        init();
    }

    public void init() {
        Misc.createDir(APP_TEMP_DIR);
        Misc.createDir(BAM_WORKSPACE_ROOT);

        lastUsedDir = System.getProperty("user.home");
    }

    public void cleanup() {
        Misc.deleteDir(BAM_WORKSPACE_ROOT);
        Misc.deleteDir(APP_TEMP_DIR);
    }
}
