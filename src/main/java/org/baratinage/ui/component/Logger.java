package org.baratinage.ui.component;

import java.awt.EventQueue;
import java.awt.Font;

import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import java.util.Date;

import java.util.ArrayList;
import java.util.List;

public class Logger extends JPanel {

    private List<LogItem> logs;
    private String currentLogs;
    private JScrollPane scrollPane;
    private JTextPane logContainer;

    private String bodyStyle;
    private String timeStampStyle;
    private String logStyle;

    class LogItem {
        public String message;
        public Date date;
        public String timeStamp;

        LogItem(String message) {
            this.message = message;
            date = new Date();
            timeStamp = new SimpleDateFormat("HH:mm:ss").format(date);
        }
    }

    public Logger() {

        logs = new ArrayList<>();

        logContainer = new JTextPane();
        scrollPane = new JScrollPane(logContainer);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        logContainer.setContentType("text/html");
        logContainer.setText(currentLogs);
        logContainer.setEditable(false);

        Font font = UIManager.getFont("Label.font");
        bodyStyle = "font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt;";
        timeStampStyle = "color: gray; margin-right: 10px; display: inline-block;";
        logStyle = "";
        currentLogs = String.format("<body style='%s'></body>", bodyStyle);

        this.add(scrollPane);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    }

    public void addLogs(String[] logsText) {
        for (String logText : logsText) {
            this.addLogItem(logText);
        }
        this.updateView();
    }

    public void addLog(String logText) {
        this.addLogItem(logText);
        this.updateView();
    }

    private void addLogItem(String logItem) {
        logs.add(new LogItem(logItem));
        currentLogs = String.format("<body style='%s'>", bodyStyle);
        for (LogItem log : logs) {
            String timeStamp = String.format("<span style='%s'>%s :</span>", timeStampStyle, log.timeStamp);
            String logMessage = String.format("<span style='%s'>%s</span>", logStyle, log.message);
            String item = String.format("<div>%s%s</div>", timeStamp, logMessage);
            currentLogs = currentLogs + item;
        }
        currentLogs = currentLogs + "</body>";
    }

    private void updateView() {
        this.logContainer.setText(currentLogs);
        this.logContainer.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            }
        });

    }

}
