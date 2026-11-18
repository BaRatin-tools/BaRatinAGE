package org.baratinage.ui.commons.variable_parameter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Distribution;
import org.baratinage.ui.commons.DistributionField;
import org.baratinage.ui.component.EquationLabel;
import org.baratinage.ui.component.SimpleNumberField;

public class ParameterLabelsAndFields {

  public final JLabel icon = new JLabel();
  public final EquationLabel symbolAndUnit = new EquationLabel(); // FIXME: I should split symbol and unit
  public final JLabel label = new JLabel();
  public final DistributionField field = new DistributionField();
  public final SimpleNumberField initialGuess = new SimpleNumberField();
  public final SimpleNumberField meanField = new SimpleNumberField();
  public final SimpleNumberField uncertaintyField = new SimpleNumberField();

  private boolean autoInitialGuess = false;
  private boolean isEnabled = false;

  public ParameterLabelsAndFields() {
    field.addChangeListener(l -> updateInitialGuessField());

    field.addChangeListener(l -> fireChangeListeners());
    meanField.addChangeListener(l -> fireChangeListeners());
    uncertaintyField.addChangeListener(l -> fireChangeListeners());
    initialGuess.addChangeListener(l -> fireChangeListeners());
  }

  public void setAutoInitialGuess(boolean auto) {
    autoInitialGuess = auto;
    updateInitialGuessField();
    updateEnableState();
  }

  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
    updateEnableState();
  }

  private void updateEnableState() {
    field.setEnabled(isEnabled);
    initialGuess.setEnabled(isEnabled && !autoInitialGuess);
    meanField.setEnabled(isEnabled);
    uncertaintyField.setEnabled(isEnabled);
  }

  private void updateInitialGuessField() {
    if (!autoInitialGuess) {
      return;
    }
    Distribution dist = field.getDistribution();
    if (dist != null) {
      Double median = dist.getMedian();
      if (median != null) {
        initialGuess.setValue(median);
      }
    } else {
      initialGuess.setValue(Double.NaN);
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

}
