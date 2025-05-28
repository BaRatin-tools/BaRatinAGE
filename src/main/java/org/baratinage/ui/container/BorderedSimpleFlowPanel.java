package org.baratinage.ui.container;

import javax.swing.BorderFactory;

import org.baratinage.AppSetup;

public class BorderedSimpleFlowPanel extends SimpleFlowPanel {
  public BorderedSimpleFlowPanel() {
    this(false);
  }

  public BorderedSimpleFlowPanel(boolean isVertical) {
    super(isVertical);
    setGap(5);
    setPadding(0, 5, 0, 0);
    setBorder(
        BorderFactory.createMatteBorder(
            0, 1, 0, 0,
            AppSetup.COLORS.DEFAULT_FG_LIGHT));

  }
}
