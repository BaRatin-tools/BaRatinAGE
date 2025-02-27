package org.baratinage.ui.component;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.baratinage.translation.T;
import org.baratinage.utils.Misc;

public class SimpleNumberField extends SimpleTextField {

    private Double doubleValue = null;
    private Integer intValue = null;

    private static NumberFormat DOUBLE_FORMATTER = NumberFormat.getNumberInstance();
    private static NumberFormat INTEGER_FORMATTER = NumberFormat.getIntegerInstance();
    private boolean integer = false;

    public static void init() {
        T.permanent(() -> {
            DOUBLE_FORMATTER = NumberFormat.getNumberInstance(T.getLocale());
            INTEGER_FORMATTER = NumberFormat.getIntegerInstance(T.getLocale());
            DOUBLE_FORMATTER.setGroupingUsed(false);
            INTEGER_FORMATTER.setGroupingUsed(false);
        });
    }

    private List<Predicate<Number>> validators = new ArrayList<>();

    private static String doubleToString(Double value) {
        if (value == null) {
            return "";
        } else {
            return Misc.formatNumber(value, true);
        }
    }

    private static Double stringToDouble(String str) {
        try {
            Double d = Double.parseDouble(str);
            return d;
        } catch (Exception e) {
            try {
                Number n = DOUBLE_FORMATTER.parse(str);
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
            return value.toString();
        }

    }

    private static Integer stringToInteger(String str) {
        try {
            Integer i = Integer.parseInt(str);
            return i;
        } catch (Exception e) {
            try {
                Number n = INTEGER_FORMATTER.parse(str);
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

        // using tLasting to make sure it doesn't get overriden elsewhere

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

        updateValidityView();

        T.t(this, this::updateTextFieldFromValue);
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
        setValidityView(isValueValid());
    }

    public void setValue(Double value) {
        if (integer) {
            return;
        }
        if (value != null && Double.isNaN(value)) {
            value = null;
        }
        doubleValue = value;
        updateTextFieldFromValue();
        updateValidityView();
    }

    public void setValue(Integer value) {
        if (!integer) {
            return;
        }
        intValue = value;
        updateTextFieldFromValue();
        updateValidityView();
    }

    public void unsetValue() {
        intValue = null;
        doubleValue = null;
        updateTextFieldFromValue();
        updateValidityView();
    }

    public Double getDoubleValue() {
        if (doubleValue != null && Double.isInfinite(doubleValue)) {
            if (doubleValue == Double.POSITIVE_INFINITY) {
                return Double.MAX_VALUE;
            } else if (doubleValue == Double.NEGATIVE_INFINITY) {
                return Double.MIN_VALUE;
            }
        }
        return doubleValue;
    }

    public Integer getIntegerValue() {
        return intValue;
    }

    public void addValidator(Predicate<Number> validator) {
        validators.add(validator);
        updateValidityView();
    }

    public void removeValidator(Predicate<Number> validator) {
        validators.remove(validator);
        updateValidityView();
    }

}
