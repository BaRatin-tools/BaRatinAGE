package org.baratinage.ui.baratin.gaugings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GaugingDataset {

  // private static final String STAGE = "stage";
  // private static final String DISCHARGE = "discharge";
  // private static final String DISCHARGE_U = "dischargePercentUncertainty";
  // private static final String DISCHARGE_STD = "dischargeStd";
  // private static final String DISCHARGE_LOW = "dischargeLow";
  // private static final String DISCHARGE_HIGH = "dischargeHigh";
  // private static final String STAGE_U = "stageAbsoluteUncertainty";
  // private static final String STAGE_STD = "stageStd";
  // private static final String STAGE_LOW = "stageLow";
  // private static final String STAGE_HIGH = "stageHigh";
  // private static final String STATE = "active";
  // private static final String DATETIME = "dateTime";

  /**
   * contains the gaugings in an array
   * can build an abstract dataset representation of the gaugings for
   * saving/loading
   * 
   */

  // private final AbstractDataset dataset;
  private final List<GaugingData> gaugings;
  private boolean hasStageUncertainty;
  private boolean hasDateTime;

  // public GaugingDataset(AbstractDataset gaugingDataset) {
  // dataset = gaugingDataset;
  // gaugings = new ArrayList<>();
  // }

  public GaugingDataset(
      String name,
      double[] stage,
      double[] discharge,
      double[] dischargePercentUncertainty,
      boolean[] active,
      LocalDateTime[] datetime,
      double[] stageAbsoluteUncertainty) throws IllegalArgumentException {

    gaugings = new ArrayList<>();
    int n = stage.length;

    if (discharge == null) {
      throw new IllegalArgumentException("The discharge array must be non null");
    }
    if (discharge.length != n) {
      throw new IllegalArgumentException("The discharge array has not the correct length (%d != %d)"
          .formatted(discharge.length, n));

    }
    if (dischargePercentUncertainty == null) {
      throw new IllegalArgumentException("The discharge uncertainty array must be non null");

    }
    if (dischargePercentUncertainty.length != n) {
      throw new IllegalArgumentException("The discharge uncertainty array has not the correct length (%d != %d)"
          .formatted(dischargePercentUncertainty.length, n));
    }

    if (active != null && active.length != n) {
      throw new IllegalArgumentException("The active array has not the correct length (%d != %d)"
          .formatted(active.length, n));
    }

    if (datetime != null && datetime.length != n) {
      throw new IllegalArgumentException("The datetime array has not the correct length (%d != %d)"
          .formatted(datetime.length, n));
    }

    if (stageAbsoluteUncertainty != null && stageAbsoluteUncertainty.length != n) {
      throw new IllegalArgumentException("The stage uncertainty array has not the correct length (%d != %d)"
          .formatted(stageAbsoluteUncertainty.length, n));
    }

    hasStageUncertainty = stageAbsoluteUncertainty != null;
    hasDateTime = datetime != null;

    for (int k = 0; k < n; k++) {
      GaugingData g = new GaugingData();
      g.stage = stage[k];
      g.discharge = discharge[k];
      g.dischargeUncertainty = dischargePercentUncertainty[k];
      g.isActive = active == null ? true : active[k];
      g.dataTime = hasDateTime ? datetime[k] : null;
      g.stageUncertainty = hasStageUncertainty ? stageAbsoluteUncertainty[k] : null;
      gaugings.add(g);
    }

  }

}
