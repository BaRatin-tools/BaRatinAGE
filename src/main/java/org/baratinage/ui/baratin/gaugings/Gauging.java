package org.baratinage.ui.baratin.gaugings;

public class Gauging {
    public double stage;
    public double discharge;
    public double dischargeUncertainty;
    public boolean activeState;

    Gauging(double stage, double discharge, double dischargeUncertainty, boolean activeState) {
        this.stage = stage;
        this.discharge = discharge;
        this.dischargeUncertainty = dischargeUncertainty;
        this.activeState = activeState;
    }
}
