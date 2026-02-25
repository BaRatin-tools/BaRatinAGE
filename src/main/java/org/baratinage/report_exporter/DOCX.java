package org.baratinage.report_exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.baratinage.utils.ConsoleLogger;
import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.ext.gfm.tables.TableHead;
import org.commonmark.ext.gfm.tables.TableRow;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPrGeneral;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STStyleType;

public class DOCX {

  private final XWPFDocument document;
  private XWPFParagraph currentParagraph;
  private long currentMaxWidthEmu = -1;
  private Path baseDirectory;

  public DOCX(Node markdown, Path baseDirectory) {
    this.baseDirectory = baseDirectory;
    document = new XWPFDocument();
    try {
      visit(markdown);
    } catch (Exception e) {
      ConsoleLogger.error(e);
    }
  }

  public void writeToFile(File file) {
    try {
      FileOutputStream out = new FileOutputStream(file);
      document.write(out);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void visit(Node node) throws Exception {
    if (node instanceof Heading h) {
      heading(h);
    } else if (node instanceof Paragraph p) {
      paragraph(p);
    } else if (node instanceof FencedCodeBlock c) {
      codeBlock(c);
    } else if (node instanceof TableBlock t) {
      table(t);
      return;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      visit(child);
    }
  }

  // ---------- BLOCK ELEMENTS ----------

  private void heading(Heading h) {

    if (h.getLevel() == 2) {
      XWPFParagraph breakParagraph = document.createParagraph();
      XWPFRun breakRun = breakParagraph.createRun();
      breakRun.addBreak(BreakType.PAGE);
    }

    ensureHeadingStyle(document, h.getLevel());

    XWPFParagraph p = document.createParagraph();
    p.setStyle("Heading" + h.getLevel());
    currentParagraph = p;
    inline(h);
  }

  private void paragraph(Paragraph p) {
    currentParagraph = document.createParagraph();
    inline(p);
  }

  private void codeBlock(FencedCodeBlock c) {
    XWPFParagraph p = document.createParagraph();

    String[] lines = c.getLiteral().split("\\R"); // split on any line break
    for (int i = 0; i < lines.length; i++) {
      XWPFRun r = p.createRun();
      r.setFontFamily("Courier New");
      r.setText(lines[i]);
      if (i < lines.length - 1) {
        r.addBreak(); // add line break after each line except last
      }
    }

  }

  private void table(TableBlock table) {
    int rows = 0;
    int cols = 0;

    // First pass: determine dimensions
    for (Node section = table.getFirstChild(); section != null; section = section.getNext()) {
      for (Node row = section.getFirstChild(); row != null; row = row.getNext()) {
        rows++;
        if (cols == 0 && row instanceof TableRow) {
          cols = countCells(row);
        }
      }
    }

    long pageWidth = getMaxPageWidthEmu(document);
    long colWidth = pageWidth / cols;

    XWPFTable t = document.createTable(rows, cols);
    int r = 0;

    // Second pass: fill table
    for (Node section = table.getFirstChild(); section != null; section = section.getNext()) {
      boolean header = section instanceof TableHead;
      for (Node row = section.getFirstChild(); row != null; row = row.getNext()) {
        XWPFTableRow tr = t.getRow(r++);
        int c = 0;
        for (Node cell = row.getFirstChild(); cell != null; cell = cell.getNext()) {
          XWPFTableCell tc = tr.getCell(c++);
          XWPFParagraph p = tc.getParagraphs().get(0);
          currentParagraph = p;
          currentMaxWidthEmu = colWidth;
          inline(cell);
          if (header) {
            for (XWPFRun run : p.getRuns()) {
              run.setBold(true);
            }
          }
        }
      }
    }
    currentMaxWidthEmu = -1; // reset context
  }

  private void image(Image i) throws Exception {
    Path imagePath = Path.of(i.getDestination());
    Path absImagePath = baseDirectory.resolve(imagePath);

    File file = absImagePath.toFile();

    BufferedImage img = ImageIO.read(file);
    if (img == null)
      return;

    int imgWidthPx = img.getWidth();
    int imgHeightPx = img.getHeight();

    long imgWidthEmu = Units.pixelToEMU(imgWidthPx);
    long imgHeightEmu = Units.pixelToEMU(imgHeightPx);

    long maxWidthEmu = currentMaxWidthEmu > 0
        ? currentMaxWidthEmu
        : getMaxPageWidthEmu(document);

    // scale down only if necessary
    if (imgWidthEmu > maxWidthEmu) {
      double scale = (double) maxWidthEmu / imgWidthEmu;
      imgWidthEmu = maxWidthEmu;
      imgHeightEmu = (long) (imgHeightEmu * scale);
    }

    try (InputStream is = new FileInputStream(file)) {
      XWPFRun r = currentParagraph.createRun();
      r.addPicture(is,
          PictureType.PNG,
          file.getName(),
          (int) imgWidthEmu,
          (int) imgHeightEmu);
    }
  }

  // ---------- INLINE ELEMENTS ----------

  private void inline(Node parent) {
    for (Node n = parent.getFirstChild(); n != null; n = n.getNext()) {
      if (n instanceof Text t) {
        currentParagraph.createRun().setText(t.getLiteral());
      } else if (n instanceof StrongEmphasis s) {
        XWPFRun r = currentParagraph.createRun();
        r.setBold(true);
        r.setText(extractText(s.getFirstChild()));
      } else if (n instanceof Emphasis em) {
        XWPFRun r = currentParagraph.createRun();
        r.setItalic(true);
        r.setText(extractText(em.getFirstChild()));
      } else if (n instanceof Code c) {
        XWPFRun r = currentParagraph.createRun();
        r.setFontFamily("Courier New");
        r.setText(c.getLiteral());
      } else if (n instanceof Image i) {
        try {
          image(i);
        } catch (Exception e) {
          ConsoleLogger.error(e);
        }
      }
    }
  }

  // ---------- HELPERS ----------
  private static int countCells(Node row) {
    int count = 0;
    for (Node n = row.getFirstChild(); n != null; n = n.getNext()) {
      count++;
    }
    return count;
  }

  private static String extractText(Node node) {
    StringBuilder sb = new StringBuilder();
    collectText(node, sb);
    return sb.toString();
  }

  private static void collectText(Node node, StringBuilder sb) {
    if (node instanceof Text t) {
      sb.append(t.getLiteral());
    } else if (node instanceof Code c) {
      sb.append(c.getLiteral());
    }

    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      collectText(child, sb);
    }
  }

  private static long getMaxPageWidthEmu(XWPFDocument doc) {
    CTSectPr sectPr = doc.getDocument().getBody().getSectPr();
    if (sectPr == null) {
      sectPr = doc.getDocument().getBody().addNewSectPr();
    }

    // Page size (defaults to A4 portrait if missing)
    CTPageSz pgSz = sectPr.isSetPgSz()
        ? sectPr.getPgSz()
        : sectPr.addNewPgSz();

    if (!pgSz.isSetW()) {
      pgSz.setW(BigInteger.valueOf(11906)); // A4 width in twips
    }

    // Margins (defaults)
    CTPageMar mar = sectPr.isSetPgMar()
        ? sectPr.getPgMar()
        : sectPr.addNewPgMar();

    mar.setLeft(BigInteger.valueOf(1440));
    mar.setRight(BigInteger.valueOf(1440));

    BigInteger pageWidthTwips = (BigInteger) pgSz.getW();
    BigInteger usableWidthTwips = pageWidthTwips.subtract((BigInteger) mar.getLeft())
        .subtract((BigInteger) mar.getRight());

    // 1 twip = 1/20 pt, 1 pt = 12700 EMU
    return usableWidthTwips.longValue() * 12700L / 20;
  }

  private static void ensureHeadingStyle(XWPFDocument doc, int level) {
    XWPFStyles styles = doc.getStyles();
    if (styles == null) {
      styles = doc.createStyles();
    }

    String styleId = "Heading" + level;

    if (styles.styleExist(styleId)) {
      return;
    }

    // Create style
    CTStyle ctStyle = CTStyle.Factory.newInstance();
    ctStyle.setStyleId(styleId);
    CTString styleName = CTString.Factory.newInstance();
    styleName.setVal("heading " + level);
    ctStyle.setName(styleName);
    ctStyle.setType(STStyleType.PARAGRAPH);
    CTPPrGeneral ppr = ctStyle.addNewPPr();

    // THIS is what makes it a real heading
    CTDecimalNumber outlineLvl = ppr.addNewOutlineLvl();
    outlineLvl.setVal(BigInteger.valueOf(level - 1));

    // Set styling
    CTRPr rpr = ctStyle.addNewRPr();
    rpr.addNewB();
    int fontSize = switch (level) {
      case 1 -> 48;
      case 2 -> 32;
      case 3 -> 20;
      case 4 -> 16;
      case 5 -> 12;
      default -> 12;
    };
    CTHpsMeasure size = rpr.addNewSz();
    size.setVal(BigInteger.valueOf(fontSize * 2)); // DOCX uses half-points
    CTHpsMeasure sizeCs = rpr.addNewSzCs();
    sizeCs.setVal(BigInteger.valueOf(fontSize * 2));

    XWPFStyle style = new XWPFStyle(ctStyle);
    styles.addStyle(style);
  }

}
