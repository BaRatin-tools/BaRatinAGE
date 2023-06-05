package org.baratinage.ui.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextField extends JTextField {

    private String placeholder;

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

    // Placeholder implementation comes from: https://stackoverflow.com/a/16229082
    @Override
    protected void paintComponent(final Graphics pG) {
        super.paintComponent(pG);

        if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g = (Graphics2D) pG;
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getDisabledTextColor());
        g.drawString(placeholder, getInsets().left, pG.getFontMetrics()
                .getMaxAscent() + getInsets().top);
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
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
