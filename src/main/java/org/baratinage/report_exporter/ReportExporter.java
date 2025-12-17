package org.baratinage.report_exporter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.baratinage.AppSetup;
import org.baratinage.report_exporter.item_exporters.GaugingsREI;
import org.baratinage.report_exporter.item_exporters.HydraulicConfigurationREI;
import org.baratinage.report_exporter.item_exporters.HydrographREI;
import org.baratinage.report_exporter.item_exporters.LimnigraphREI;
import org.baratinage.report_exporter.item_exporters.RatingCurveComparatorREI;
import org.baratinage.report_exporter.item_exporters.RatingCurveREI;
import org.baratinage.report_exporter.item_exporters.RatingShiftDetectorREI;
import org.baratinage.report_exporter.item_exporters.StructuralErrorREI;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.baratin.Gaugings;
import org.baratinage.ui.baratin.HydraulicConfiguration;
import org.baratinage.ui.baratin.Hydrograph;
import org.baratinage.ui.baratin.Limnigraph;
import org.baratinage.ui.baratin.RatingCurve;
import org.baratinage.ui.baratin.RatingCurveCompare;
import org.baratinage.ui.baratin.RatingShiftHappens;
import org.baratinage.ui.commons.StructuralErrorModelBamItem;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleCheckbox;
import org.baratinage.ui.component.SimpleDialog;
import org.baratinage.ui.component.SimpleTextAreaField;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.perf.TimedActions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.awt.Desktop;

public class ReportExporter {

  private final ReportExportWriter reportWriter;

  private final SimpleDialog dialog;

  private BamProject project;

  private final HashMap<BamItem, SimpleCheckbox> bamItemsCheckboxes = new HashMap<>();
  private final SimpleTextAreaField mdTextField = new SimpleTextAreaField();

  public ReportExporter(BamProject project) {

    this.project = project;

    dialog = new SimpleDialog(AppSetup.MAIN_FRAME, true);
    dialog.setTitle(T.text("report_exporter"));
    dialog.setSize(700, 400);

    reportWriter = new ReportExportWriter("default");

    SimpleFlowPanel topPanel = new SimpleFlowPanel();
    topPanel.setGap(5);
    topPanel.addChild(buildProjectComponentPicker(), 1);
    topPanel.addChild(buildActionsPanel(), 1);
    dialog.setContent(topPanel);

    updateReport();
  }

