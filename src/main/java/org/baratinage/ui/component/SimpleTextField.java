package org.baratinage.ui.component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SimpleTextField extends JTextField {

    private String placeholder;

    public SimpleTextField() {
        super();

        Dimension dim = this.getPreferredSize();
        dim.width = 200;
        setPreferredSize(dim);
        Dimension minDim = getMinimumSize();
        minDim.width = 100;
        setMinimumSize(minDim);

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

    // Placeholder implementation comes from: https://stackoverflow.com/a/16229082
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setColor(getDisabledTextColor());
        float maxCharHeight = g2D.getFontMetrics().getMaxAscent();
        float totalHeight = getHeight();
        float x = getInsets().left;

        float y = maxCharHeight / 2 + totalHeight / 2 - getInsets().top / 2; // fixed vertical centering of placeholder
                                                                             // text
        // float y = maxCharHeight + getInsets().top; // original value
        // x, y is the baseline of first character
        g2D.drawString(placeholder, x, y);
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }

    private boolean doNotFireChange = false;

    protected void setTextWithoutFiringChangeListeners(String text) {
        doNotFireChange = true;
        super.setText(text);
        doNotFireChange = false;
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

}
