package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.report_exporter.ReportExporterTools;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.baratin.RatingCurve;
import org.baratinage.ui.component.DataTable;
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

    ReportExporterPlot eqPlot = new ReportExporterPlot(
        "%s_eq_plot".formatted(Misc.sanitizeName(name)),
        T.text("equation"),
        bamItem.resultsPanel.rcEquation.niceEqLbl);
    plots.add(eqPlot);
    md.add(eqPlot.getMarkdownPNG("assets"));

    // --------------------------------------------------------------
    // densities
    md.add(MD.h(4, T.text("parameter_densities")));

    ReportExporterPlot densityPlot = new ReportExporterPlot(
        "%s_density_plot".formatted(Misc.sanitizeName(name)),
        T.text("posterior_density"),
        bamItem.resultsPanel.paramDensityPlots.getPlot());
    plots.add(densityPlot);
    md.add(densityPlot.getMarkdownPNG("assets"));

    // --------------------------------------------------------------
    // table of parameters
    md.add(MD.h(4, T.text("parameter_summary_table")));

    DataTable table = bamItem.resultsPanel.paramSummaryTable;
    md.add(MD.rows(ReportExporterTools.getDataTableRows(table.table), true));

    // --------------------------------------------------------------
    // traces
    md.add(MD.h(4, T.text("mcmc_results")));

    ReportExporterPlot tracePlot = new ReportExporterPlot(
        "%s_mcmc_results_plot".formatted(Misc.sanitizeName(name)),
        T.text("mcmc_results"),
        bamItem.resultsPanel.mcmcResultPanel.paramTracePlots.getPlot());
    plots.add(tracePlot);
    md.add(tracePlot.getMarkdownPNG("assets"));

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
