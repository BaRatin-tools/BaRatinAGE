package org.baratinage.ui.bam;

import org.baratinage.jbam.EstimatedParameter;

public class EstimatedParameterWrapper {

    public final EstimatedParameter parameter;

    public final String symbol;
    public final String htmlName;

    public enum TYPE {
        MODEL,
        GAMMA,
        LOGPOST,
        DERIVED
    }

    static public final TYPE MODEL = TYPE.MODEL;
    static public final TYPE GAMMA = TYPE.GAMMA;
    static public final TYPE LOGPOST = TYPE.LOGPOST;
    static public final TYPE DERIVED = TYPE.DERIVED;

    public final TYPE type;

    private boolean displayPrior = false;

    public EstimatedParameterWrapper(
            EstimatedParameter parameter,
            String symbol,
            String htmlName,
            TYPE type) {

        this.parameter = parameter;
        this.symbol = symbol;
        this.htmlName = htmlName;

        this.type = type;

        if (type == MODEL) {
            setDisplayPrior(true);
        }
    }

    public EstimatedParameterWrapper(
            EstimatedParameter parameter,
            TYPE type) {

        this.parameter = parameter;
        this.symbol = parameter.name;
        this.htmlName = buildGuessedHtmlName(parameter.name);

        this.type = type;

        if (type == MODEL) {
            setDisplayPrior(true);
        }
    }

    public EstimatedParameterWrapper copyAndModify(String symbol,
            String htmlName) {
        return new EstimatedParameterWrapper(parameter, symbol, htmlName, type);
    }

    public void setDisplayPrior(boolean displayPrior) {
        if (parameter.parameterConfig == null && displayPrior) {
            displayPrior = false;
            return;
        }
        this.displayPrior = displayPrior;
    }

    public boolean shouldDisplayPrior() {
        return displayPrior;
    }

    public static String buildGuessedHtmlName(String symbol) {
        if (symbol.contains("_")) {
            String result = symbol.replaceFirst("_", "<sub>");
            return "<html>" + result + "</sub></html>";
        } else {
            if (symbol.matches(".*\\d$")) {
                int lastDigitIndex = symbol.length() - 1;
                while (lastDigitIndex >= 0 && Character.isDigit(symbol.charAt(lastDigitIndex))) {
                    lastDigitIndex--;
                }
                String beforeNumber = symbol.substring(0, lastDigitIndex + 1);
                String numberPart = symbol.substring(lastDigitIndex + 1);
                return "<html>" + beforeNumber + "<sub>" + numberPart + "</sub></html>";
            } else {
                return "<html>" + symbol + "</html>";
            }
        }
    }
}
