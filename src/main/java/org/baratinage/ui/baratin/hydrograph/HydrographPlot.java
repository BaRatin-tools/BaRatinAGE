package org.baratinage.ui.baratin.hydrograph;

import java.awt.BasicStroke;
import java.time.LocalDateTime;
import java.util.List;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotItem;
import org.baratinage.ui.plot.PlotTimeSeriesBand;
import org.baratinage.ui.plot.PlotTimeSeriesLine;
import org.jfree.data.time.Second;

public class HydrographPlot extends SimpleFlowPanel {
        public void updatePlot(
                        LocalDateTime[] dateTime,
                        double[] dischargeMaxpost,
                        List<double[]> dischargelimniU,
                        List<double[]> dischargeParamU,
                        List<double[]> dischargeTotalU) {

                boolean includeLimniBand = dischargelimniU != null;

                Plot plot = new Plot(true, true);

                Second[] time = PlotItem.localDateTimeToSecond(dateTime);

                PlotTimeSeriesLine mpLine = new PlotTimeSeriesLine(
                                "Maxpost",
                                time,
                                dischargeMaxpost,
                                AppSetup.COLORS.PLOT_LINE,
                                new BasicStroke(2));

                PlotTimeSeriesBand paramBand = new PlotTimeSeriesBand(
                                "Parametric uncertainty",
                                time,
                                dischargeParamU.get(0),
                                dischargeParamU.get(1),
                                AppSetup.COLORS.RATING_CURVE_PARAM_UNCERTAINTY);

                PlotTimeSeriesBand limniBand = new PlotTimeSeriesBand(
                                "Limnigraph uncertainty",
                                time,
                                includeLimniBand ? dischargelimniU.get(0) : dischargeParamU.get(0),
                                includeLimniBand ? dischargelimniU.get(1) : dischargeParamU.get(1),
                                AppSetup.COLORS.LIMNIGRAPH_STAGE_UNCERTAINTY);

                PlotTimeSeriesBand totalBand = new PlotTimeSeriesBand(
                                "Total uncertainty",
                                time,
                                dischargeTotalU.get(0),
                                dischargeTotalU.get(1),
                                AppSetup.COLORS.RATING_CURVE_TOTAL_UNCERTAINTY);

                plot.addXYItem(totalBand);
                plot.addXYItem(paramBand);
                if (includeLimniBand) {
                        plot.addXYItem(limniBand);
                }
                plot.addXYItem(mpLine);

                T.t(this, () -> {
                        mpLine.setLabel(T.text("lgd_discharge_maxpost"));
                        paramBand.setLabel(T.text(
                                        includeLimniBand ? "lgd_discharge_limni_param_u" : "lgd_discharge_param_u"));
                        totalBand.setLabel(T.text("lgd_discharge_total_u"));
                        if (includeLimniBand) {
                                limniBand.setLabel(T.text("lgd_discharge_limni_u"));
                        }
                        plot.axisXdate.setLabel(T.text("time"));
                        plot.axisY.setLabel(T.text("discharge") + " [m3/s]");
                        plot.axisYlog.setLabel(T.text("discharge") + " [m3/s]");
                });

                PlotContainer plotContainer = new PlotContainer(plot);
                T.updateHierarchy(this, plotContainer);

                removeAll();
                addChild(plotContainer, true);
        }
}
