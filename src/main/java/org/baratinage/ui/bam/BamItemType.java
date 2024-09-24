package org.baratinage.ui.bam;

import javax.swing.ImageIcon;

import org.baratinage.AppSetup;

public enum BamItemType {
    /**
     * **Important notes:**
     * 
     * ids are used as translation keys:
     * - id = name of the item type
     * - "create_" + id = create item of this item type
     * 
     * and to find icon svg images (see below):
     * - id + ".svg" = main item type icon
     * - id + "_add.svg" = create item of that type icon
     * 
     * setBuilderFunction and setAddItemAction should be called at least once
     */

    EMPTY_ITEM("empty_item"),
    HYDRAULIC_CONFIG("hydraulic_config"),
    HYDRAULIC_CONFIG_BAC("hydraulic_config_bac"),
    HYDRAULIC_CONFIG_QFH("hydraulic_config_qfh"),
    GAUGINGS("gaugings"),
    HYDROGRAPH("hydrograph"),
    LIMNIGRAPH("limnigraph"),
    RATING_CURVE("rating_curve"),
    STRUCTURAL_ERROR("structural_error_model"),
    IMPORTED_DATASET("imported_dataset");

    public final String id;

    private ImageIcon icon;
    private ImageIcon addIcon;

    private BamItemType(String id) {
        this.id = id;
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            icon = AppSetup.ICONS.getCustomAppImageIcon(id + ".svg");
        }
        return icon;
    }

    public ImageIcon getAddIcon() {
        if (addIcon == null) {
            addIcon = AppSetup.ICONS.getCustomAppImageIcon(id + "_add.svg");
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
