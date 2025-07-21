package org.baratinage.ui.baratin.hydrograph;

import java.awt.BasicStroke;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JCheckBox;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.DateTime;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;

public class HydrographPlot extends SimpleFlowPanel {

        public final JCheckBox cropNegativeValuesCB;

        public HydrographPlot() {
                super(true);
                cropNegativeValuesCB = new JCheckBox();
                T.t(this, cropNegativeValuesCB, false, "crop_total_envelop_zero");
        }

        public void updatePlot(
                        LocalDateTime[] dateTime,
                        double[] dischargeMaxpost,
                        List<double[]> dischargelimniU,
                        List<double[]> dischargeParamU,
                        List<double[]> dischargeTotalU) {

                boolean includeLimniBand = dischargelimniU != null;

                Plot plot = new Plot(true, true);

                // Second[] time = PlotItem.localDateTimeToSecond(dateTime);
                double[] time = DateTime.dateTimeToDoubleArrayMilliseconds(dateTime);

                PlotLine mpLine = new PlotLine(
                                "Maxpost",
                                time,
                                dischargeMaxpost,
                                AppSetup.COLORS.PLOT_LINE,
                                new BasicStroke(2));

                PlotBand paramBand = new PlotBand(
                                "Parametric uncertainty",
                                time,
                                dischargeParamU.get(0),
                                dischargeParamU.get(1),
                                false,
                                AppSetup.COLORS.RATING_CURVE_PARAM_UNCERTAINTY);

                PlotBand limniBand = new PlotBand(
                                "Limnigraph uncertainty",
                                time,
                                includeLimniBand ? dischargelimniU.get(0) : dischargeParamU.get(0),
                                includeLimniBand ? dischargelimniU.get(1) : dischargeParamU.get(1),
                                false,
                                AppSetup.COLORS.LIMNIGRAPH_STAGE_UNCERTAINTY);

                PlotBand totalBand = new PlotBand(
                                "Total uncertainty",
                                time,
                                dischargeTotalU.get(0),
                                dischargeTotalU.get(1),
                                false,
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
                addChild(cropNegativeValuesCB, false, 5);
        }
}
