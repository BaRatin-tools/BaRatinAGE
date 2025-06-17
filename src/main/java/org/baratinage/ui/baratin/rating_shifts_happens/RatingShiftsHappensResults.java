package org.baratinage.ui.baratin.rating_shifts_happens;

import org.baratinage.translation.T;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionOverall;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.TabContainer;
import org.baratinage.ui.container.TitledPanel;

public class RatingShiftsHappensResults extends SimpleFlowPanel {

  public RatingShiftsHappensResults() {

  }

  public void setGaugingsBasedDetectionResults(ShiftDetectionOverall shiftDetectionOverall) {
    TabContainer tabContainer = new TabContainer();

    ShiftDetectionResults results = new ShiftDetectionResults(
        shiftDetectionOverall.getRootRatingShiftDetection(),
        false);

    // Q=f(t) or h=f(t) + shifts
    // results.get
    TitledPanel mainResTab = new TitledPanel(results.getMainResultPanel());
    mainResTab.setText(T.text("periods_and_shifts"));
    tabContainer.addTab(mainResTab);

    TitledPanel tableResTab = new TitledPanel(results.getShiftsDataTablePlotItems());
    tableResTab.setText(T.text("parameter_summary_table"));
    tabContainer.addTab(tableResTab);

    TitledPanel gaugingsTab = new TitledPanel(results.getGaugingsDatasetsPanel());
    gaugingsTab.setText(T.text("gaugings"));
    tabContainer.addTab(gaugingsTab);

    TitledPanel detailsTab = new TitledPanel(results.getDetailedResultsPanel());
    detailsTab.setText(T.text("intermediate_results"));
    tabContainer.addTab(detailsTab);

    removeAll();
    addChild(tabContainer);
  }

}
