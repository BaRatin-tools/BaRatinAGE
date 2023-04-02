package org.baratinage.ui.component;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextField extends JTextField {

    public TextField() {
        super();

        Dimension dim = this.getPreferredSize();
        dim.width = 200;
        this.setPreferredSize(dim);

        this.getDocument().addDocumentListener(new DocumentListener() {

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

    public void setTextWithoutFiringChangeListeners(String text) {
        doNotFireChange = true;
        super.setText(text);
        doNotFireChange = false;
    }

    @FunctionalInterface
    public interface TextChangeListener extends EventListener {
        public void hasChanged(String newText);
    }

    private final List<TextChangeListener> textChangeListeners = new ArrayList<>();

    public void addChangeListener(TextChangeListener listener) {
        this.textChangeListeners.add(listener);
    }

    public void removeChangeListener(TextChangeListener listener) {
        this.textChangeListeners.remove(listener);
    }

    public void fireChangeListeners() {
        if (doNotFireChange)
            return;
        for (TextChangeListener cl : this.textChangeListeners) {
            cl.hasChanged(getText());
        }
    }

    @FunctionalInterface
    public interface TextValidators {
        public boolean isTextValid(String text);
    }

    private final List<TextValidators> textValidators = new ArrayList<>();

    public void addTextValidator(TextValidators validator) {
        this.textValidators.add(validator);
    }

    public void removeTextValidator(TextValidators validator) {
        this.textValidators.remove(validator);
    }

    public boolean isTextValid() {
        String text = getText();
        for (TextValidators tv : this.textValidators) {
            if (!tv.isTextValid(text))
                return false;
        }
        return true;
    }

}
