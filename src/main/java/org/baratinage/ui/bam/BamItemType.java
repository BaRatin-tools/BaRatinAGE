package org.baratinage.ui.bam;

public enum BamItemType {
    EMPTY_ITEM("empty_item"),
    HYDRAULIC_CONFIG("hydraulic_config"),
    GAUGINGS("gaugings"),
    HYDROGRAPH("hydrograph"),
    LIMNIGRAPH("limnigraph"),
    RATING_CURVE("rating_curve"),
    STRUCTURAL_ERROR("structural_error"),
    IMPORTED_DATASET("imported_dataset");

    public String name;

    private BamItemType(String name) {
        this.name = name;
    }
}
