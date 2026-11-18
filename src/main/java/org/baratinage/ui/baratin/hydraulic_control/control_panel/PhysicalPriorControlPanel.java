package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import org.baratinage.ui.commons.ParameterPriorDistSimplified;

public abstract class PhysicalPriorControlPanel extends PriorControlPanel {

  protected PhysicalPriorControlPanel(int nColumns, int nRows, String equation) {
    super(nColumns, nRows, equation);
  }

  protected abstract ParameterPriorDistSimplified getKB();

  protected abstract ParameterPriorDistSimplified getExponent();

  protected abstract Double[] toAMeanAndStd();

}