  private void updateReport() {

    reportWriter.lines.clear();
    reportWriter.images.clear();

    reportWriter.lines.add(MD.h(1, project.getProjectName()));

    // hydraulic config
    List<IReportExporterItem> hydraulicConfigItems = this.getReportExplorerItems(
        BamItemType.HYDRAULIC_CONFIG,
        (bamItem) -> new HydraulicConfigurationREI((HydraulicConfiguration) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        hydraulicConfigItems,
        BamItemType.HYDRAULIC_CONFIG);

    // gaugings
    List<IReportExporterItem> gaugingsItems = this.getReportExplorerItems(
        BamItemType.GAUGINGS,
        (bamItem) -> new GaugingsREI((Gaugings) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        gaugingsItems,
        BamItemType.GAUGINGS);

    // structural errors
    List<IReportExporterItem> structuralErrorItems = this.getReportExplorerItems(
        BamItemType.STRUCTURAL_ERROR,
        (bamItem) -> new StructuralErrorREI((StructuralErrorModelBamItem) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        structuralErrorItems,
        BamItemType.STRUCTURAL_ERROR);

    // rating curve
    List<IReportExporterItem> ratingCurveItems = this.getReportExplorerItems(
        BamItemType.RATING_CURVE,
        (bamItem) -> new RatingCurveREI((RatingCurve) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        ratingCurveItems,
        BamItemType.RATING_CURVE);

    // limnigraph
    List<IReportExporterItem> limnigraphItems = this.getReportExplorerItems(
        BamItemType.LIMNIGRAPH,
        (bamItem) -> new LimnigraphREI((Limnigraph) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        limnigraphItems,
        BamItemType.LIMNIGRAPH);

    // hydrograph
    List<IReportExporterItem> hydrographItems = this.getReportExplorerItems(
        BamItemType.HYDROGRAPH,
        (bamItem) -> new HydrographREI((Hydrograph) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        hydrographItems,
        BamItemType.HYDROGRAPH);

    // rating curve comparator
    List<IReportExporterItem> rcCompareItems = this.getReportExplorerItems(
        BamItemType.COMPARING_RATING_CURVES,
        (bamItem) -> new RatingCurveComparatorREI((RatingCurveCompare) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        rcCompareItems,
        BamItemType.COMPARING_RATING_CURVES);

    // rating shift detector
    List<IReportExporterItem> rcShiftDetectorItems = this.getReportExplorerItems(
        BamItemType.RATING_SHIFT_HAPPENS,
        (bamItem) -> new RatingShiftDetectorREI((RatingShiftHappens) bamItem));
    ReportExporter.updateReportExportWriter(
        reportWriter,
        rcShiftDetectorItems,
        BamItemType.RATING_SHIFT_HAPPENS);

    // update markdown text field;
    mdTextField.setText(String.join("\n", reportWriter.getMarkdown()));
  }

  private static ReportExportWriter updateReportExportWriter(
      ReportExportWriter rew,
      List<IReportExporterItem> items,
      BamItemType itemType) {

    if (items.size() > 0) {
      rew.lines.add(MD.h(2, T.text(itemType.id)));
      for (IReportExporterItem item : items) {
        rew.lines.add(item.getMarkdown());
        rew.images.addAll(item.getImages());
      }
    }

    return rew;
  }

  private List<IReportExporterItem> getReportExplorerItems(
      BamItemType itemType,
      Function<BamItem, IReportExporterItem> builder) {
    BamItemList bamItems = project.BAM_ITEMS.filterByType(itemType);

    List<IReportExporterItem> items = new ArrayList<>();
    for (BamItem bamItem : bamItems) {
      SimpleCheckbox cb = bamItemsCheckboxes.get(bamItem);
      if (cb != null && cb.isSelected()) {
        items.add(builder.apply(bamItem));
      }
    }
    return items;
  }

  private SimpleFlowPanel buildProjectComponentPicker() {

    SimpleFlowPanel cbsPanel = new SimpleFlowPanel(true);
    cbsPanel.setGap(5);

    for (BamItem item : project.BAM_ITEMS) {
      SimpleCheckbox cb = new SimpleCheckbox();
      cb.setText(item.bamItemNameField.getText());
      cb.setSelected(true);
      cb.addItemListener(l -> {
        TimedActions.debounce("report_exporter", 100, () -> {
          updateReport();
        });
      });
      bamItemsCheckboxes.put(item, cb);
      cbsPanel.addChild(cb, 0);
    }

    JScrollPane cbsScrollPane = new JScrollPane(cbsPanel);
    cbsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    JLabel componentLabels = new JLabel();
    componentLabels.setText(T.text("components_selection"));

    JButton selectAllBtn = new JButton();
    selectAllBtn.setText(T.text("select_all"));
    selectAllBtn.addActionListener(l -> {
      bamItemsCheckboxes.forEach((i, c) -> {
        c.setSelected(true);
      });
    });
    JButton unselectAllBtn = new JButton();
    unselectAllBtn.setText(T.text("unselect_all"));
    unselectAllBtn.addActionListener(l -> {
      bamItemsCheckboxes.forEach((i, c) -> {
        c.setSelected(false);
      });
    });

    SimpleFlowPanel actionsPanel = new SimpleFlowPanel();
    actionsPanel.setGap(5);
    actionsPanel.addChild(selectAllBtn, 1);
    actionsPanel.addChild(unselectAllBtn, 1);

    SimpleFlowPanel mainPanel = new SimpleFlowPanel(true);
    mainPanel.setGap(5);
    mainPanel.addChild(componentLabels, 0);
    mainPanel.addChild(cbsScrollPane);
    mainPanel.addChild(actionsPanel, 0);

    return mainPanel;
  }

  public SimpleFlowPanel buildActionsPanel() {

    JButton cancelButton = new JButton();
    cancelButton.setText(T.text("exit"));
    cancelButton.addActionListener(l -> {
      dialog.closeDialog();
    });

    JButton previewButton = new JButton();
    previewButton.setText(T.text("preview_html"));
    previewButton.addActionListener(l -> {
      reportWriter.writeImages();
      File htmlFile = reportWriter.writeHTML();
      browsePage(htmlFile);
    });

    JButton exportMarkdownButton = new JButton();
    exportMarkdownButton.setText(T.text("export_to_md"));
    exportMarkdownButton.addActionListener(l -> {
      File selectedDirectory = CommonDialog.saveDirDialog(T.text("select_target_dir"));
      if (selectedDirectory == null) {
        return;
      }
      reportWriter.writeImages(selectedDirectory.toPath());
      reportWriter.writeMardown(selectedDirectory.toPath());
      openDirectory(selectedDirectory);
    });

    JButton exportHTMLButton = new JButton();
    exportHTMLButton.setText(T.text("export_to_html"));
    exportHTMLButton.addActionListener(l -> {
      File selectedDirectory = CommonDialog.saveDirDialog(T.text("select_target_dir"));
      if (selectedDirectory == null) {
        return;
      }
      reportWriter.writeImages(selectedDirectory.toPath());
      reportWriter.writeHTML(selectedDirectory.toPath());
      openDirectory(selectedDirectory);
    });

    JButton exportDOCXButton = new JButton();
    exportDOCXButton.setText(T.text("export_to_docx"));
    exportDOCXButton.addActionListener(l -> {
      File selectedFile = CommonDialog.saveFileDialog(
          "",
          null,
          new CommonDialog.CustomFileFilter(
              T.text("docx_format"), "docx"));
      if (selectedFile == null) {
        return;
      }
      reportWriter.writeDOCX(selectedFile.toPath());
    });

    JLabel label = new JLabel();
    label.setText(T.text("report_exporter"));
    SimpleFlowPanel actionsPanel = new SimpleFlowPanel(true);
    actionsPanel.setGap(5);
    actionsPanel.addChild(label, false);
    actionsPanel.addChild(previewButton, false);
    actionsPanel.addChild(exportMarkdownButton, false);
    actionsPanel.addChild(exportHTMLButton, false);
    actionsPanel.addChild(exportDOCXButton, false);
    actionsPanel.addChild(cancelButton, false);

    return actionsPanel;
  }

  private static void openDirectory(File dir) {
    if (Desktop.isDesktopSupported() &&
        Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
      try {
        Desktop.getDesktop().open(dir);
      } catch (IOException e) {
        ConsoleLogger.error(e);
      }
    } else {
      ConsoleLogger.error("Desktop is not supported");
    }
  }

  private static void browsePage(File htmlFile) {
    try {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(htmlFile.toURI());
      } else {
        System.err.println("Desktop is not supported");
      }
    } catch (IOException e) {
      ConsoleLogger.error(e);
    }
  }

  public void showDialog() {
    dialog.openDialog();
  }

}