package org.baratinage.ui.lg;

import javax.swing.AbstractButton;
import javax.swing.JLabel;

import org.baratinage.ui.component.TextField;

public abstract class LgElement<T> {

    public final T object;

    public LgElement(T obj) {
        object = obj;
    }

    public abstract void setTranslatedText();

    public static LgElement<JLabel> registerLabel(JLabel label, String resourceKey, String textKey) {
        return registerLabel(label, resourceKey, textKey, false);
    }

    public static LgElement<JLabel> registerLabel(JLabel label, String resourceKey, String textKey, boolean html) {
        LgElement<JLabel> lge = new LgElement<JLabel>(label) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText(resourceKey, textKey, html);
                object.setText(text);
            }
        };
        Lg.register(lge);
        return lge;
    }

    public static LgElement<AbstractButton> registerButton(AbstractButton button, String resourceKey, String textKey) {
        return registerButton(button, resourceKey, textKey, false);
    }

    public static LgElement<AbstractButton> registerButton(AbstractButton button, String resourceKey, String textKey,
            boolean html) {
        LgElement<AbstractButton> lge = new LgElement<AbstractButton>(button) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText(resourceKey, textKey, html);
                object.setText(text);
            }
        };
        Lg.register(lge);
        return lge;
    };

    public static LgElement<TextField> registerTextFieldPlaceholder(TextField textField, String resourceKey,
            String textKey) {
        LgElement<TextField> lge = new LgElement<TextField>(textField) {
            @Override
            public void setTranslatedText() {
                String text = Lg.getText(resourceKey, textKey);
                object.setPlaceholder(text);
            }
        };
        Lg.register(lge);
        return lge;
    }
}
