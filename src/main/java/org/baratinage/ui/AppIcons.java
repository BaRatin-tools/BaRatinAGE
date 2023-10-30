package org.baratinage.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.baratinage.ui.component.SvgIcon;

public class AppIcons {

    private List<SvgIcon> allIcons = new ArrayList<>();

    public float ICON_SIZE = 28;
    public final SvgIcon BARATINAGE_ICON_LARGE;
    public final SvgIcon COPY_ICON;
    public final SvgIcon TRASH_ICON;
    public final SvgIcon LOCK_ICON;
    public final SvgIcon SAVE_ICON;
    public final SvgIcon EXTERNAL_ICON;

    public final SvgIcon LEFT_DOWN_ARROW_ICON;
    public final SvgIcon RIGHT_DOWN_ARROW_ICON;
    public final SvgIcon LEFT_UP_ARROW_ICON;
    public final SvgIcon RIGHT_UP_ARROW_ICON;

    private HashMap<String, SvgIcon> otherIcons = new HashMap<>();

    public AppIcons() {

        BARATINAGE_ICON_LARGE = addIcon(new SvgIcon(
                Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "icon.svg").toString(),
                64, 64));

        COPY_ICON = addIcon(buildFeatherAppImageIcon("copy.svg"));
        TRASH_ICON = addIcon(buildFeatherAppImageIcon("trash.svg"));
        LOCK_ICON = addIcon(buildFeatherAppImageIcon("lock.svg"));
        SAVE_ICON = addIcon(buildFeatherAppImageIcon("save.svg"));
        EXTERNAL_ICON = addIcon(buildFeatherAppImageIcon("external-link.svg"));

        LEFT_DOWN_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-left-down.svg"));
        RIGHT_DOWN_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-right-down.svg"));
        LEFT_UP_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-left-up.svg"));
        RIGHT_UP_ARROW_ICON = addIcon(buildFeatherAppImageIcon("corner-right-up.svg"));

    }

    public SvgIcon getCustomAppImageIcon(String fileName) {
        if (otherIcons.containsKey(fileName)) {
            return otherIcons.get(fileName);
        } else {
            SvgIcon icon = buildCustomAppImageIcon(fileName);
            addIcon(icon);
            otherIcons.put(fileName, icon);
            return icon;
        }
    }

    private SvgIcon addIcon(SvgIcon icon) {
        allIcons.add(icon);
        return icon;
    }

    static private SvgIcon buildCustomAppImageIcon(String name) {
        return buildCustomAppImageIcon(name, AppConfig.AC.ICONS.ICON_SIZE);
    }

    static private SvgIcon buildCustomAppImageIcon(String name, float size) {
        return new SvgIcon(
                Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "custom", name).toString(),
                size, size);
    }

    static private SvgIcon buildFeatherAppImageIcon(String name) {
        return buildFeatherAppImageIcon(name, AppConfig.AC.ICONS.ICON_SIZE * 0.75f);
    }

    static private SvgIcon buildFeatherAppImageIcon(String name, float size) {
        return new SvgIcon(
                Path.of(AppConfig.AC.ICONS_RESOURCES_DIR, "feather", name).toString(),
                size, size);
    }

    public void updateAllIcons() {
        for (SvgIcon icon : allIcons) {
            icon.rebuildIcon();
        }
        SwingUtilities.updateComponentTreeUI(AppConfig.AC.APP_MAIN_FRAME);
    }
}
