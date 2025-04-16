package org.baratinage.ui.textfile;

import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.baratinage.ui.component.SimpleTextAreaField;

public class EquationEditor extends SimpleTextAreaField {

    private boolean equationValidity = false;

    public EquationEditor() {
        addChangeListener(l -> {
            equationValidity = isEquationValid(getText());
            setValidityView(equationValidity);
        });
        setValidityView(false);
    }

    public boolean isEquationValid() {
        return equationValidity;
    }

    public HashSet<String> getVariables() {
        return EquationEditor.getVariables(getText());
    }

    static public HashSet<String> getVariables(String equation) {

        Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b(?!\\s*\\()");

        Matcher matcher = pattern.matcher(equation);
        HashSet<String> variables = new HashSet<>();

        while (matcher.find()) {
            String variable = matcher.group();
            variables.add(variable);
        }

        return variables;
    }

    public static boolean isEquationValid(String equation) {

        if (!areParenthesesBalanced(equation)) {
            return false;
        }
        // Numbers, operators, parentheses, Allowed functions whitespace
        String numbersOperationParenthesisSpace = "^[\\d\\s+\\-*/^()_.]+";
        // Allowed functions
        String functions = "|\\b(|abs|exp|log10|log|sqrt|sinh|cosh|tanh|sin|cos|tan|asin|acos|atan|is0|ispos|isneg|isspos|issneg)\\b";
        // Variables: start with letter or underscore, followed by letters/numbers
        String varNames = "|\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b";

        String validPattern = numbersOperationParenthesisSpace + functions + varNames;

        Pattern pattern = Pattern.compile(validPattern);
        Matcher matcher = pattern.matcher(equation);

        if (!matcher.find()) {
            return false;
        }

        if (!areOperatorsValid(equation)) {
            return false;
        }

        return true;
    }

    private static boolean areParenthesesBalanced(String equation) {
        Stack<Character> stack = new Stack<>();
        for (char ch : equation.toCharArray()) {
            if (ch == '(') {
                stack.push(ch);
            } else if (ch == ')') {
                if (stack.isEmpty() || stack.pop() != '(') {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    private static boolean areOperatorsValid(String equation) {
        // String operatorPattern = "[+\\-*/^]";
        String invalidOperatorSequence = "([+\\-*/^]{2,})"; // Two or more consecutive operators

        Pattern invalidPattern = Pattern.compile(invalidOperatorSequence);
        Matcher invalidMatcher = invalidPattern.matcher(equation);

        if (invalidMatcher.find()) {
            return false;
        }

        equation = equation.trim();
        if (equation.startsWith("+") || equation.startsWith("-") || equation.startsWith("*") ||
                equation.startsWith("/") || equation.startsWith("^") ||
                equation.endsWith("+") || equation.endsWith("-") || equation.endsWith("*") ||
                equation.endsWith("/") || equation.endsWith("^")) {
            return false;
        }

        return true;
    }

}
