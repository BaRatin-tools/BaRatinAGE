package org.baratinage.report_exporter;

import java.util.List;

public interface IReportExporterItem {
  public String getMarkdown();

  public List<ReportExporterPlot> getImages();

}
