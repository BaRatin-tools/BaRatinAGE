package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.baratin.RatingShiftHappens;
import org.baratinage.ui.baratin.rating_shifts_happens.gaugings.ShiftDetectionResults;
import org.baratinage.utils.Misc;

public class RatingShiftDetectorREI implements IReportExporterItem {

  private final List<ReportExporterPlot> plots = new ArrayList<>();
  private final String mdStr;

  public RatingShiftDetectorREI(RatingShiftHappens bamItem) {

    String name = bamItem.bamItemNameField.getText();
    String desc = bamItem.bamItemDescriptionField.getText();

    List<String> md = new ArrayList<>();

    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }

    BamItem hcParent = bamItem.hydrauConfParent.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text(bamItem.hydrauConfParent.TYPE.id),
        hcParent == null ? "" : hcParent.bamItemNameField.getText()));

    BamItem gParent = bamItem.gaugingsParent.getCurrentBamItem();
    md.add("**%s** : _%s_\n".formatted(
        T.text(bamItem.gaugingsParent.TYPE.id),
        gParent == null ? "" : gParent.bamItemNameField.getText()));

    ShiftDetectionResults results = bamItem.ratingShiftResults.results;
    if (results != null) {
      ReportExporterPlot mainPlot = new ReportExporterPlot(
          "%s_shifts_plot".formatted(Misc.sanitizeName(name)),
          T.text("rating_shift_happens"),
          results.mainPlot.mainPlot);
      md.add(mainPlot.getMarkdownPNG("assets"));
      plots.add(mainPlot);
      ReportExporterPlot gaugingPlot = new ReportExporterPlot(
          "%s_gaugings_plot".formatted(Misc.sanitizeName(name)),
          T.text("gaugings"),
          results.gaugings.plotContainer);
      md.add(gaugingPlot.getMarkdownPNG("assets"));
      plots.add(gaugingPlot);
    }

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
