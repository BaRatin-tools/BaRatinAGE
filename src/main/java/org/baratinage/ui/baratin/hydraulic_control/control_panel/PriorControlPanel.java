package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.AppConfig;
import org.baratinage.ui.commons.AbstractParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.component.SvgIcon;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.lg.Lg;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class PriorControlPanel extends GridPanel {

        private static String vAlignFixString = "<sup>&nbsp;</sup><sub>&nbsp;</sub>";

        private static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

        public static final ImageIcon activationHeightIcon = SvgIcon.buildCustomAppImageIcon(
                        "activation_height.svg", AppConfig.AC.ICON_SIZE);

        public static final ImageIcon slopeIcon = SvgIcon.buildCustomAppImageIcon(
                        "slope.svg", AppConfig.AC.ICON_SIZE);

        public static final ImageIcon weirCoefIcon = SvgIcon.buildCustomAppImageIcon(
                        "weir_coefficient.svg", AppConfig.AC.ICON_SIZE);

        public static final ImageIcon stricklerCoefIcon = SvgIcon.buildCustomAppImageIcon(
                        "strickler_coef.svg", AppConfig.AC.ICON_SIZE);

        public static final ImageIcon widthIcon = SvgIcon.buildCustomAppImageIcon(
                        "width.svg", AppConfig.AC.ICON_SIZE);

        public static final ImageIcon gravityIcon = SvgIcon.buildCustomAppImageIcon(
                        "gravity.svg", AppConfig.AC.ICON_SIZE);

        public static final ImageIcon exponentIcon = SvgIcon.buildCustomAppImageIcon(
                        "exponent.svg", AppConfig.AC.ICON_SIZE);

        public static final ImageIcon coefficientIcon = SvgIcon.buildCustomAppImageIcon(
                        "coefficient.svg", AppConfig.AC.ICON_SIZE);

        private static final ImageIcon lockIcon = SvgIcon.buildFeatherAppImageIcon(
                        "lock.svg", AppConfig.AC.ICON_SIZE * 0.8f);

        private final JLabel equationLabel;

        private final List<JLabel> columnHeaders;
        private final List<AbstractParameterPriorDist> parameters = new ArrayList<>();

        public PriorControlPanel(int nColumns, String equation) {
                setPadding(5);
                setGap(5);

                for (int k = 0; k < nColumns; k++) {
                        setColWeight(k + 3, 1);
                }

                equationLabel = new JLabel();
                equationLabel.setText(String.format("<html>%s %s</html>", equation, vAlignFixString));
                equationLabel.setFont(MONOSPACE_FONT);

                insertChild(equationLabel, 0, 1, 3, 1);

                JSeparator iconNamSymbolUnitSep = new JSeparator(JSeparator.HORIZONTAL);
                insertChild(iconNamSymbolUnitSep, 0, 2, 3, 1);

                JLabel lockLabel = new JLabel();
                lockLabel.setIcon(lockIcon);
                insertChild(lockLabel, 3 + nColumns, 1);
                JSeparator lockSeparator = new JSeparator(JSeparator.HORIZONTAL);
                insertChild(lockSeparator, 3 + nColumns, 2);

                columnHeaders = new ArrayList<>();
                for (int k = 0; k < nColumns; k++) {
                        JLabel label = new JLabel("column #" + k + 1);
                        columnHeaders.add(label);
                        insertChild(label, 3 + k, 1);
                        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
                        insertChild(sep, 3 + k, 2);
                }

        }

        protected void setHeaders(String... headers) {
                int n = headers.length;
                if (n != columnHeaders.size()) {
                        throw new IllegalArgumentException(
                                        "Number of provided headers doesn't match the number of need headers!");
                }
                for (int k = 0; k < n; k++) {
                        columnHeaders.get(k).setText(headers[k]);
                }
        }

        protected void addParameter(ParameterPriorDistSimplified parameter) {

                int index = parameters.size() + 3;
                int colIndex = 0;
                insertChild(parameter.iconLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.nameLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.symbolUnitLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.meanValueField, colIndex, index);
                colIndex++;
                insertChild(parameter.uncertaintyValueField, colIndex, index);
                colIndex++;
                insertChild(parameter.lockCheckbox, colIndex, index);
                colIndex++;

                parameters.add(parameter);
        }

        protected void addParameter(ParameterPriorDist parameter) {

                int index = parameters.size() + 3;
                int colIndex = 0;

                insertChild(parameter.iconLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.nameLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.symbolUnitLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.initialGuessField, colIndex, index);
                colIndex++;
                insertChild(parameter.distributionField.distributionCombobox, colIndex, index);
                colIndex++;
                insertChild(parameter.distributionField.parameterFieldsPanel, colIndex, index);
                colIndex++;
                insertChild(parameter.lockCheckbox, colIndex, index);
                colIndex++;

                parameters.add(parameter);
        }

        protected void setParameterPriorDist(int index, boolean isLocked, Parameter parameter) {
                AbstractParameterPriorDist p = parameters.get(index);
                if (p != null) {
                        p.configure(isLocked, parameter);
                }
        }

        public Parameter[] getParameters() {
                int n = parameters.size();
                Parameter[] pars = new Parameter[n];
                for (int k = 0; k < n; k++) {
                        pars[k] = parameters.get(k).getParameter();
                }
                return pars;
        }

        public void setGlobalLock(boolean lock) {
                for (AbstractParameterPriorDist p : parameters) {
                        p.setGlobalLock(lock);
                }
        }

        public JSONArray toJSON() {
                JSONArray json = new JSONArray();
                int n = parameters.size();
                for (int k = 0; k < n; k++) {
                        json.put(k, parameters.get(k).toJSON());
                }
                return json;
        }

        public void fromJSON(JSONArray json) {
                int n = parameters.size();
                for (int k = 0; k < n; k++) {
                        parameters.get(k).fromJSON(json.getJSONObject(k));
                }
        }

}
