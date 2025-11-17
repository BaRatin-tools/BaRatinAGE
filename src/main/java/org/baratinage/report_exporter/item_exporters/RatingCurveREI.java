package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.report_exporter.ReportExporterTools;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
import org.baratinage.ui.baratin.RatingCurve;
import org.baratinage.ui.component.DataTable;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.Misc;

public class RatingCurveREI implements IReportExporterItem {

  List<ReportExporterPlot> plots = new ArrayList<>();
  private final String mdStr;

  public RatingCurveREI(RatingCurve bamItem) {

    String name = bamItem.bamItemNameField.getText();
    String desc = bamItem.bamItemDescriptionField.getText();

    List<String> md = new ArrayList<>();

    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }

    // --------------------------------------------------------------
    // parents
    md.add(MD.h(4, T.text("parent_components")));

    BamItem hcParent = bamItem.hydrauConfParent.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text(bamItem.hydrauConfParent.TYPE.id),
        hcParent == null ? "" : hcParent.bamItemNameField.getText()));

    BamItem gParent = bamItem.gaugingsParent.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text(bamItem.gaugingsParent.TYPE.id),
        gParent == null ? "" : gParent.bamItemNameField.getText()));

    BamItem seParent = bamItem.structErrorParent.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text(bamItem.structErrorParent.TYPE.id),
        seParent == null ? "" : seParent.bamItemNameField.getText()));

    // --------------------------------------------------------------
    // main rc plot
    md.add(MD.h(4, T.text("chart")));

    ReportExporterPlot rcPlot = new ReportExporterPlot(
        "%s_rc_plot".formatted(Misc.sanitizeName(name)),
        T.text("rating_curve"),
        bamItem.resultsPanel.ratingCurvePlot.plotContainer);
    plots.add(rcPlot);
    md.add(rcPlot.getMarkdownPNG("assets"));

    // --------------------------------------------------------------
    // equation
    md.add(MD.h(4, T.text("equation")));

    md.add(MD.code("", bamItem.resultsPanel.rcEquation.getEquationString()));

    // --------------------------------------------------------------
    // densities
    md.add(MD.h(4, T.text("parameter_densities")));

    List<String[]> densityPlots = new ArrayList<>();

    int nCtrl = bamItem.resultsPanel.paramDensityPlots.plotContainers.size() / 4 - 1;
    for (int k = 0; k < nCtrl; k++) {
      String[] p = new String[4];
      for (int i = 0; i < 4; i++) {
        PlotContainer pc = bamItem.resultsPanel.paramDensityPlots.plotContainers.get(k * 4 + i);
        EstimatedParameterWrapper par = bamItem.resultsPanel.paramDensityPlots.estimatedParameters.get(k * 4 + i);
        ReportExporterPlot densityPlot = new ReportExporterPlot(
            "%s_%s_density_plot".formatted(Misc.sanitizeName(name), par.symbol),
            T.text("parameter_densities"),
            pc, 200, 150);
        plots.add(densityPlot);
        p[i] = densityPlot.getMarkdownPNG("assets");
      }
      densityPlots.add(p);
    }
    String[] p1 = new String[4];
    for (int i = 0; i < 4; i++) {
      PlotContainer pc = bamItem.resultsPanel.paramDensityPlots.plotContainers.get(nCtrl * 4 + i);
      String n = i == 3 ? "legend"
          : bamItem.resultsPanel.paramDensityPlots.estimatedParameters.get(nCtrl * 4 + i).symbol;
      ReportExporterPlot densityPlot = new ReportExporterPlot(
          "%s_%s_density_plot".formatted(Misc.sanitizeName(name), n),
          T.text("parameter_densities"),
          pc, 200, 150);
      plots.add(densityPlot);
      p1[i] = densityPlot.getMarkdownPNG("assets");
    }
    densityPlots.add(p1);

    md.add(MD.rows(densityPlots, false));

    // --------------------------------------------------------------
    // table of parameters
    md.add(MD.h(4, T.text("parameter_summary_table")));

    DataTable table = bamItem.resultsPanel.paramSummaryTable;
    md.add(MD.rows(ReportExporterTools.getDataTableRows(table.table), true));

    // --------------------------------------------------------------
    // traces
    md.add(MD.h(4, T.text("mcmc_results")));

    List<String[]> tracePlots = new ArrayList<>();
    for (int i = 0; i < nCtrl; i++) {
      String[] p = new String[4];
      for (int j = 0; j < 4; j++) {
        PlotContainer pc = bamItem.resultsPanel.mcmcResultPanel.paramTracePlots.plotContainers.get(i * 4 + j);
        EstimatedParameterWrapper par = bamItem.resultsPanel.mcmcResultPanel.paramTracePlots.estimatedParameters
            .get(i * 4 + j);
        ReportExporterPlot tracePlot = new ReportExporterPlot(
            "%s_%s_trace_plot".formatted(Misc.sanitizeName(name), par.symbol),
            T.text("mcmc_results"),
            pc, 200, 150);
        plots.add(tracePlot);
        p[j] = tracePlot.getMarkdownPNG("assets");
      }
      tracePlots.add(p);
    }

    String[] p = new String[4];
    for (int j = 0; j < 4; j++) {
      PlotContainer pc = bamItem.resultsPanel.mcmcResultPanel.paramTracePlots.plotContainers.get(nCtrl * 4 + j);
      String n = j == 3 ? "legend"
          : bamItem.resultsPanel.mcmcResultPanel.paramTracePlots.estimatedParameters.get(nCtrl * 4 + j).symbol;
      ReportExporterPlot tracePlot = new ReportExporterPlot(
          "%s_%s_trace_plot".formatted(Misc.sanitizeName(name), n),
          T.text("mcmc_results"),
          pc, 200, 150);
      plots.add(tracePlot);
      p[j] = tracePlot.getMarkdownPNG("assets");
    }
    tracePlots.add(p);

    md.add(MD.rows(tracePlots, false));

    mdStr = String.join("\n", md);

  }

  @Override
  public String getMarkdown() {
    return mdStr;
  }

  @Override
  public List<ReportExporterPlot> getImages() {
    return plots;
  }

}
