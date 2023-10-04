package org.baratinage.ui.baratin;

import java.awt.BasicStroke;
import java.time.LocalDateTime;
import java.util.List;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotTimeSeriesBand;
import org.baratinage.ui.plot.PlotTimeSeriesLine;
import org.jfree.data.time.Second;

public class HydrographPlot extends RowColPanel {
        public void updatePlot(
                        LocalDateTime[] dateTime,
                        double[] dischargeMaxpost,
                        List<double[]> dischargeParamU,
                        List<double[]> dischargeTotalU) {

                System.out.println("HydrographPlot");

                Plot plot = new Plot(true, true);

                Second[] time = PlotItem.localDateTimeToSecond(dateTime);

                PlotTimeSeriesLine mpLine = new PlotTimeSeriesLine(
                                "Maxpost",
                                time,
                                dischargeMaxpost,
                                AppConfig.AC.PLOT_LINE_COLOR,
                                new BasicStroke(2));

                PlotTimeSeriesBand paramBand = new PlotTimeSeriesBand(
                                "Parametric uncertainty",
                                time,
                                dischargeParamU.get(0),
                                dischargeParamU.get(1),
                                AppConfig.AC.RATING_CURVE_PARAM_UNCERTAINTY_COLOR);

                PlotTimeSeriesBand totalBand = new PlotTimeSeriesBand(
                                "Total uncertainty",
                                time,
                                dischargeTotalU.get(0),
                                dischargeTotalU.get(1),
                                AppConfig.AC.RATING_CURVE_TOTAL_UNCERTAINTY_COLOR);

                plot.addXYItem(totalBand);
                plot.addXYItem(paramBand);
                plot.addXYItem(mpLine);

                T.t(mpLine, "discharge_maxpost");
                T.t(paramBand, "discharge_param_u");
                T.t(totalBand, "discharge_total_u");

                T.t(plot, (plt) -> {
                        plt.axisXdate.setLabel(T.text("time"));
                        plt.axisY.setLabel(T.text("discharge"));
                        plt.axisYlog.setLabel(T.text("discharge"));

                });

                PlotContainer plotContainer = new PlotContainer(plot);

                clear();
                appendChild(plotContainer);
        }
}
