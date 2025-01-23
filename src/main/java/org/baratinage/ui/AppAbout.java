package org.baratinage.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.ReadFile;
import org.baratinage.AppSetup;
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

    public AppAbout() {
        super(AppSetup.MAIN_FRAME, true);
        RowColPanel contentPanel = new RowColPanel(RowColPanel.AXIS.COL);

        String version = "";

        String buildInfo = "";

        if (AppSetup.MANIFEST_MAIN_ATTRIBUTES != null) {
            version = AppSetup.MANIFEST_MAIN_ATTRIBUTES.getValue("Project-Version");
            String buildJdk = AppSetup.MANIFEST_MAIN_ATTRIBUTES.getValue("Build-Jdk");
            String buildOS = AppSetup.MANIFEST_MAIN_ATTRIBUTES.getValue("Build-OS");
            String buildTimestamp = AppSetup.MANIFEST_MAIN_ATTRIBUTES.getValue("Build-Timestamp");
            // "jdk: " + ;
            buildInfo = String.format(
                    "<html><b>Build info:</b><br><code>JDK: %s</code><br><code>OS: %s</code><br><code>Timestamp: %s</code></html>",
                    buildJdk, buildOS, buildTimestamp);
        } else {
            buildInfo = String.format(
                    "<html><b>Build info:</b><br><code>JDK: %s</code><br><code>OS: %s</code><br><code>Timestamp: %s</code></html>",
                    "JDK", "Windows", "2024-02-29");
        }

        setTitle(T.text("about"));

        JLabel title = new JLabel();

        title.setText(String.format("<html><h1>BaRatinAGE <code>%s</code></h1></html>", version));

        JLabel description = new JLabel();
        T.t(this, description, true, "about_main_description");

        JLabel howToCite = new JLabel();
        T.t(this, howToCite, true, "about_how_to_cite");

        JTextPane howToCiteReference = new JTextPane();
        howToCiteReference.setContentType("text/html");
        howToCiteReference.setEditable(false);
        howToCiteReference.setBackground(AppSetup.COLORS.DEFAULT_BG);
        howToCiteReference.setText(
                String.format(
                        "<html> <div> %s (%s).  %s <i> %s,  %s </i>,  %s <a href='%s'> %s </i> <div> </html>",
                        "Le Coz, J., Renard, B., Bonnifait, L., Branger, F., & Le Boursicaud, R.",
                        "2014",
                        "Combining hydraulic knowledge and uncertainty gaugings in the estimation of hydrometric rating curves: A Bayesian approach.",
                        "Journal of Hydrology",
                        "509",
                        "573–587",
                        "https://doi.org/10.1016/j.jhydrol.2013.11.016",
                        "https://doi.org/10.1016/j.jhydrol.2013.11.016"));

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

        try {
            List<String[]> creditsMatrix = ReadFile.readStringMatrix(
                    Path.of(AppSetup.PATH_RESSOURCES_DIR, "credits.csv").toString(),
                    ";",
                    1,
                    true,
                    true);
            int nEntry = creditsMatrix.get(0).length;
            for (int k = 0; k < nEntry; k++) {
                creditEntries.add(
                        new CreditEntry(
                                creditsMatrix.get(0)[k],
                                creditsMatrix.get(1)[k],
                                creditsMatrix.get(2)[k],
                                ReadFile.parseString(creditsMatrix.get(3)[k], ",", true)));
            }
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }

        contentPanel.appendChild(title, 0);
        contentPanel.appendChild(description, 0);
        contentPanel.appendChild(new JSeparator(), 0);
        contentPanel.appendChild(credits, 0);
        contentPanel.appendChild(creditsScrollPane, 1);
        contentPanel.appendChild(new JSeparator(), 0);
        contentPanel.appendChild(howToCite, 0);
        contentPanel.appendChild(howToCiteReference, 0);
        contentPanel.appendChild(new JSeparator(), 0);
        contentPanel.appendChild(new JLabel(buildInfo), 0);

        contentPanel.setGap(5);
        contentPanel.setPadding(10);

        Dimension dim = new Dimension(700, 600);
        creditsScrollPane.setPreferredSize(dim);
        contentPanel.setPreferredSize(dim);

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
                    descString = T.text("translation_in", localeText);
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
        setLocationRelativeTo(AppSetup.MAIN_FRAME);
        setVisible(true);
    }

}
