package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
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
    private String innerLabel;

    private final Border defaultBorder;

    private final Font placeholderFont;
    private final Color placeholderColor;

    private final Font innerLabelFont;
    private final Color innerLabelColor;

    public SimpleTextField() {
        super();

        int H = 32;

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
        placeholderFont = getFont();
        placeholderColor = new Color(180, 180, 200);

        innerLabelFont = getFont().deriveFont(13f);
        innerLabelColor = new Color(180, 180, 200);

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

        boolean hasPlaceholder = placeholder != null && placeholder.length() > 0;
        boolean hasInnerLabel = innerLabel != null && innerLabel.length() > 0;

        super.paintComponent(g);

        final Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = getInsets();
        float fieldHeight = getHeight();

        if (hasPlaceholder && getText().length() == 0) {

            g2D.setColor(placeholderColor);
            g2D.setFont(placeholderFont);
            float maxCharHeight = g2D.getFontMetrics().getMaxAscent();

            float x = insets.left;

            float y = maxCharHeight / 2 + fieldHeight / 2 - Math.round((float) insets.top / 2f);
            g2D.drawString(placeholder, x, y);

        }
        if (hasInnerLabel) {
            g2D.setColor(innerLabelColor);
            g2D.setFont(innerLabelFont);
            setBorder(new CompoundBorder(defaultBorder, new EmptyBorder(12, 0, 0, 0)));

            float maxCharHeight = g2D.getFontMetrics().getMaxAscent();

            float x = insets.left;
            float y = maxCharHeight;

            g2D.drawString(innerLabel, x, y);
        } else {
            setBorder(defaultBorder);
        }

    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }

    public void setInnerLabel(final String s) {
        innerLabel = s;
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
