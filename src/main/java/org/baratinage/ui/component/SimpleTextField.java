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

import org.baratinage.AppSetup;
import org.baratinage.utils.perf.TimedActions;

public class SimpleTextField extends JTextField {

    private static Color REGULAR_BG;
    private static Color INVALID_BG;
    private static Color INVALID_DISABLED_BG;
    private static Color INNER_LABEL_FG;
    private boolean isViewValid = true;

    private String placeholder;
    private String innerLabel;

    private final Border defaultBorder;

    private final Font placeholderFont;
    private final Color placeholderColor;

    private final Font innerLabelFont;
    private final Color innerLabelColor;

    public static void init() {
        REGULAR_BG = new JTextField().getBackground();
        INVALID_BG = AppSetup.COLORS.INVALID_BG;
        INVALID_DISABLED_BG = AppSetup.COLORS.INVALID_DISABLED_BG;
        INNER_LABEL_FG = AppSetup.COLORS.DEFAULT_FG_LIGHT;
    }

    public SimpleTextField() {
        super();

        defaultBorder = getBorder();
        placeholderFont = getFont();
        placeholderColor = INNER_LABEL_FG;

        innerLabelFont = getFont().deriveFont(12f);
        innerLabelColor = INNER_LABEL_FG;

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

    // Placeholder implementation comes from:https:// stackoverflow.com/a/16229082
    @Override
    protected void paintComponent(final Graphics g) {

        super.paintComponent(g);

        final Graphics2D g2D = (Graphics2D) g;
        g2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Insets insets = getInsets();
        float fieldHeight = getHeight();

        if (hasPlaceholder() && getText().length() == 0) {

            g2D.setColor(placeholderColor);
            g2D.setFont(placeholderFont);
            float maxCharHeight = g2D.getFontMetrics().getMaxAscent();

            float x = insets.left;

            float y = maxCharHeight / 2 + fieldHeight / 2 - Math.round((float) insets.top
                    / 2f);
            g2D.drawString(placeholder, x, y);

        }
        if (hasInnerLabel()) {
            g2D.setColor(innerLabelColor);
            g2D.setFont(innerLabelFont);

            float maxCharHeight = g2D.getFontMetrics().getMaxAscent();

            float x = insets.left - 1 - 1;
            float y = maxCharHeight + 2 - 2;

            g2D.drawString(innerLabel, x, y);
        }
    }

    private void updateBorderToAccomodateInnerLabel() {
        if (hasInnerLabel()) {
            setBorder(new CompoundBorder(defaultBorder, new EmptyBorder(12, 0, 0, 0)));
        } else {
            setBorder(defaultBorder);
        }
    }

    public boolean hasPlaceholder() {
        return placeholder != null && placeholder.length() > 0;
    }

    public boolean hasInnerLabel() {
        return innerLabel != null && innerLabel.length() > 0;
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setInnerLabel(final String s) {
        innerLabel = s;
        updateBorderToAccomodateInnerLabel();
    }

    public String getInnerLabel() {
        return innerLabel;
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

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.width = 0;
        return dim;
    }
}
