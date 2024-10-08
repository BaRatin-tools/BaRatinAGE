package org.baratinage.ui.baratin.baratin_qfh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.baratin.baratin_qfh.QFHPreset.RatingCurveEquationParameter;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.utils.ConsoleLogger;

public class QFHPriors extends GridPanel implements IPriors, ChangeListener {

    private final HashMap<String, QFHPriorParameterDist> PriorParDists;
    private final HashSet<String> usedParNames;

    private final List<JLabel> headers;

    public QFHPriors() {
        super();
        PriorParDists = new HashMap<>();
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
        for (RatingCurveEquationParameter p : preset.parameters()) {
            QFHPriorParameterDist priorParDist = new QFHPriorParameterDist(p.symbole());
            priorParDist.addChangeListener(this);
            priorParDist.setParameterType(p.type());
            priorParDist.knownParameterType.setEnabled(false);
            PriorParDists.put(p.symbole(), priorParDist);
            usedParNames.add(p.symbole());
        }
        updatePanel();
    }

    public void updateUsedParNames(HashSet<String> newUsedParNames) {
        usedParNames.clear();
        for (String parName : newUsedParNames) {
            if (!PriorParDists.containsKey(parName)) {
                QFHPriorParameterDist priorParDist = new QFHPriorParameterDist(parName);
                priorParDist.addChangeListener(this);
                PriorParDists.put(parName, priorParDist);
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
            QFHPriorParameterDist p = PriorParDists.get(parName);
            if (p == null) {
                ConsoleLogger.error("QFHPriorParameterDist not found though it should exist!");
                continue;
            }
            if (p.knownParameterType.isEnabled()) {
                insertChild(p.knownParameterType, colIndex, rowIndex);
            } else {
                insertChild(p.knownParameterType.getSelectedItemLabel(), colIndex, rowIndex);
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
        for (QFHPriorParameterDist QFHPriorParDist : PriorParDists.values()) {
            QFHPriorParDist.removeChangeListener(this);
        }
        PriorParDists.clear();
        usedParNames.clear();
    }

    @Override
    public Parameter[] getParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParameters'");
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

}
