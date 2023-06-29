package org.baratinage.ui.bam;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

abstract public class BamItem extends GridPanel {

    public final BamItemType TYPE;
    public final String ID;
    public final BamProject PROJECT;

    private String name = "";
    private String description = "";

    private JLabel titleLabel;
    public final JButton cloneButton = new JButton();
    public final JButton deleteButton = new JButton();
    private GridPanel headerPanel;
    private RowColPanel contentPanel;

    public BamItem(BamItemType type, String uuid, BamProject project) {
        // super(AXIS.COL);
        TYPE = type;
        ID = uuid;
        PROJECT = project;

        headerPanel = new GridPanel();
        headerPanel.setGap(5);
        headerPanel.setPadding(5);
        headerPanel.setColWeight(0, 1);

        contentPanel = new RowColPanel();

        insertChild(headerPanel, 0, 0);
        insertChild(new JSeparator(), 0, 1);
        insertChild(contentPanel, 0, 2);

        setColWeight(0, 1);
        setRowWeight(2, 1);

        titleLabel = new JLabel(getName());
        Font font = titleLabel.getFont();
        titleLabel.setFont(font.deriveFont(Font.BOLD));

        headerPanel.insertChild(titleLabel, 0, 0);
        headerPanel.insertChild(cloneButton, 1, 0);
        headerPanel.insertChild(deleteButton, 2, 0);

    }

    public void addDeleteAction(ActionListener action) {
        this.deleteButton.addActionListener((e) -> {
            int response = JOptionPane.showConfirmDialog(this, "<html>Êtes-vous sûr de vouloir supprimer <b>" + name
                    + "</b>? <br/> Cette opération ne peut pas être annulée!</html>", "Attention!",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                action.actionPerformed(e);
            }
        });
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    public void setContent(Component component) {
        this.contentPanel.clear();
        this.contentPanel.appendChild(component);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        String oldName = getName();
        firePropertyChange("bamItemName", oldName, name);
        this.name = name;
        setTitle(name);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getTempDataFileNames() {
        return new String[] {};
    }

    public abstract JSONObject toJSON();

    public abstract void fromJSON(JSONObject json);

    public JSONObject toFullJSON() {
        JSONObject json = new JSONObject();
        json.put("type", TYPE);
        json.put("uuid", ID);
        json.put("name", name);
        json.put("description", description);
        json.put("content", toJSON());
        return json;
    }

    public void fromFullJSON(JSONObject json) {
        name = json.getString("name");
        description = json.getString("description");
        fromJSON(json.getJSONObject("content"));
    }

    @Override
    public String toString() {
        return "BamItem | " + TYPE + " | " + name + " (" + ID + ")";
    }

    public static boolean areMatching(JSONObject jsonA, JSONObject jsonB, String[] keys, boolean exclude) {

        if (exclude) {
            // create shallow copies (see: https://stackoverflow.com/a/12809884)
            jsonA = new JSONObject(jsonA, JSONObject.getNames(jsonA));
            jsonB = new JSONObject(jsonB, JSONObject.getNames(jsonB));
            for (String key : keys) {
                if (jsonA.has(key)) {
                    jsonA.remove(key);
                }
                if (jsonB.has(key)) {
                    jsonB.remove(key);
                }
            }
        } else {
            JSONObject filteredJsonA = new JSONObject();
            JSONObject filteredJsonB = new JSONObject();
            for (String key : keys) {
                if (jsonA.has(key)) {
                    filteredJsonA.put(key, jsonA.get(key));
                }
                if (jsonB.has(key)) {
                    filteredJsonB.put(key, jsonB.get(key));
                }
            }
            jsonA = filteredJsonA;
            jsonB = filteredJsonB;
        }

        String jsonStringA = jsonA.toString();
        String jsonStringB = jsonB.toString();

        System.out.println("***********************************");
        System.out.println(jsonStringA);
        System.out.println("--");
        System.out.println(jsonStringB);
        System.out.println("***********************************");

        return jsonStringA.equals(jsonStringB);
    }

    public boolean isMatchingWith(String jsonString, String[] keys, boolean exclude) {
        return isMatchingWith(new JSONObject(jsonString), keys, exclude);
    }

    public boolean isMatchingWith(JSONObject json, String[] keys, boolean exclude) {
        return areMatching(toJSON(), json, keys, exclude);
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
        String uuid = UUID.randomUUID().toString();
        return clone(uuid);
    }
}
