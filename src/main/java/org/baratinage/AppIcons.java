package org.baratinage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.baratinage.ui.component.SvgIcon;

public class AppIcons {

    private List<SvgIcon> allIcons = new ArrayList<>();

    public float ICON_SIZE;
    private float FEATHER_SIZE;

    public final SvgIcon BARATINAGE_LARGE;
    public final SvgIcon BARATINAGE;
    public final SvgIcon COPY;
    public final SvgIcon TRASH;
    public final SvgIcon LOCK;
    public final SvgIcon SAVE;
    public final SvgIcon EXTERNAL;
    public final SvgIcon LIST;
    public final SvgIcon FOLDER;
    public final SvgIcon NEW_FILE;
    public final SvgIcon CLOSE;

    public final SvgIcon LEFT_DOWN_ARROW;
    public final SvgIcon RIGHT_DOWN_ARROW;
    public final SvgIcon LEFT_UP_ARROW;
    public final SvgIcon RIGHT_UP_ARROW;

    private HashMap<String, SvgIcon> otherIcons = new HashMap<>();

    public AppIcons() {

        BARATINAGE_LARGE = addIcon(new SvgIcon(
                Path.of(AppSetup.PATH_ICONS_RESOURCES_DIR, "icon.svg").toString(),
                64, 64));

        ICON_SIZE = 28;
        // FEATHER_SIZE = 20;
        FEATHER_SIZE = ICON_SIZE;

        BARATINAGE = addIcon(new SvgIcon(
                Path.of(AppSetup.PATH_ICONS_RESOURCES_DIR, "icon.svg").toString(),
                ICON_SIZE, ICON_SIZE));

        COPY = addIcon(buildFeatherAppImageIcon("copy.svg", FEATHER_SIZE));
        TRASH = addIcon(buildFeatherAppImageIcon("trash.svg", FEATHER_SIZE));
        TRASH.setSvgTagAttribute("stroke", AppSetup.COLORS.DANGER);
        LOCK = addIcon(buildFeatherAppImageIcon("lock.svg", FEATHER_SIZE));
        SAVE = addIcon(buildFeatherAppImageIcon("save.svg", FEATHER_SIZE));
        EXTERNAL = addIcon(buildFeatherAppImageIcon("external-link.svg", FEATHER_SIZE));
        LIST = addIcon(buildFeatherAppImageIcon("list.svg", FEATHER_SIZE));
        FOLDER = addIcon(buildFeatherAppImageIcon("folder.svg", FEATHER_SIZE));
        NEW_FILE = addIcon(buildFeatherAppImageIcon("file-plus.svg", FEATHER_SIZE));
        CLOSE = addIcon(buildFeatherAppImageIcon("x-square.svg", FEATHER_SIZE));

        LEFT_DOWN_ARROW = addIcon(buildFeatherAppImageIcon("corner-left-down.svg", FEATHER_SIZE));
        RIGHT_DOWN_ARROW = addIcon(buildFeatherAppImageIcon("corner-right-down.svg", FEATHER_SIZE));
        LEFT_UP_ARROW = addIcon(buildFeatherAppImageIcon("corner-left-up.svg", FEATHER_SIZE));
        RIGHT_UP_ARROW = addIcon(buildFeatherAppImageIcon("corner-right-up.svg", FEATHER_SIZE));

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
                Path.of(AppSetup.PATH_ICONS_RESOURCES_DIR, "custom", name).toString(),
                size, size);
    }

    static private SvgIcon buildFeatherAppImageIcon(String name, float size) {
        SvgIcon svgIcon = new SvgIcon(
                Path.of(AppSetup.PATH_ICONS_RESOURCES_DIR, "feather", name).toString(),
                size, size);
        svgIcon.setSvgTagAttribute("stroke-width", "1");
        return svgIcon;
    }

    public void updateAllIcons() {
        if (!SvgIcon.scalesHaveChanged()) {
            return;
        }
        for (SvgIcon icon : allIcons) {
            icon.rebuildIcon();
        }
        SvgIcon.memorizeCurrentScales();
        SwingUtilities.updateComponentTreeUI(AppSetup.MAIN_FRAME);
    }
}
