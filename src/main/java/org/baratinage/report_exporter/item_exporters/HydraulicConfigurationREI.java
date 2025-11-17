package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.report_exporter.ReportExporterTools;
import org.baratinage.translation.T;
import org.baratinage.ui.baratin.HydraulicConfiguration;
import org.baratinage.ui.baratin.hydraulic_control.OneHydraulicControl;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.plot.PlotContainer;
import org.baratinage.utils.Misc;

public class HydraulicConfigurationREI implements IReportExporterItem {

  private String md = "";
  private ReportExporterPlot plot = null;

  public HydraulicConfigurationREI(HydraulicConfiguration hc) {

    // component name and description
    String name = hc.bamItemNameField.getText();
    String desc = hc.bamItemDescriptionField.getText();

    // component plots
    PlotContainer pc = hc.priorRatingCurve.resultsPanel.ratingCurvePlot.plotContainer;

    plot = new ReportExporterPlot(
        "%s_prior_plot".formatted(Misc.sanitizeName(name)),
        T.text("prior_rating_curve"),
        pc);

    // hydraulic controls
    List<OneHydraulicControl> controls = hc.hydraulicControls.getHydraulicControls();
    List<List<String[]>> controlsPhysicalTables = new ArrayList<>();
    List<List<String[]>> controlsKACTables = new ArrayList<>();
    for (OneHydraulicControl control : controls) {
      controlsPhysicalTables.add(getPhysicalParametersTable(control));
      controlsKACTables.add(getKACParametersTable(control));
    }

    List<String> md = new ArrayList<>();

    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }
    md.add(MD.h(4, T.text("prior_parameter_specification")));
    for (int k = 0; k < controls.size(); k++) {
      md.add(MD.h(5, T.text("control_nbr", k + 1)));
      md.add("%s: %s\n".formatted(T.text("description"), controls.get(k).descriptionField.getText()));
      List<String[]> phyTable = controlsPhysicalTables.get(k);
      if (phyTable != null) {
        md.add("**%s**\n".formatted(T.text(controls.get(k).getPhysicalControlTypeKey())));
        md.add(MD.rows(phyTable, true));
      }
      md.add("**&kappa;, a, c**");
      md.add(MD.rows(controlsKACTables.get(k), true));
    }

    md.add(MD.h(3, T.text("prior_rating_curve")));
    md.add(plot.getMarkdownPNG("assets"));

    this.md = String.join("\n", md);

  }

  private static List<String[]> getPhysicalParametersTable(OneHydraulicControl control) {
    List<ParameterPriorDistSimplified> physicalParamters = control.getPhysicalParPriorDist();
    return ReportExporterTools.getParPriorDistSimplifiedStringTable(physicalParamters);
  }

  private static List<String[]> getKACParametersTable(OneHydraulicControl control) {
    List<ParameterPriorDist> kacParameters = control.getBKACParPriorDist();
    return ReportExporterTools.getParPriorDistStringTable(kacParameters);
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
