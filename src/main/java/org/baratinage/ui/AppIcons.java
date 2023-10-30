package org.baratinage.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.baratinage.ui.component.SvgIcon;
import org.baratinage.utils.perf.TimedActions;

public class AppIcons {

    private List<SvgIcon> allIcons = new ArrayList<>();

    public float ICON_SIZE;
    private float FEATHER_ICON_SIZE;

    public SvgIcon BARATINAGE_ICON_LARGE;
    public SvgIcon COPY_ICON;
    public SvgIcon TRASH_ICON;
    public SvgIcon LOCK_ICON;
    public SvgIcon SAVE_ICON;
    public SvgIcon EXTERNAL_ICON;

    public SvgIcon LEFT_DOWN_ARROW_ICON;
    public SvgIcon RIGHT_DOWN_ARROW_ICON;
    public SvgIcon LEFT_UP_ARROW_ICON;
    public SvgIcon RIGHT_UP_ARROW_ICON;

    private HashMap<String, SvgIcon> otherIcons = new HashMap<>();

    public AppIcons() {

        BARATINAGE_ICON_LARGE = addIcon(new SvgIcon(
                Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "icon.svg").toString(),
                64, 64));

        ICON_SIZE = 28;
        FEATHER_ICON_SIZE = 20;

        COPY_ICON = addIcon(buildFeatherAppImageIcon("copy.svg", FEATHER_ICON_SIZE));
        TRASH_ICON = addIcon(buildFeatherAppImageIcon("trash.svg", FEATHER_ICON_SIZE));
        LOCK_ICON = addIcon(buildFeatherAppImageIcon("lock.svg", FEATHER_ICON_SIZE));
        SAVE_ICON = addIcon(buildFeatherAppImageIcon("save.svg", FEATHER_ICON_SIZE));
        EXTERNAL_ICON = addIcon(buildFeatherAppImageIcon("external-link.svg", FEATHER_ICON_SIZE));

        LEFT_DOWN_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-left-down.svg", FEATHER_ICON_SIZE));
        RIGHT_DOWN_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-right-down.svg", FEATHER_ICON_SIZE));
        LEFT_UP_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-left-up.svg", FEATHER_ICON_SIZE));
        RIGHT_UP_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-right-up.svg", FEATHER_ICON_SIZE));

        // every 10 seconds, check if scales have change, and rebuild icons if necessary
        TimedActions.interval(
                "app_icons_rebuild_if_necessary",
                10000,
                this::updateAllIcons);

    }

    public SvgIcon getCustomAppImageIcon(String fileName) {
        if (otherIcons.containsKey(fileName)) {
            return otherIcons.get(fileName);
        } else {
            SvgIcon icon = buildCustomAppImageIcon(fileName, ICON_SIZE);
            addIcon(icon);
            otherIcons.put(fileName, icon);
            return icon;
        }
    }

    private SvgIcon addIcon(SvgIcon icon) {
        allIcons.add(icon);
        return icon;
    }

    static private SvgIcon buildCustomAppImageIcon(String name, float size) {
        return new SvgIcon(
                Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "custom", name).toString(),
                size, size);
    }

    static private SvgIcon buildFeatherAppImageIcon(String name, float size) {
        return new SvgIcon(
                Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "feather", name).toString(),
                size, size);
    }

    public void updateAllIcons() {
        if (!SvgIcon.scalesHaveChanged()) {
            return;
        }
        for (SvgIcon icon : allIcons) {
            icon.rebuildIcon();
        }
        SwingUtilities.updateComponentTreeUI(AppConfig.AC.APP_MAIN_FRAME);
    }
}
