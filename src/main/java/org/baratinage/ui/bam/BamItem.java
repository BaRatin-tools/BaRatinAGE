package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.translation.T;
import org.baratinage.translation.Translatable;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.component.Title;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.Misc;
import org.json.JSONObject;

abstract public class BamItem extends GridPanel implements Translatable {

    public final BamItemType TYPE;
    public final String ID;
    public final BamProject PROJECT;

    public final Title bamItemTypeLabel = new Title();
    public final SimpleTextField bamItemNameField = new SimpleTextField();
    public final SimpleTextField bamItemDescriptionField = new SimpleTextField();
    public final JButton cloneButton = new JButton();
    public final JButton deleteButton = new JButton();

    private GridPanel headerPanel;
    private RowColPanel contentPanel;

    public BamItem(BamItemType type, String uuid, BamProject project) {
        TYPE = type;
        ID = uuid;
        PROJECT = project;

        headerPanel = new GridPanel();
        headerPanel.setGap(5);
        headerPanel.setPadding(5);
        headerPanel.setColWeight(1, 1);

        bamItemTypeLabel.setText("BamItem");
        bamItemNameField.setText("Unnamed");

        headerPanel.insertChild(bamItemTypeLabel, 0, 0);
        headerPanel.insertChild(bamItemNameField, 1, 0, ANCHOR.C, FILL.H);
        headerPanel.insertChild(cloneButton, 2, 0, ANCHOR.C, FILL.BOTH);
        headerPanel.insertChild(deleteButton, 3, 0, ANCHOR.C, FILL.BOTH);
        headerPanel.insertChild(bamItemDescriptionField, 0, 1, 4, 1);

        contentPanel = new RowColPanel();

        insertChild(headerPanel, 0, 0);
        insertChild(new JSeparator(), 0, 1);
        insertChild(contentPanel, 0, 2);

        setColWeight(0, 1);
        setRowWeight(2, 1);

        T.t(this, this);
    }

    @Override
    public void translate() {
        bamItemNameField.setPlaceholder(T.text("name"));
        bamItemDescriptionField.setPlaceholder(T.text("name"));
    }

    public void setContent(Component component) {
        this.contentPanel.clear();
        this.contentPanel.appendChild(component);
    }

    public abstract BamItemConfig save(boolean writeFiles);

    public abstract void load(BamItemConfig bamItemBackup);

    public JSONObject toFullJSON() {
        JSONObject json = new JSONObject();
        json.put("type", TYPE);
        json.put("uuid", ID);
        json.put("name", bamItemNameField.getText());
        json.put("description", bamItemDescriptionField.getText());
        json.put("content", BamItemConfig.toJSON(save(true)));
        return json;
    }

    public void fromFullJSON(JSONObject json) {
        bamItemNameField.setText(json.getString("name"));
        bamItemDescriptionField.setText(json.getString("description"));
        BamItemConfig bamItemBackup = BamItemConfig.fromJSON(json.getJSONObject("content"));
        load(bamItemBackup);
    }

    @Override
    public String toString() {
        return "BamItem | " + TYPE + " | " + bamItemNameField.getText() + " (" + ID + ")";
    }

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public void fireChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    public abstract BamItem clone(String uuid);

    public BamItem clone() {
        String uuid = Misc.getTimeStampedId();
        return clone(uuid);
    }

    public void setCopyName() {
        String oldName = bamItemNameField.getText();
        String newName = T.text("copy_of", oldName);
        bamItemNameField.setText(newName);
    }
}
