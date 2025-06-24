package org.baratinage.ui.baratin.rating_shifts_happens;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.baratinage.translation.T;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionOverall;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults;
import org.baratinage.ui.component.SimpleComboBox;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.ui.container.TitledPanel;
import org.baratinage.ui.plot.ColorPalette;

public class RatingShiftsHappensResults extends SimpleFlowPanel {

  public final SimpleComboBox colorPaletteSelector;

  private ShiftDetectionResults results = null;

  public RatingShiftsHappensResults() {
    super(true);
    setGap(5);
    setPadding(5);

    colorPaletteSelector = new SimpleComboBox();
    colorPaletteSelector.setEmptyItem(null);
    ColorPalette[] palettes = ColorPalette.values();
    JLabel[] paletteLabels = new JLabel[palettes.length];
    for (int k = 0; k < palettes.length; k++) {
      paletteLabels[k] = new JLabel();
      paletteLabels[k].setText(palettes[k].name());
      paletteLabels[k].setIcon(new ImageIcon(palettes[k].buildIcon(50, 20)));
    }
    colorPaletteSelector.setItems(paletteLabels, true);
    colorPaletteSelector.setSelectedItem(4);
    colorPaletteSelector.addChangeListener(l -> {
      updateResults();
    });
  }

  public void updateResults() {
    if (results == null) {
      return;
    }
    String colorPaletteStr = colorPaletteSelector.getSelectedItemLabel().getText();
    ColorPalette palette = ColorPalette.getPaletteFromString(colorPaletteStr);
    results.setPalette(palette);
    results.updateResults();

  }

  public void setGaugingsBasedDetectionResults(ShiftDetectionOverall shiftDetectionOverall) {
    TabContainer tabContainer = new TabContainer();
    tabContainer.addChangeListener(l -> {
      updateResults();
    });

    results = new ShiftDetectionResults(
        shiftDetectionOverall.getRootRatingShiftDetection(),
        false);

    // Q=f(t) or h=f(t) + shifts
    // results.get
    TitledPanel mainResTab = new TitledPanel(results.mainPlot);
    mainResTab.setText(T.text("periods_and_shifts"));
    tabContainer.addTab(mainResTab);

    TitledPanel tableResTab = new TitledPanel(results.table);
    tableResTab.setText(T.text("parameter_summary_table"));
    tabContainer.addTab(tableResTab);

    TitledPanel gaugingsTab = new TitledPanel(results.gaugings);
    gaugingsTab.setText(T.text("gaugings"));
    tabContainer.addTab(gaugingsTab);

    TitledPanel detailsTab = new TitledPanel(results.intermediateResults);
    detailsTab.setText(T.text("intermediate_results"));
    tabContainer.addTab(detailsTab);

    SimpleFlowPanel optionsPanel = new SimpleFlowPanel();
    optionsPanel.setGap(5);
    JLabel colorPaletteLabel = new JLabel();
    colorPaletteLabel.setText(T.text("color_palette"));

    optionsPanel.addChild(colorPaletteLabel, false);
    optionsPanel.addChild(colorPaletteSelector, false);

    removeAll();
    addChild(tabContainer, true);
    addChild(optionsPanel, false);

    updateResults();
  }

}
