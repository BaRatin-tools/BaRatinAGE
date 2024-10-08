package org.baratinage.ui.component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.baratinage.AppSetup;
import org.baratinage.utils.perf.TimedActions;

public class SimpleTextAreaField extends JTextArea {

    private static Color REGULAR_BG = new JTextField().getBackground();
    private boolean isViewValid = true;
    private static Color INVALID_BG = AppSetup.COLORS.INVALID_BG;
    private static Color INVALID_DISABLED_BG = AppSetup.COLORS.INVALID_DISABLED_BG;

    public SimpleTextAreaField() {
        super();

        getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                fireChangeListeners();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireChangeListeners();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireChangeListeners();
            }

        });
    }

    private boolean doNotFireChange = false;

    protected void setTextWithoutFiringChangeListeners(String text) {
        doNotFireChange = true;
        setText(text);
        doNotFireChange = false;
    }

    public void setTextDelayed(String text, Boolean fireChangeListeners) {
        TimedActions.delay(0, () -> {
            if (!fireChangeListeners) {
                setTextWithoutFiringChangeListeners(text);
            } else {
                setText(text);
            }
        });
    }

    private final List<ChangeListener> textChangeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener listener) {
        textChangeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        textChangeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        if (doNotFireChange)
            return;
        for (ChangeListener cl : textChangeListeners) {
            cl.stateChanged(new ChangeEvent(this));
        }
    }

    private final List<Predicate<String>> textValidators = new ArrayList<>();

    public void addTextValidator(Predicate<String> validator) {
        textValidators.add(validator);
    }

    public void removeTextValidator(Predicate<String> validator) {
        textValidators.remove(validator);
    }

    public boolean isTextValid() {
        String text = getText();
        for (Predicate<String> tv : textValidators) {
            if (!tv.test(text))
                return false;
        }
        return true;
    }

    public void setValidityView(boolean valid) {
        isViewValid = valid;
        updateBackgroundColor();
    }

    private void updateBackgroundColor() {
        Color c = null;
        boolean isEnabled = isEnabled();
        if (!isViewValid && isEnabled) {
            c = INVALID_BG;
        } else if (!isViewValid && !isEnabled) {
            c = INVALID_DISABLED_BG;
        } else if (isViewValid && isEnabled) {
            c = REGULAR_BG;
        }
        setBackground(c);
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        super.setEnabled(isEnabled);
        updateBackgroundColor();
    }
}
