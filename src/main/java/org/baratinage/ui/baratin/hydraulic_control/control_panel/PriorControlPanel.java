package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.AbstractParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.GridPanel;
import org.json.JSONArray;

public abstract class PriorControlPanel extends GridPanel implements ChangeListener {

        protected record KBACGaussianConfig(
                        Double kbMean, Double kbStd,
                        Double aMean, Double aStd,
                        Double cMean, Double cStd) {
        }; // FIXME: rename to KBACGaussianConfig, rename kMean and kStd

        private static String vAlignFixString = "<sup>&nbsp;</sup><sub>&nbsp;</sub>";

        private static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

        private final JLabel equationLabel;
        protected final JLabel lockLabel;

        private final List<JLabel> columnHeaders;
        private final List<AbstractParameterPriorDist> parameters = new ArrayList<>();

        public PriorControlPanel(int nColumns, String equation) {
                setPadding(0);
                setGap(5);

                equationLabel = new JLabel();
                equationLabel.setText(String.format("<html>%s %s</html>", equation, vAlignFixString));
                equationLabel.setFont(MONOSPACE_FONT);

                insertLabel(equationLabel, 0, 0, 3);

                insertChild(new SimpleSep(), 0, 1, 3, 1);

                lockLabel = new JLabel();
                lockLabel.setIcon(AppSetup.ICONS.LOCK);
                insertLabel(lockLabel, 3 + nColumns, 0);
                insertChild(new SimpleSep(), 3 + nColumns, 1);

                columnHeaders = new ArrayList<>();
                for (int k = 0; k < nColumns; k++) {
                        JLabel label = new JLabel("column #" + k + 1);
                        columnHeaders.add(label);
                        insertLabel(label, 3 + k, 0);
                        insertChild(new SimpleSep(), 3 + k, 1);
                }

        }

        private void insertLabel(JLabel label, int x, int y, int spanX) {
                insertChild(label, x, y,
                                spanX, 1, ANCHOR.C, FILL.BOTH,
                                0, 0, -5, 0);
        }

        private void insertLabel(JLabel label, int x, int y) {
                insertLabel(label, x, y, 1);
        }

        protected void setHeaders(String... headers) {
                int n = headers.length;
                if (n != columnHeaders.size()) {
                        throw new IllegalArgumentException(
                                        "Number of provided headers doesn't match the number of needed headers!");
                }
                for (int k = 0; k < n; k++) {
                        columnHeaders.get(k).setText(headers[k]);
                }
        }

        protected void addParameter(ParameterPriorDistSimplified parameter) {

                setColWeight(3, 1);
                setColWeight(4, 1);

                int index = parameters.size() + 2;
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

                parameter.meanValueField.addChangeListener(this);
                parameter.uncertaintyValueField.addChangeListener(this);

                parameters.add(parameter);

                T.updateHierarchy(this, parameter);
        }

        protected void addParameter(ParameterPriorDist parameter) {

                setColWeight(4, 2);
                setColWeight(5, 1);

                int index = parameters.size() + 2;
                int colIndex = 0;

                insertChild(parameter.iconLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.nameLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.symbolUnitLabel, colIndex, index);
                colIndex++;
                insertChild(parameter.distributionField.distributionCombobox, colIndex, index);
                colIndex++;
                insertChild(parameter.distributionField.parameterFieldsPanel, colIndex, index);
                colIndex++;
                insertChild(parameter.initialGuessField, colIndex, index);
                colIndex++;
                insertChild(parameter.menuButton, colIndex, index);
                colIndex++;

                parameter.initialGuessField.addChangeListener(this);
                parameter.distributionField.distributionCombobox.addChangeListener(this);
                for (SimpleNumberField f : parameter.distributionField.parameterFields) {
                        f.addChangeListener(this);
                }
                parameters.add(parameter);
                T.updateHierarchy(this, parameter);
        }

        public Parameter[] getParameters() {
                int n = parameters.size();
                Parameter[] pars = new Parameter[n];
                for (int k = 0; k < n; k++) {
                        Parameter par = parameters.get(k).getParameter();
                        if (par == null) {
                                return null;
                        }
                        pars[k] = par;
                }
                return pars;
        }

        public void setGlobalLock(boolean lock) {
                for (AbstractParameterPriorDist p : parameters) {
                        p.setEnabled(!lock);
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

        private final List<ChangeListener> changeListeners = new ArrayList<>();

        public void addChangeListener(ChangeListener l) {
                changeListeners.add(l);
        }

        public void removeChangeListener(ChangeListener l) {
                changeListeners.remove(l);
        }

        protected void fireChangeListeners() {
                for (ChangeListener l : changeListeners) {
                        l.stateChanged(new ChangeEvent(this));
                }
        }

        @Override
        public void stateChanged(ChangeEvent chEvt) {
                fireChangeListeners();
        }

        public abstract KBACGaussianConfig toKACGaussianConfig();

}
