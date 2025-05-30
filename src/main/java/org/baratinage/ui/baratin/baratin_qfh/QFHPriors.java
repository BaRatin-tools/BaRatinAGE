package org.baratinage.ui.baratin.baratin_qfh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.baratin.baratin_qfh.QFHPreset.QFHPresetParameter;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONArray;
import org.json.JSONObject;

public class QFHPriors extends JScrollPane implements IPriors, ChangeListener {

    private final HashMap<String, QFHPriorParameterDist> priorParDists;
    private final Set<String> usedParNames;

    private final GridPanel mainPanel;
    private final List<JLabel> headers;

    public QFHPriors() {
        super();

        setBorder(new EmptyBorder(0, 0, 0, 0));
        mainPanel = new GridPanel();
        mainPanel.setAnchor(GridPanel.ANCHOR.N);
        setViewportView(mainPanel);

        priorParDists = new HashMap<>();
        usedParNames = new LinkedHashSet<>();

        mainPanel.setColWeight(3, 2);
        mainPanel.setColWeight(4, 1);

        mainPanel.setPadding(5);
        mainPanel.setGap(5);

        headers = new ArrayList<>();
        // setting up headers
        addHeaderItem("optional_type");
        addHeaderItem("name");
        addHeaderItem("distribution");
        addHeaderItem("distribution_parameters");
        addHeaderItem("initial_guess");
    }

    private void addHeaderItem(String lgKey) {
        JLabel headerLabel = new JLabel();
        T.t(this, headerLabel, true, lgKey);
        addHeaderItem(headerLabel);
    }

    private void addHeaderItem(JLabel label) {
        headers.add(label);
    }

    private void insertHeaders() {
        for (int k = 0; k < headers.size(); k++) {
            mainPanel.insertChild(headers.get(k), k, 0);
            mainPanel.insertChild(new SimpleSep(), k, 1);
        }
    }

    public void setFromPreset(QFHPreset preset) {
        reset();
        for (QFHPresetParameter p : preset.parameters()) {
            QFHPriorParameterDist priorParDist = new QFHPriorParameterDist(p.symbole());
            priorParDist.addChangeListener(this);
            priorParDist.setParameterType(p.type());
            priorParDist.knownParameterType.setEnabled(false);
            if (p.distribution() != null) {
                priorParDist.setDistributionType(
                        DistributionType.getDistribFromBamName(
                                p.distribution().distribution_id()));
                priorParDist.setDistributionParameters(p.distribution().parameters());
                priorParDist.setInitialGuess(p.distribution().initial_guess());
            }

            priorParDists.put(p.symbole(), priorParDist);
            usedParNames.add(p.symbole());
        }
        updatePanel();
    }

    public void updateUsedParNames(Set<String> newUsedParNames) {
        usedParNames.clear();
        for (String parName : newUsedParNames) {
            if (!priorParDists.containsKey(parName)) {
                QFHPriorParameterDist priorParDist = new QFHPriorParameterDist(parName);
                priorParDist.addChangeListener(this);
                priorParDists.put(parName, priorParDist);
            }
            usedParNames.add(parName);
        }
        updatePanel();
    }

    private void updatePanel() {
        mainPanel.clear();
        insertHeaders();
        int rowIndex = 2;
        for (String parName : usedParNames) {
            int colIndex = 0;
            QFHPriorParameterDist p = priorParDists.get(parName);
            if (p == null) {
                ConsoleLogger.error("QFHPriorParameterDist not found though it should exist!");
                continue;
            }
            if (p.knownParameterType.isEnabled()) {
                mainPanel.insertChild(p.knownParameterType, colIndex, rowIndex);
            } else {
                JLabel label = p.knownParameterType.getSelectedItemLabel();
                if (label == null) {
                    System.out.println("Label is null when it shouldn't (" + p.bamName + ")");
                }
                mainPanel.insertChild(label != null ? label : p.knownParameterType, colIndex, rowIndex);
            }
            colIndex++;
            mainPanel.insertChild(p.nameLabel, colIndex, rowIndex,
                    1, 1,
                    GridPanel.ANCHOR.C, GridPanel.FILL.BOTH,
                    0, 20, 0, 5);
            colIndex++;
            mainPanel.insertChild(p.distributionField.distributionCombobox, colIndex, rowIndex);
            colIndex++;
            mainPanel.insertChild(p.distributionField.parameterFieldsPanel, colIndex, rowIndex);
            colIndex++;
            mainPanel.insertChild(p.initialGuessField, colIndex, rowIndex);
            colIndex++;
            mainPanel.insertChild(p.menuButton, colIndex, rowIndex);
            rowIndex++;
        }
        updateUI();
    }

    public void reset() {
        for (QFHPriorParameterDist QFHPriorParDist : priorParDists.values()) {
            QFHPriorParDist.removeChangeListener(this);
        }
        priorParDists.clear();
        usedParNames.clear();
    }

    @Override
    public Parameter[] getParameters() {
        int n = usedParNames.size();
        Parameter[] parameters = new Parameter[n];
        int k = 0;
        for (String parName : usedParNames) {
            QFHPriorParameterDist p = priorParDists.get(parName);
            parameters[k] = p.getParameter();
            k++;
        }
        return parameters;
    }

    private List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(e);
        }
    }

    public JSONArray toJSON() {
        JSONArray json = new JSONArray();
        for (String key : priorParDists.keySet()) {
            JSONObject j = new JSONObject();
            j.put("key", key);
            j.put("priorParDist", priorParDists.get(key).toJSON());
            json.put(j);
        }
        return json;
    }

    public void fromJSON(JSONArray json, boolean shouldEnableParTypeConfig) {
        for (int k = 0; k < json.length(); k++) {
            JSONObject j = json.getJSONObject(k);
            String key = j.optString("key", "");
            JSONObject priorParDistConfig = j.optJSONObject("priorParDist");
            if (!priorParDists.containsKey(key)) {
                continue;
            }
            QFHPriorParameterDist par = priorParDists.get(key);
            par.fromJSON(priorParDistConfig);
            par.knownParameterType.setEnabled(shouldEnableParTypeConfig);
        }
        updatePanel();
    }
}
