package org.baratinage.ui.bam;

import javax.swing.ImageIcon;

import org.baratinage.ui.AppConfig;
import org.baratinage.utils.Misc;

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
        return builder.build(Misc.getTimeStampedId());
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            icon = AppConfig.AC.ICONS.getCustomAppImageIcon(id + ".svg");
        }
        return icon;
    }

    public ImageIcon getAddIcon() {
        if (addIcon == null) {
            addIcon = AppConfig.AC.ICONS.getCustomAppImageIcon(id + "_add.svg");
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
