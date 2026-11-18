package org.baratinage.ui.commons.variable_parameter;

import java.util.List;

import javax.swing.event.ChangeListener;

public interface PeriodSetsSource {
  public List<PeriodSet> getPeriodSets();

  void addChangeListener(ChangeListener l);

  void removeChangeListener(ChangeListener l);
}
