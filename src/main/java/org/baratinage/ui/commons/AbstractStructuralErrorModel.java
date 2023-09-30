package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.ui.bam.IStructuralErrorModels;
import org.baratinage.ui.container.GridPanel;

public abstract class AbstractStructuralErrorModel extends GridPanel implements IStructuralErrorModels {
    public AbstractStructuralErrorModel() {
        setColWeight(3, 1);
        setGap(5);
        setPadding(5);

        JLabel parameterNameLabel = new JLabel();
        parameterNameLabel.setText("Nom");

        JLabel initialGuessLabel = new JLabel();
        initialGuessLabel.setText("Valeure initiale");

        JLabel distributionLabel = new JLabel();
        distributionLabel.setText("Distribution");

        JLabel distributionParametersLabel = new JLabel();
        distributionParametersLabel.setText("Paramètres de la distribution");

        insertChild(parameterNameLabel, 0, 0);
        insertChild(initialGuessLabel, 1, 0);
        insertChild(distributionLabel, 2, 0);
        insertChild(distributionParametersLabel, 3, 0);

    }

    public abstract void applyDefaultConfig();

    public abstract StructuralErrorModel getStructuralErrorModel();

    public abstract void setFromParameters(Parameter[] parameters);

    private List<ChangeListener> changeListenerers = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListenerers.add(l);
    }

    protected void fireChangeListener() {
        for (ChangeListener l : changeListenerers) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

}
