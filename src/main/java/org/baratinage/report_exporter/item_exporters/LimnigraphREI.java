package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.Limnigraph;
import org.baratinage.utils.Misc;

public class LimnigraphREI implements IReportExporterItem {

  private final String mdStr;
  private ReportExporterPlot plot = null;

  public LimnigraphREI(Limnigraph bamItem) {

    String name = bamItem.bamItemNameField.getText();
    String desc = bamItem.bamItemDescriptionField.getText();

    List<String> md = new ArrayList<>();

    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }

    plot = new ReportExporterPlot(
        "%s_limni_plot".formatted(Misc.sanitizeName(name)),
        T.text("limnigraph"),
        bamItem.limniPlot.plotContainer);

    md.add(plot.getMarkdownPNG("assets"));

    mdStr = String.join("\n", md);
  }

  @Override
  public String getMarkdown() {
    return mdStr;
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
