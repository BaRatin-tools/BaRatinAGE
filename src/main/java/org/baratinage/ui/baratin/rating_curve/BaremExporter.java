package org.baratinage.ui.baratin.rating_curve;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleDateTimeField;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;

public class BaremExporter extends RowColPanel {

    private final JDialog dialog;

    private boolean canceled;

    private String hydroCode;
    private String hydroName;
    private LocalDateTime validityStartTime;
    private LocalDateTime validityEndTime;

    private double[] h;
    private double[] Q;
    private double[] Qlow;
    private double[] Qhigh;

    private final SimpleTextField hydroCodeField;
    private final SimpleTextField hyroNameField;
    private final SimpleDateTimeField startDateField;
    private final SimpleDateTimeField endDateField;

    public BaremExporter() {
        this("", "", LocalDateTime.now(), LocalDateTime.of(
                2050,
                1,
                1,
                0,
                0));

    }

    public BaremExporter(
            String hydroCode, String hydroName,
            LocalDateTime validityStartTime, LocalDateTime validityEndTime) {

        // creating dialog content

        hydroCodeField = new SimpleTextField();
        hyroNameField = new SimpleTextField();
        startDateField = new SimpleDateTimeField(this, false);
        endDateField = new SimpleDateTimeField(this, false);

        JButton okButton = new JButton();
        JButton cancelButton = new JButton();
        RowColPanel actionPanel = new RowColPanel(AXIS.ROW, ALIGN.STRETCH);
        actionPanel.appendChild(cancelButton, 0);
        actionPanel.appendChild(new JComponent() {

        }, 1);
        actionPanel.appendChild(okButton, 0);

        JLabel hydroCodeLabel = new JLabel();
        JLabel hydroNameLabel = new JLabel();
        JLabel startDateLabel = new JLabel();
        JLabel endDateLabel = new JLabel();

        setMainAxis(AXIS.COL);
        setPadding(5);
        setGap(5);

        appendChild(hydroCodeLabel, 0);
        appendChild(hydroCodeField, 0);

        appendChild(hydroNameLabel, 0);
        appendChild(hyroNameField, 0);

        appendChild(startDateLabel, 0);
        appendChild(startDateField, 0);

        appendChild(endDateLabel, 0);
        appendChild(endDateField, 0);

        appendChild(new JSeparator(), 0);
        appendChild(actionPanel, 0);

        T.t(this, () -> {
            hydroCodeLabel.setText(T.text("hydro_code"));
            hydroNameLabel.setText(T.text("name"));
            startDateLabel.setText(T.text("validity_start"));
            endDateLabel.setText(T.text("validity_end"));
            okButton.setText(T.text("ok"));
            cancelButton.setText(T.text("cancel"));
        });

        // setting up default

        this.hydroCode = hydroCode;
        this.hydroName = hydroName;
        this.validityStartTime = validityStartTime;
        this.validityEndTime = validityEndTime;
        hydroCodeField.setText(hydroCode);
        hyroNameField.setText(hydroName);
        startDateField.setDateTime(validityStartTime);
        endDateField.setDateTime(validityEndTime);

        // setting up dialog

        dialog = new JDialog(AppSetup.MAIN_FRAME, true);
        dialog.setResizable(false);
        dialog.setTitle(T.text("export_to_bareme_format"));
        dialog.setContentPane(this);

        // setting listeners

        hydroCodeField.addChangeListener((e) -> {
            String txt = hydroCodeField.getText();
            if (txt.length() > 8) {
                hydroCodeField.setTextDelayed(txt.substring(0, 8), false);
            }
        });
        hyroNameField.addChangeListener((e) -> {
            String txt = hyroNameField.getText();
            if (txt.length() > 6) {
                hyroNameField.setTextDelayed(txt.substring(0, 6), false);
            }
        });

        canceled = true;

        okButton.addActionListener((e) -> {

            canceled = false;
            dialog.setVisible(false);

            File f = CommonDialog.saveFileDialog(
                    T.text("export_to_bareme_format"),
                    T.text("bareme_format"), "dat");
            if (f != null) {
                verifyAndDoExport(f);
            } else {
                ConsoleLogger.warn("No file selected, aborting export.");
            }
        });
        cancelButton.addActionListener((e) -> {
            canceled = true;
            dialog.setVisible(false);
            ConsoleLogger.warn("xport canceled.");
        });

    }

    public String getHydroCode() {
        return canceled ? null : hydroCode;
    }

    public String getName() {
        return canceled ? null : hydroName;
    }

    public LocalDateTime getValidityStartDate() {
        return canceled ? null : validityStartTime;
    }

    public LocalDateTime getValidityEndDate() {
        return canceled ? null : validityEndTime;
    }

    public void updateRatingCurveValues(double[] h, double[] Q, double[] Qlow, double[] Qhigh) {
        this.h = h;
        this.Q = Q;
        this.Qlow = Qlow;
        this.Qhigh = Qhigh;
    }

    public void exportRatingCurve() {
        if (h == null || Q == null || Qlow == null || Qhigh == null) {
            ConsoleLogger
                    .error("At least one of stage, discharge or discharge envelope vector is null, aborting export.");
            return;
        }
        int n = h.length;
        if (Q.length != n || Qlow.length != n || Qhigh.length != n) {
            ConsoleLogger.error(
                    "At least one of discharge or discharge envelope vector has a non matching length, aborting export");
            return;
        }
        // FIXME: should I check for missing values as well, for a minimum number of
        // points, ...
        if (n > 100) {
            CommonDialog.infoDialog(T.text("export_to_bareme_max_value_warning"), T.text("warning"));
        }

        dialog.pack();
        dialog.setLocationRelativeTo(AppSetup.MAIN_FRAME);
        dialog.setVisible(true);
        dialog.dispose();
    }

