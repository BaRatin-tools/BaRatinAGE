package org.baratinage.ui.bam;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.ui.component.TextField;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.lg.LgElement;
import org.json.JSONObject;

abstract public class BamItem extends GridPanel {

    public final BamItemType TYPE;
    public final String ID;
    public final BamProject PROJECT;

    public final JLabel bamItemTypeLabel = new JLabel();
    public final TextField bamItemNameField = new TextField();
    public final TextField bamItemDescriptionField = new TextField();
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
        bamItemTypeLabel.setFont(bamItemTypeLabel.getFont().deriveFont(Font.BOLD));
        bamItemNameField.setText("Unnamed");
        // bamItemNameField.setFont(bamItemNameField.getFont().deriveFont(Font.BOLD));

        LgElement.registerTextFieldPlaceholder(bamItemNameField, "ui", "name");
        LgElement.registerTextFieldPlaceholder(bamItemDescriptionField, "ui", "description");

        headerPanel.insertChild(bamItemTypeLabel, 0, 0);
        headerPanel.insertChild(bamItemNameField, 1, 0, ANCHOR.C, FILL.H);
        headerPanel.insertChild(cloneButton, 2, 0, ANCHOR.C, FILL.H);
        headerPanel.insertChild(deleteButton, 3, 0, ANCHOR.C, FILL.H);
        headerPanel.insertChild(bamItemDescriptionField, 0, 1, 4, 1);

        contentPanel = new RowColPanel();

        insertChild(headerPanel, 0, 0);
        insertChild(new JSeparator(), 0, 1);
        insertChild(contentPanel, 0, 2);

        setColWeight(0, 1);
        setRowWeight(2, 1);

    }

    public void setContent(Component component) {
        this.contentPanel.clear();
        this.contentPanel.appendChild(component);
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
        json.put("name", bamItemNameField.getText());
        json.put("description", bamItemDescriptionField.getText());
        json.put("content", toJSON());
        return json;
    }

    public void fromFullJSON(JSONObject json) {
        bamItemNameField.setText(json.getString("name"));
        bamItemDescriptionField.setText(json.getString("description"));
        fromJSON(json.getJSONObject("content"));
    }

    @Override
    public String toString() {
        return "BamItem | " + TYPE + " | " + bamItemNameField.getText() + " (" + ID + ")";
    }

    public static boolean areMatching(JSONObject jsonA, JSONObject jsonB, String[] keys, boolean exclude) {

        String[] namesA = JSONObject.getNames(jsonA);
        String[] namesB = JSONObject.getNames(jsonB);

        if (namesA == null || namesB == null) {
            System.err.println("At least one of the JSON object to compare is empty!");
            return namesA == null && namesB == null;
        }

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

        printJsonStrings(jsonStringA, jsonStringB);

        return jsonStringA.equals(jsonStringB);
    }

    public boolean isMatchingWith(String jsonString, String[] keys, boolean exclude) {
        return isMatchingWith(new JSONObject(jsonString), keys, exclude);
    }

    public boolean isMatchingWith(JSONObject json, String[] keys, boolean exclude) {
        return areMatching(toJSON(), json, keys, exclude);
    }

    private static void printJsonStrings(String jsonA, String jsonB) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(jsonA);
        System.out.println("------------------------------------------------");
        System.out.println(jsonB);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
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

    public void setCopyName() {
        String oldName = bamItemNameField.getText();
        String newName = Lg.getText("ui", "copy_of");
        newName = Lg.format(newName, oldName);
        bamItemNameField.setText(newName);
    }
}
