package org.baratinage.ui.baratin.gaugings;

import java.time.LocalDateTime;

public class GaugingData {
  public LocalDateTime dataTime = null;
  public Double stage = null;
  public Double discharge = null;
  public Double stageUncertainty = null; // absolute in m
  public Double dischargeUncertainty = null; // relative in percent
  public Boolean isActive = null;
}