    private void verifyAndDoExport(File f) {
        hydroCode = hydroCodeField.getText();
        hydroName = hyroNameField.getText();
        validityStartTime = startDateField.getDateTime();
        validityEndTime = endDateField.getDateTime();
        if (hydroCode == "") {
            ConsoleLogger.warn("hydroCode is empty.");
        }
        if (hydroName == "") {
            ConsoleLogger.warn("hydroName is empty.");
        }
        if (validityStartTime.isAfter(validityEndTime)) {
            ConsoleLogger.warn("validity start time is after validity end time");
        }
        try {
            writeBaremeFile(f, hydroCode, hydroName, validityStartTime, validityEndTime, h, Q, Qlow, Qhigh);
        } catch (IOException e) {
            ConsoleLogger.error(e);
        }
    }

    private static void appendLineToBufferedWriter(BufferedWriter bw, String... elements) throws IOException {
        String line = String.join(";", elements);
        bw.write(line);
        bw.newLine();
    }

    private static void writeBaremeFile(
            File f,
            String hydroCode,
            String hydroName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            double[] h, double[] Q, double[] Qlow, double[] Qhigh) throws IOException {

        if (!f.exists()) {
            f.createNewFile();
        }
        FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        // pre-processing values
        int m = h.length;
        int n = Math.min(100, h.length); // only the first 100 values are taken
        if (n != m) {
            CommonDialog.infoDialog(T.text("export_to_bareme_max_value_warning"), T.text("warning"));
        }
        String[] hValues = new String[n];
        String[] QValues = new String[n];
        String[] QminValues = new String[n];
        String[] QmaxValues = new String[n];
        for (int k = 0; k < n; k++) {
            // convert from m to mm;
            hValues[k] = Integer.valueOf(Double.valueOf(h[k] * 1000).intValue()).toString();
            // convert from m3/s to l/s;
            QValues[k] = Integer.valueOf(Double.valueOf(Q[k] * 1000).intValue()).toString();
            QminValues[k] = Integer.valueOf(Double.valueOf(Qlow[k] * 1000).intValue()).toString();
            QmaxValues[k] = Integer.valueOf(Double.valueOf(Qhigh[k] * 1000).intValue()).toString();
        }

        // pre-processing timestamps
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String startDate = startTime.format(dateFormatter);
        String startHourMinute = startTime.format(hourMinuteFormatter);
        String endDate = endTime.format(dateFormatter);
        String endHourMinute = endTime.format(hourMinuteFormatter);

        // line 1 - no idea what this means...
        appendLineToBufferedWriter(bw, "DEC", "  6 13");

        // line 2 - creation date, code, and various stuff I don't understand...
        String currTimeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
        appendLineToBufferedWriter(bw, "DEB", "BA-HYDRO", "BaRatin", currTimeString, hydroCode, "1111-1", "", "", "");

        // line 3 - lower envelop RC - not sure what the "2;12;5000" refers to...
        appendLineToBufferedWriter(bw, "C", "TAR", hydroCode, hydroName + "_inf", "2", "12", "5000", "", "",
                "h/q Baratin : Qmin", "");

        // line 4 - lower envelop RC validity
        appendLineToBufferedWriter(bw, "C", "PAT", hydroCode, hydroName + "_inf",
                startDate, startHourMinute, endDate, endHourMinute, "");

        // line 5 - upper envelop RC - not sure what the "2;12;5000" refers to...
        appendLineToBufferedWriter(bw, "C", "TAR", hydroCode, hydroName + "_sup", "2", "12", "5000", "", "",
                "h/q Baratin : Qmax", "");

        // line 6 - lower envelop RC validity
        appendLineToBufferedWriter(bw, "C", "PAT", hydroCode, hydroName + "_sup",
                startDate, startHourMinute, endDate, endHourMinute, "");

        // line 7 - maxpost RC - not sure what the "2;12;5000" refers to...
        appendLineToBufferedWriter(bw, "C", "TAR", hydroCode, hydroName, "2", "12", "5000", "", "",
                "h/q Baratin : Qmaxpost", "");

        // line 8 - maxpost RC validity
        appendLineToBufferedWriter(bw, "C", "PAT", hydroCode, hydroName,
                startDate, startHourMinute, endDate, endHourMinute, "");

        // inf CT block
        for (int k = 0; k < n; k++) {
            appendLineToBufferedWriter(bw, "C", "PIV", hydroCode, hydroName + "_inf", hValues[k], QminValues[k], "");
        }

        // sup CT block
        for (int k = 0; k < n; k++) {
            appendLineToBufferedWriter(bw, "C", "PIV", hydroCode, hydroName + "_sup", hValues[k], QmaxValues[k], "");
        }

        // Maxpost CT block
        for (int k = 0; k < n; k++) {
            appendLineToBufferedWriter(bw, "C", "PIV", hydroCode, hydroName, hValues[k], QValues[k], "");
        }

        // Ending line - no idea what this means...
        appendLineToBufferedWriter(bw, "FIN", "EXP-HYDRO", Integer.toString(8 + 3 * n + 1), "");

        bw.close();
    }
}
