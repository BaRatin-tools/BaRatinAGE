package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.baratin.RatingCurveCompare;
import org.baratinage.utils.Misc;

public class RatingCurveComparatorREI implements IReportExporterItem {

  private ReportExporterPlot plot = null;
  private final String mdStr;

  public RatingCurveComparatorREI(RatingCurveCompare bamItem) {

    String name = bamItem.bamItemNameField.getText();
    String desc = bamItem.bamItemDescriptionField.getText();

    List<String> md = new ArrayList<>();

    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }

    // bamItem.ratingCurveParent.getBa
    BamItem rcOne = bamItem.rcOne.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text("rc_n", 1),
        rcOne == null ? "" : rcOne.bamItemNameField.getText()));

    BamItem rcTwo = bamItem.rcTwo.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text("rc_n", 2),
        rcTwo == null ? "" : rcTwo.bamItemNameField.getText()));

    plot = new ReportExporterPlot(
        "%s_rc_compare_plot".formatted(Misc.sanitizeName(name)),
        T.text("comparing_rating_curves"),
        bamItem.plotContainer);

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
