package org.baratinage.ui.bam;

import javax.swing.ImageIcon;

import org.baratinage.ui.component.SvgIcon;

public enum BamItemType {
    EMPTY_ITEM("empty_item"),
    HYDRAULIC_CONFIG("hydraulic_config"),
    GAUGINGS("gaugings"),
    HYDROGRAPH("hydrograph"),
    LIMNIGRAPH("limnigraph"),
    RATING_CURVE("rating_curve"),
    STRUCTURAL_ERROR("structural_error_model"),
    IMPORTED_DATASET("imported_dataset");

    public String id;
    private ImageIcon icon;
    private ImageIcon addIcon;
    private static final float ICON_SIZE = 30;

    private BamItemType(String id) {
        this.id = id;
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            icon = SvgIcon.buildCustomAppImageIcon(id + ".svg", ICON_SIZE);
        }
        return icon;
    }

    public ImageIcon getAddIcon() {
        if (addIcon == null) {
            addIcon = SvgIcon.buildCustomAppImageIcon(id + "_add.svg", ICON_SIZE);
        }
        return addIcon;
    }
}
