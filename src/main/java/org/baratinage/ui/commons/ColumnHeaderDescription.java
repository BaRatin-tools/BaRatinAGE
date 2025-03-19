package org.baratinage.ui.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.baratinage.translation.T;
import org.baratinage.ui.component.SimpleDialog;
import org.baratinage.ui.container.RowColPanel;

public class ColumnHeaderDescription {

    private int k = 0;
    private List<String> headers = new ArrayList<>();
    private Map<String, Supplier<String>> descriptions = new HashMap<>();

    public void addColumnDesc(Supplier<String> descSupplier) {
        String header = String.format("###_%d", k);
        k++;
        addColumnDesc(header, descSupplier);
    }

    public void addColumnDesc(String header, Supplier<String> descSupplier) {
        headers.add(header);
        descriptions.put(header, descSupplier);
    }

    public void clearAllColumnDesc() {
        headers.clear();
        descriptions.clear();
        k = 0;
    }

    private JScrollPane buildMessagePanel() {
        RowColPanel panel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        panel.setPadding(5);
        panel.setGap(5);
        for (String h : headers) {
            Supplier<String> descSupplier = descriptions.get(h);
            String desc = descSupplier.get();
            String message = "";
            if (h.startsWith("###")) {
                message = String.format("<html>%s</html>", desc);
            } else {
                message = String.format("<html><b><code>%s</code></b><code> = </code>%s</html>", h, desc);
            }
            JLabel label = new JLabel();
            label.setText(message);
            panel.appendChild(label, 0);
        }

        return new JScrollPane(panel);
    }

    public void openDialog() {
        openDialog(null);
    }

    public void openDialog(String title) {

        String titleText = T.text("table_headers_desc");
        if (title != null) {
            titleText = String.format("%s - %s", title, titleText);
        }
        SimpleDialog dialog = SimpleDialog.buildInfoDialog(titleText, buildMessagePanel());

        dialog.setSize(800, 500);
        dialog.openDialog();
    }
}
