package org.baratinage.ui.bam;

import java.util.HashMap;

import org.baratinage.ui.plot.PlotItem;

public interface IPlotDataProvider {
    public HashMap<String, PlotItem> getPlotItems();
}
