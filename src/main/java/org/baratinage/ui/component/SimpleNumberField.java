package org.baratinage.ui.component;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.plaf.basic.BasicBorders;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.lg.Lg;

public class SimpleNumberField extends SimpleTextField {

    private Double doubleValue = null;
    private Integer intValue = null;

    private static NumberFormat doubleFormatter = NumberFormat.getNumberInstance();
    private static NumberFormat integerFormatter = NumberFormat.getIntegerInstance();
    private boolean integer = false;

    private List<Predicate<Number>> validators = new ArrayList<>();

    private final Color defaultBg;
    private final Color invalidBg;

    private static String doubleToString(Double value) {
        if (value == null) {
            return "";
        } else {
            return doubleFormatter.format(value);
        }
    }

    private static Double stringToDouble(String str) {
        try {
            Double d = Double.parseDouble(str);
            return d;
        } catch (Exception e) {
            try {
                Number n = doubleFormatter.parse(str);
                Double d = n.doubleValue();
                return d;
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    private static String integerToString(Integer value) {
        if (value == null) {
            return "";
        } else {
            return integerFormatter.format(value);
        }

    }

    private static Integer stringToInteger(String str) {
        try {
            Integer i = Integer.parseInt(str);
            return i;
        } catch (Exception e) {
            try {
                Number n = integerFormatter.parse(str);
                Integer i = n.intValue();
                return i;
            } catch (ParseException e1) {
                return null;
            }
        }
    }

    public SimpleNumberField() {
        this(false);
    }

    public SimpleNumberField(boolean integer) {
        super();
        this.integer = integer;

        Lg.register(this, () -> {
            doubleFormatter = NumberFormat.getNumberInstance(Lg.getLocale());
            integerFormatter = NumberFormat.getIntegerInstance(Lg.getLocale());
            doubleFormatter.setGroupingUsed(false);
            integerFormatter.setGroupingUsed(false);
            updateTextFieldFromValue();
        });

        addChangeListener((chEvt) -> {
            String str = getText();
            if (integer) {
                intValue = stringToInteger(str);
            } else {
                doubleValue = stringToDouble(str);
            }

            updateValidityView();
        });

        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                updateTextFieldFromValue();
            }

        });

        defaultBg = getBackground();
        invalidBg = AppConfig.AC.INVALID_COLOR_BG;
    }

    public boolean isValueValid() {
        Number value = null;
        if (integer) {
            if (intValue == null) {
                return false;
            }
            value = intValue;
        } else {
            if (doubleValue == null) {
                return false;
            }
            value = doubleValue;
        }
        for (Predicate<Number> validator : validators) {
            if (!validator.test(value)) {
                return false;
            }
        }
        return true;
    }

    private void updateTextFieldFromValue() {
        String str = integer ? integerToString(intValue) : doubleToString(doubleValue);
        setTextWithoutFiringChangeListeners(str);
    }

    private void updateValidityView() {
        setBackground(isValueValid() ? defaultBg : invalidBg);
    }

    public void setValue(Double value) {
        if (integer) {
            return;
        }
        doubleValue = value;
        updateTextFieldFromValue();
    }

    public void setValue(Integer value) {
        if (!integer) {
            return;
        }
        intValue = value;
        updateTextFieldFromValue();
    }

    public void unsetValue() {
        intValue = null;
        doubleValue = null;
        updateTextFieldFromValue();
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public Integer getIntegerValue() {
        return intValue;
    }

    public void addValidator(Predicate<Number> validator) {
        validators.add(validator);
    }

    public void removeValidator(Predicate<Number> validator) {
        validators.remove(validator);
    }

    // private boolean i
}
