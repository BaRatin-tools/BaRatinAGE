package org.baratinage.ui.baratin;

import java.time.LocalDateTime;
import java.util.List;

import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;

public class HydrographPlot extends RowColPanel {
    public void updatePlot(
            LocalDateTime[] dateTime,
            double[] dischargeMaxpost,
            List<double[]> dischargeParamU,
            List<double[]> dischargeTotalU) {

        System.out.println("HydrographPlot");

        Plot plot = new Plot(false, true);

        // PlotItem[] items = limniDataset.getPlotLines();
        // for (PlotItem item : items) {
        // plot.addXYItem(item);
        // }

        // Lg.register(plot, () -> {
        // plot.axisXdate.setLabel(Lg.text("time"));
        // plot.axisY.setLabel(Lg.text("stage_level"));
        // plot.axisYlog.setLabel(Lg.text("stage_level"));
        // });

        // PlotContainer plotContainer = new PlotContainer(plot);

        // plotPanel.clear();
        // plotPanel.appendChild(plotContainer);

        // PlotContainer plotContainer = new PlotContainer(plot);

        // clear();
        // appendChild(plotContainer);
    }
}
