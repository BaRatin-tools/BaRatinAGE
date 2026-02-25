package org.baratinage.report_exporter.item_exporters;

import java.util.ArrayList;
import java.util.List;

import org.baratinage.report_exporter.IReportExporterItem;
import org.baratinage.report_exporter.MD;
import org.baratinage.report_exporter.ReportExporterPlot;
import org.baratinage.report_exporter.ReportExporterTools;
import org.baratinage.ui.commons.StructuralErrorModelBamItem;
import org.baratinage.ui.commons.StructuralErrorModelPanel;
import org.baratinage.ui.component.NameSymbolUnit;

public class StructuralErrorREI implements IReportExporterItem {

  private final String mdStr;

  public StructuralErrorREI(StructuralErrorModelBamItem bamItem) {

    String name = bamItem.bamItemNameField.getText();
    String desc = bamItem.bamItemDescriptionField.getText();

    List<String> md = new ArrayList<>();

    md.add(MD.h(3, name));
    if (desc != null && !desc.equals("")) {
      md.add(desc);
    }

    for (int k = 0; k < bamItem.nOutputs; k++) {
      NameSymbolUnit nsu = bamItem.nameSymbolUnits[k];
      md.add("**%s**".formatted(nsu.name() + " (" + nsu.symbol() + ")"));
      StructuralErrorModelPanel semp = bamItem.strucErrModelPanels[k];
      List<String[]> parTable = ReportExporterTools.getParPriorDistStringTable(semp.parameters);
      md.add(MD.rows(parTable, true));
    }

    mdStr = String.join("\n", md);
  }

  @Override
  public String getMarkdown() {
    return mdStr;
  }

  @Override
  public List<ReportExporterPlot> getImages() {
    return new ArrayList<>();
  }

}
