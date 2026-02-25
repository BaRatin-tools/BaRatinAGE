package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.Gaugings;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.Misc;

public class GaugingsREI implements IReportExporterItem {

  private ReportExporterPlot plot = null;
  private String md = "";

  public GaugingsREI(Gaugings gaugings) {
    PlotContainer pc = gaugings.plotPanel.plotContainer;

    String name = gaugings.bamItemNameField.getText();
    String desc = gaugings.bamItemDescriptionField.getText();

    plot = new ReportExporterPlot(
        "%s_prior_plot".formatted(Misc.sanitizeName(name)),
        T.text("prior_rating_curve"),
        pc);

    List<String> md = new ArrayList<>();
    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }
    md.add(plot.getMarkdownPNG("assets"));

    this.md = String.join("\n", md);
  }

  @Override
  public String getMarkdown() {
    return md;
  }

  @Override
  public List<ReportExporterPlot> getImages() {
    List<ReportExporterPlot> plots = new ArrayList<>();
    if (plot != null) {
      plots.add(plot);
    }
    return plots;
  }
}
