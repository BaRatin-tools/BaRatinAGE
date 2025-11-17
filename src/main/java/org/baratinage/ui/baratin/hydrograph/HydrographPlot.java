package org.baratinage.ui.baratin.hydrograph;

import java.awt.BasicStroke;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JToggleButton;

import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.plot.EditablePlot;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.utils.DateTime;
import org.baratinage.ui.plot.PlotLine;
import org.baratinage.ui.plot.PlotBand;

public class HydrographPlot extends SimpleFlowPanel {
        public final PlotEditor plotEditor;

        private final JToggleButton openPlotEditorBtn;

        public final JCheckBox cropNegativeValuesCB;

        public final PlotContainer plotContainer;

        private final SimpleFlowPanel plotArea;

        public HydrographPlot() {
                super();
                cropNegativeValuesCB = new JCheckBox();
                T.t(this, cropNegativeValuesCB, false, "crop_total_envelop_zero");

                plotEditor = new PlotEditor();

                plotContainer = new PlotContainer();

                plotArea = new SimpleFlowPanel(true);

                openPlotEditorBtn = new JToggleButton();
                openPlotEditorBtn.setIcon(AppSetup.ICONS.EDIT);
                openPlotEditorBtn.addActionListener(l -> {
                        removeAll();
                        if (openPlotEditorBtn.isSelected()) {
                                addChild(plotEditor, false);
                        }
                        addChild(plotArea, true);
                });

                plotContainer.toolsPanel.addChild(openPlotEditorBtn, false);

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

                plotContainer.setPlot(plot);

                boolean firstDraw = plotEditor.getEditablePlot() == null;

                plotEditor.addEditablePlot(plot);
                plotEditor.addEditablePlotItem("maxpost", "maxpost", mpLine);
                if (includeLimniBand) {
                        plotEditor.addEditablePlotItem("limniu", "limniu", limniBand);
                }
                plotEditor.addEditablePlotItem("paramu", "paramu", paramBand);
                plotEditor.addEditablePlotItem("totalu", "totalu", totalBand);

                plotArea.removeAll();
                plotArea.addChild(plotContainer, true);
                plotArea.addChild(cropNegativeValuesCB, false, 5);

                removeAll();
                if (openPlotEditorBtn.isSelected()) {
                        addChild(plotEditor, false);
                }
                addChild(plotArea, true);

                if (firstDraw) {
                        setDefaultPlotEditorConfig();
                }

                plotEditor.updateEditor();
                plotEditor.saveAsDefault(false);

        }

        private final void setDefaultPlotEditorConfig() {

                EditablePlotItem maxpost = plotEditor.getEditablePlotItem("maxpost");
                EditablePlotItem paramu = plotEditor.getEditablePlotItem("paramu");
                EditablePlotItem limniu = plotEditor.getEditablePlotItem("limniu");
                EditablePlotItem totalu = plotEditor.getEditablePlotItem("totalu");

                boolean includeLimniBand = limniu != null;

                maxpost.setLabel(T.text("lgd_discharge_maxpost"));
                maxpost.setLinePaint(AppSetup.COLORS.PLOT_LINE);
                maxpost.setLineWidth(2);

                paramu.setLabel(T.text(includeLimniBand ? "lgd_discharge_limni_param_u" : "lgd_discharge_param_u"));
                paramu.setFillPaint(AppSetup.COLORS.RATING_CURVE_PARAM_UNCERTAINTY);

                if (includeLimniBand) {
                        limniu.setLabel(T.text("lgd_discharge_limni_u"));
                        limniu.setFillPaint(AppSetup.COLORS.LIMNIGRAPH_STAGE_UNCERTAINTY);
                }

                totalu.setLabel(T.text("lgd_discharge_total_u"));
                totalu.setFillPaint(AppSetup.COLORS.RATING_CURVE_TOTAL_UNCERTAINTY);

                // plot axis legend items order
                EditablePlot p = plotEditor.getEditablePlot();

                p.updateLegendItems("maxpost", "paramu", includeLimniBand ? "limniu" : null, "totalu");

                p.setXAxisLabel(T.text("time"));
                p.setYAxisLabel(T.text("discharge") + " [m3/s]");

        }
}
