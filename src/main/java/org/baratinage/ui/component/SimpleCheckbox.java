package org.baratinage.ui.component;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;

public class SimpleCheckbox extends FlatTriStateCheckBox {
    public SimpleCheckbox() {
        super();
        setAllowIndeterminate(false);
    }
}
