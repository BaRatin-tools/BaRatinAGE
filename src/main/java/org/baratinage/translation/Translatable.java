package org.baratinage.translation;

public interface Translatable {
    public default void translate(Object object) {
        translate();
    }

    public void translate();
}
