package org.baratinage.ui.baratin.baratin_qfh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.baratin.baratin_qfh.QFHPreset.QFHPresetParameter;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.utils.ConsoleLogger;
import org.json.JSONArray;
import org.json.JSONObject;

public class QFHPriors extends GridPanel implements IPriors, ChangeListener {

    private final HashMap<String, QFHPriorParameterDist> priorParDists;
    private final HashSet<String> usedParNames;

    private final List<JLabel> headers;

    public QFHPriors() {
        super();
        priorParDists = new HashMap<>();
        usedParNames = new HashSet<>();

        setColWeight(1, 1);
        setColWeight(2, 1);
        setColWeight(4, 1);
        setPadding(0);
        setGap(5);

        headers = new ArrayList<>();
        // setting up headers
        addHeaderItem("optional_type");
        addHeaderItem("name");
        addHeaderItem("initial_guess");
        addHeaderItem("distribution");
        addHeaderItem("distribution_parameters");
    }

    private void addHeaderItem(String lgKey) {
        JLabel headerLabel = new JLabel();
        T.t(this, headerLabel, true, lgKey);
        headers.add(headerLabel);

    }

    private void insertHeaders() {
        for (int k = 0; k < headers.size(); k++) {
            insertChild(headers.get(k), k, 0);
            insertChild(new JSeparator(JSeparator.HORIZONTAL), k, 1);
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

    public void updateUsedParNames(HashSet<String> newUsedParNames) {
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
        clear();
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
                insertChild(p.knownParameterType, colIndex, rowIndex);
            } else {
                JLabel label = p.knownParameterType.getSelectedItemLabel();
                if (label == null) {
                    System.out.println("Label is null when it shouldn't (" + p.bamName + ")");
                }
                insertChild(label != null ? label : p.knownParameterType, colIndex, rowIndex);
            }
            colIndex++;
            insertChild(p.nameLabel, colIndex, rowIndex);
            colIndex++;
            insertChild(p.initialGuessField, colIndex, rowIndex);
            colIndex++;
            insertChild(p.distributionField.distributionCombobox, colIndex, rowIndex);
            colIndex++;
            insertChild(p.distributionField.parameterFieldsPanel, colIndex, rowIndex);
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
        // PriorParDists
        JSONArray json = new JSONArray();
        for (String key : priorParDists.keySet()) {
            JSONObject j = new JSONObject();
            j.put("key", key);
            j.put("priorParDist", priorParDists.get(key).toJSON());
            json.put(j);
        }
        return json;
    }

    public void fromJSON(JSONArray json) {
        for (int k = 0; k < json.length(); k++) {
            JSONObject j = json.getJSONObject(k);
            String key = j.optString("key", "");
            JSONObject priorParDistConfig = j.optJSONObject("priorParDist");
            if (!priorParDists.containsKey(key)) {
                continue;
            }
            priorParDists.get(key).fromJSON(priorParDistConfig);
        }
    }
}
