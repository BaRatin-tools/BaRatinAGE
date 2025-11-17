package org.baratinage.report_exporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.ui.plot.PlotExporter;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.DirUtils;
import org.baratinage.utils.fs.WriteFile;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class ReportExportWriter {

  static public final Path REPORT_TEMP_DIR = Path.of(AppSetup.PATH_APP_TEMP_DIR, "reports");
  static public final Path ASSETS_DIR = Path.of("assets");
  static final private Path mdCSSPath = Path.of(AppSetup.PATH_RESSOURCES_DIR, "md.css");

  static final private List<Extension> extensions = List.of(TablesExtension.create());
  static final private Parser parser = Parser.builder()
      .extensions(extensions)
      .build();
  static final private HtmlRenderer renderer = HtmlRenderer.builder()
      .extensions(extensions)
      .build();

  // private String md;
  private String id;
  public final List<String> lines = new ArrayList<>();
  public final List<ReportExporterPlot> images = new ArrayList<>();

  public ReportExportWriter(String id) {
    // this("", id);
    this.id = id;
  }

  public String getMarkdown() {
    return String.join("\n", lines);
  }

  public String getHTML() {
    Node document = parser.parse(getMarkdown());
    String html = renderer.render(document);
    return html;
  }

  public void writeImages(Path targetDir) {
    Path assetPath = targetDir.resolve(ASSETS_DIR);
    DirUtils.createDir(assetPath.toString());
    for (ReportExporterPlot img : images) {
      PlotExporter.saveToSvg(img.svg,
          assetPath
              .resolve("%s.svg".formatted(img.id))
              .toString());
      PlotExporter.saveToPng(img.png, assetPath
          .resolve("%s.png".formatted(img.id))
          .toString());
    }

  }

  public void writeImages() {
    writeImages(getMainPath());
  }

  public File writeMardown(Path targerDir) {
    Path mdPath = targerDir.resolve(Path.of(String.format("%s.md", id)));
    File mdFile = mdPath.toFile();
    try {
      WriteFile.writeStringContent(mdPath.toString(), getMarkdown());
    } catch (IOException e) {
      ConsoleLogger.error(e);
    }
    return mdFile;
  }

  public File writeMardown() {
    return writeMardown(getMainPath());
  }

  private Path getMainPath() {
    Path mainPath = REPORT_TEMP_DIR.resolve(Path.of(id));
    return mainPath.normalize();
  }

  public File writeHTML(Path targetDir) {
    Path htmlPath = targetDir.resolve(Path.of(String.format("%s.html", id)));
    String html = getHTML();
    Path assetPath = targetDir.resolve(ASSETS_DIR);
    Path mdCSSPathTarget = assetPath.resolve(Path.of("md.css"));

    DirUtils.createDir(assetPath.toString());

    File htmlFile = htmlPath.toFile();
    try {
      Files.copy(mdCSSPath, mdCSSPathTarget, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      ConsoleLogger.error(e);
    }
    try {
      WriteFile.writeStringContent(htmlFile.toString(), wrapHTML(html, id));
    } catch (IOException e) {
      ConsoleLogger.error(e);
    }
    return htmlFile;
  }

  public File writeHTML() {
    return writeHTML(getMainPath());
  }

  private static String wrapHTML(String html, String name) {
    String template = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>%1$s</title>
          <link rel="stylesheet" href="assets/%3$s">
        </head>
        <body>
        %2$s
        </body>
        </html>
        """;
    return String.format(template, name, html, "md.css");
  }

}
