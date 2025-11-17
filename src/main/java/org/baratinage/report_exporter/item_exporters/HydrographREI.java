package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.baratin.Hydrograph;
import org.baratinage.utils.Misc;

public class HydrographREI implements IReportExporterItem {

  private ReportExporterPlot plot = null;
  private final String mdStr;

  public HydrographREI(Hydrograph bamItem) {

    String name = bamItem.bamItemNameField.getText();
    String desc = bamItem.bamItemDescriptionField.getText();

    List<String> md = new ArrayList<>();

    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }

    BamItem rcParent = bamItem.ratingCurveParent.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text(bamItem.ratingCurveParent.TYPE.id),
        rcParent == null ? "" : rcParent.bamItemNameField.getText()));

    BamItem limniPrant = bamItem.limnigraphParent.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text(bamItem.ratingCurveParent.TYPE.id),
        limniPrant == null ? "" : limniPrant.bamItemNameField.getText()));

    plot = new ReportExporterPlot(
        "%s_hydro_plot".formatted(Misc.sanitizeName(name)),
        T.text("hydrograph"),
        bamItem.plotPanel.plotContainer);

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
