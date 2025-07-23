package org.baratinage.ui.baratin.limnigraph;

import javax.swing.JToggleButton;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.plot.EditablePlot;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.Plot;
import org.baratinage.ui.plot.PlotBand;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.ui.plot.PlotLine;

public class LimnigraphPlot extends SimpleFlowPanel {

  public final PlotEditor plotEditor;

  private final JToggleButton openPlotEditorBtn;

  private final PlotContainer plotContainer;

  public LimnigraphPlot() {
    plotEditor = new PlotEditor();

    plotContainer = new PlotContainer();

    openPlotEditorBtn = new JToggleButton();
    openPlotEditorBtn.setIcon(AppSetup.ICONS.EDIT);
    openPlotEditorBtn.addActionListener(l -> {
      removeAll();
      if (openPlotEditorBtn.isSelected()) {
        addChild(plotEditor, false);
      }
      addChild(plotContainer, true);
    });

    plotContainer.toolsPanel.addChild(openPlotEditorBtn, false);

  }

  public void updatePlot(LimnigraphDataset limniDataset) {

    Plot plot = new Plot(true, true);

    PlotBand envelop = limniDataset.hasStageErrMatrix() ? limniDataset.getPlotEnv() : null;
    if (envelop != null) {
      plot.addXYItem(envelop);
    }
    PlotLine limnigraph = limniDataset.getPlotLine();
    plot.addXYItem(limnigraph);

    plotContainer.setPlot(plot);

    boolean firstDraw = plotEditor.getEditablePlot() == null;

    plotEditor.addEditablePlotItem("stage", "stage", limnigraph);
    if (envelop != null) {
      plotEditor.addEditablePlotItem("u", "stage_errors", envelop);
    }
    plotEditor.addEditablePlot(plot);

    removeAll();
    if (openPlotEditorBtn.isSelected()) {
      addChild(plotEditor, false);
    }
    addChild(plotContainer, true);

    if (firstDraw) {
      setDefaultPlotEditorConfig();
    }

    plot.update();
  }

  private void setDefaultPlotEditorConfig() {

    EditablePlotItem envelop = plotEditor.getEditablePlotItem("u");
    if (envelop != null) {
      envelop.setLabel(T.text("stage_uncertainty"));
      envelop.setFillPaint(AppSetup.COLORS.LIMNIGRAPH_STAGE_UNCERTAINTY);
    }

    EditablePlotItem limnigraph = plotEditor.getEditablePlotItem("stage");
    if (limnigraph != null) {
      limnigraph.setLabel(T.text("limnigraph"));
      limnigraph.setLinePaint(AppSetup.COLORS.PLOT_LINE);
      limnigraph.setLineWidth(2);
    }

    // plot axis legend items order
    EditablePlot p = plotEditor.getEditablePlot();

    p.updateLegendItems("stage", envelop != null ? "u" : null);

    p.setXAxisLabel(T.text("time"));
    p.setYAxisLabel(T.text("stage") + " [m]");

    plotEditor.updateEditor();
    plotEditor.saveAsDefault(false);

  }
}
