package org.baratinage.ui.bam;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.ui.container.RowColPanel;

abstract public class BamItem extends RowColPanel {

    private String uuid;
    private BamItem[] parents;

    @FunctionalInterface
    public interface BamItemChangeListener extends EventListener {
        public void onChange(BamItem item);
    }

    private List<BamItemChangeListener> bamItemChangeListeners;

    private JLabel titleLabel;
    private JButton deleteButton;
    private RowColPanel headerPanel;
    private RowColPanel contentPanel;

    public BamItem(BamItem... parents) {
        super(AXIS.COL);

        headerPanel = new RowColPanel(AXIS.ROW);
        headerPanel.setGap(5);
        headerPanel.setPadding(5);
        contentPanel = new RowColPanel();

        super.appendChild(headerPanel, 0, 0, 0, 0, 0);
        super.appendChild(new JSeparator(), 0, 0, 0, 0, 0);
        super.appendChild(contentPanel, 1, 0, 0, 0, 0);

        titleLabel = new JLabel(getName());
        Font font = titleLabel.getFont();
        titleLabel.setFont(font.deriveFont(Font.BOLD));
        deleteButton = new JButton("Supprimer");

        headerPanel.appendChild(titleLabel, 1);
        headerPanel.appendChild(deleteButton, 0);

        this.uuid = UUID.randomUUID().toString();
        this.bamItemChangeListeners = new ArrayList<>();

        this.parents = parents;
        System.out.println(this.parents);
        for (BamItem parent : parents) {
            parent.addChangeListener((p) -> {
                System.out.println("PARENT_HAS_CHANGED >>> " + p);
            });
        }

    }

    public void addDeleteAction(ActionListener action) {
        this.deleteButton.addActionListener(action);
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    public void setContent(Component component) {
        this.contentPanel.clear();
        this.contentPanel.appendChild(component);
    }

    @Override
    public void appendChild(Component component, double weight,
            int topPadding, int rightPadding,
            int bottomPadding, int leftPadding) {
        throw new UnsupportedOperationException("Use setContent method! AppendChild is disabled for BamItem");
    }

    public String getUUID() {
        return this.uuid;
    }

    // // FIXME: when/how are children removed?
    public void hasChanged() {
        fireChangeListeners();
        // for (BamItem child : this.children) {
        // child.parentHasChanged(this);
        // }
    }

    public void addChangeListener(BamItemChangeListener updateListener) {
        this.bamItemChangeListeners.add(updateListener);
    }

    public void removeChangeListener(BamItemChangeListener updateListener) {
        this.bamItemChangeListeners.remove(updateListener);
    }

    public void fireChangeListeners() {
        for (BamItemChangeListener listener : this.bamItemChangeListeners) {
            listener.onChange(this);
        }
    }

    public abstract String getName();

    @Deprecated
    public abstract void parentHasChanged(BamItem parent);

    public abstract String toJsonString();

    public abstract void fromJsonString(String jsonString);
}
