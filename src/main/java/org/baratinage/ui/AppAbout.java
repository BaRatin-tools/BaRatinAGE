package org.baratinage.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.translation.T;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

public class AppAbout extends JDialog {

    private static record CreditEntry(
            String name,
            String affiliation,
            String email,
            String[] contributionKeys) {
        public String getPersonString() {
            boolean hasAffiliation = affiliation != null && !affiliation.equals("");
            boolean hasEmail = email != null && !email.equals("");
            String str = name;
            if (hasAffiliation && !hasEmail) {
                str = String.format("%s (<i>%s</i>)", name, affiliation);
            } else if (!hasAffiliation && hasEmail) {
                str = String.format("%s (<code>%s</code>)", name, email);
            } else {
                str = String.format("%s (<i>%s</i>, <code>%s</code>)", name, affiliation, email);
            }
            return String.format("<html>%s<html>", str);
        }
    };

    private final List<CreditEntry> creditEntries;
    private final GridPanel creditsPanel;
    private final JScrollPane creditsScrollPane;
    private final List<String> creditsDescTexts;
    private final List<JLabel> creditsDescLabels;

    private Attributes getMyManifestAttributes() {
        String className = getClass().getSimpleName() + ".class";
        String classPath = getClass().getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            ConsoleLogger.error("Not in a jar file.");
            return null;
        }
        try {
            URL url = new URL(classPath);
            JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            Manifest manifest = jarConnection.getManifest();
            return manifest.getMainAttributes();
        } catch (MalformedURLException e) {
            ConsoleLogger.error(e);
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
        return null;
    }

    public AppAbout() {
        super(AppConfig.AC.APP_MAIN_FRAME, true);
        RowColPanel contentPanel = new RowColPanel(RowColPanel.AXIS.COL);

        String version = "";

        String buildInfo = "";

        Attributes manifestAttributes = getMyManifestAttributes();
        if (manifestAttributes != null) {
            version = manifestAttributes.getValue("Project-Version");
            String buildJdk = manifestAttributes.getValue("Build-Jdk");
            String buildOS = manifestAttributes.getValue("Build-OS");
            String buildTimestamp = manifestAttributes.getValue("Build-Timestamp");
            // "jdk: " + ;
            buildInfo = String.format(
                    "<html><b>Build info:</b><br><code>JDK: %s</code><br><code>OS: %s</code><br><code>Timestamp: %s</code></html>",
                    buildJdk, buildOS, buildTimestamp);
        }

        setTitle(T.text("about"));

        JLabel title = new JLabel();

        title.setText(String.format("<html><h1>BaRatinAGE <code>%s</code></h1></html>", version));

        JLabel description = new JLabel();
        T.t(this, description, true, "about_main_description");

        JLabel credits = new JLabel();
        credits.setFont(credits.getFont().deriveFont(18f));
        T.t(this, credits, true, "about_credits");

        creditsDescTexts = new ArrayList<>();
        creditsDescLabels = new ArrayList<>();

        creditEntries = new ArrayList<>();
        creditsPanel = new GridPanel();
        creditsPanel.setAnchor(GridPanel.ANCHOR.N);
        creditsPanel.setPadding(5);
        creditsPanel.setGap(5);
        creditsPanel.setColWeight(1, 1);
        creditsScrollPane = new JScrollPane(creditsPanel);
        creditsScrollPane.setPreferredSize(new Dimension(700, 400));

        try {
            String[] lines = ReadFile.getLines("resources/credits.txt", Integer.MAX_VALUE, true);
            for (int k = 1; k < lines.length; k++) {
                String[] parsedLine = ReadFile.trimStringArray(ReadFile.parseString(lines[k], ";", true));
                if (parsedLine.length == 4) {
                    creditEntries.add(
                            new CreditEntry(
                                    parsedLine[0],
                                    parsedLine[1],
                                    parsedLine[2],
                                    ReadFile.trimStringArray(parsedLine[3].split(","))));
                }
            }
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }

        contentPanel.appendChild(title, 0);
        contentPanel.appendChild(description, 0);
        contentPanel.appendChild(new JSeparator(), 0);
        contentPanel.appendChild(credits, 0);
        contentPanel.appendChild(creditsScrollPane, 1);
        contentPanel.appendChild(new JLabel(buildInfo), 0);

        // JScrollPane scrollPane = new JScrollPane(contentPanel);

        contentPanel.setGap(5);
        contentPanel.setPadding(10);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            }
        });

        setContentPane(contentPanel);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = creditsScrollPane.getWidth();

                GridBagLayout gbl = (GridBagLayout) creditsPanel.getLayout();
                int[][] res = gbl.getLayoutDimensions();
                int firstColWidth = res[0][1];

                // FIXME: 0.70 is magic number I don't understand...
                Double sDbl = Double.valueOf(w - firstColWidth) * 0.70;
                int s = sDbl.intValue();

                for (int k = 0; k < creditsDescLabels.size(); k++) {
                    updateDescLabelWidth(creditsDescLabels.get(k),
                            creditsDescTexts.get(k),
                            s);
                }

                creditsPanel.updateUI();
            }
        });
    }

    private void updateDescLabelWidth(JLabel label, String text, int width) {
        label.setText(
                String.format(
                        "<html><div style='width:%dpx;'>%s</div></html>",
                        width, text));
    }

    public void showAboutDialog() {

        creditsPanel.clear();
        creditsDescTexts.clear();
        creditsDescLabels.clear();
        int rowIndex = 0;
        for (int k = 0; k < creditEntries.size(); k++) {
            if (k != 0) {
                creditsPanel.insertChild(new JSeparator(), 0, rowIndex, 2, 1);
                rowIndex++;
            }

            CreditEntry entry = creditEntries.get(k);
            JLabel personLabel = new JLabel(entry.getPersonString());
            personLabel.setVerticalAlignment(SwingConstants.TOP);
            creditsPanel.insertChild(personLabel, 0, rowIndex);
            String descString = "";
            RowColPanel contributionPanel = new RowColPanel(RowColPanel.AXIS.COL);
            contributionPanel.setGap(5);
            for (int i = 0; i < entry.contributionKeys.length; i++) {
                String contributionKey = entry.contributionKeys[i];
                if (contributionKey.startsWith("translation")) {
                    String localeKey = contributionKey.split("_")[1];
                    String localeText = Locale.forLanguageTag(localeKey).getDisplayName(T.getLocale());
                    descString = T.text("translation", localeText);
                } else {
                    descString = T.text(contributionKey);
                }
                descString = descString.substring(0, 1).toUpperCase() + descString.substring(1);
                creditsDescTexts.add(descString);
                JLabel descLabel = new JLabel();
                creditsDescLabels.add(descLabel);
                updateDescLabelWidth(descLabel, descString, 100);
                contributionPanel.appendChild(descLabel, 0);
                if (i != entry.contributionKeys.length - 1) {
                    contributionPanel.appendChild(new JSeparator(), 0);
                }
            }
            creditsPanel.insertChild(contributionPanel, 1, rowIndex);
            rowIndex++;
        }

        pack();
        setLocationRelativeTo(AppConfig.AC.APP_MAIN_FRAME);
        setVisible(true);
    }

}
