package org.baratinage.ui.bam;

import java.util.UUID;

import javax.swing.ImageIcon;

import org.baratinage.ui.component.SvgIcon;

public enum BamItemType {
    /**
     * **Important note:**
     * 
     * ids are used as translation keys:
     * - id = name of the item type
     * - "create_" + id = create item of this item type
     * 
     * and to find icon svg images (see below):
     * - id + ".svg" = main item type icon
     * - id + "_add.svg" = create item of that type icon
     * 
     */

    EMPTY_ITEM("empty_item"),
    HYDRAULIC_CONFIG("hydraulic_config"),
    GAUGINGS("gaugings"),
    HYDROGRAPH("hydrograph"),
    LIMNIGRAPH("limnigraph"),
    RATING_CURVE("rating_curve"),
    STRUCTURAL_ERROR("structural_error_model"),
    IMPORTED_DATASET("imported_dataset");

    public String id;

    private BamItemBuilderFunction builder;
    private ImageIcon icon;
    private ImageIcon addIcon;

    private static final float ICON_SIZE = 30;

    @FunctionalInterface
    public interface BamItemBuilderFunction {
        public BamItem build(String id);
    }

    private BamItemType(String id) {
        this.id = id;
    }

    public void setBuilderFunction(BamItemBuilderFunction builder) {
        this.builder = builder;
    }

    public BamItem buildBamItem(String uuid) {
        if (builder == null) {
            return null;
        }
        return builder.build(uuid);
    }

    public BamItem buildBamItem() {
        if (builder == null) {
            return null;
        }
        return builder.build(UUID.randomUUID().toString());
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

    public boolean matchOneOf(BamItemType... itemTypesToMatch) {
        for (BamItemType type : itemTypesToMatch) {
            if (this == type) {
                return true;
            }
        }
        return false;
    }
}
