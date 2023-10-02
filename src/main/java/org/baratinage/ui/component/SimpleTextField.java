package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SimpleTextField extends JTextField {

    private String placeholder;

    private boolean placeholderAlwaysVisibleOnTop = true;
    private final Border defaultBorder;
    private final Font placeholderFont;
    private final Color placeholderColor;

    public SimpleTextField() {
        super();

        int H = 35;

        Dimension prefDim = this.getPreferredSize();
        prefDim.width = 100;
        prefDim.height = H;
        Dimension minDim = getMinimumSize();
        minDim.width = 50;
        minDim.height = H;
        Dimension maxDim = getMaximumSize();
        maxDim.height = H;

        setPreferredSize(prefDim);
        setMinimumSize(minDim);
        setMinimumSize(maxDim);

        defaultBorder = getBorder();
        placeholderFont = getFont().deriveFont(13f);
        placeholderColor = new Color(150, 150, 175);

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

        boolean hasNoPlaceHolder = placeholder == null || placeholder.length() == 0;

        if (placeholderAlwaysVisibleOnTop && !hasNoPlaceHolder) {
            setBorder(new CompoundBorder(defaultBorder, new EmptyBorder(12, 0, 0, 0)));
        } else {
            setBorder(defaultBorder);
        }

        super.paintComponent(g);

        if (hasNoPlaceHolder) {
            return;
        }
        if (!placeholderAlwaysVisibleOnTop && getText().length() > 0) {
            return;
        }

        final Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setColor(placeholderColor);
        g2D.setFont(placeholderFont);
        float maxCharHeight = g2D.getFontMetrics().getMaxAscent();
        float totalHeight = getHeight();
        float x = getInsets().left;
        float y;
        // original value x, y is the baseline of first character
        if (!placeholderAlwaysVisibleOnTop) {
            // fixed vertical centering of placeholder
            y = maxCharHeight / 2 + totalHeight / 2 - getInsets().top / 2;
        } else {
            y = maxCharHeight;
        }
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
