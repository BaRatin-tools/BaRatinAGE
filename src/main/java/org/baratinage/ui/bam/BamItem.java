package org.baratinage.ui.bam;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONObject;

abstract public class BamItem extends GridPanel {

    // FIXME: should be in its own file.
    static public enum ITEM_TYPE {
        EMPTY_ITEM,
        HYRAULIC_CONFIG, // FIXME: typo
        GAUGINGS,
        HYDROGRAPH,
        LIMNIGRAPH,
        RATING_CURVE,
        STRUCTURAL_ERROR,
        IMPORTED_DATASET;
    };

    public final ITEM_TYPE TYPE;
    public final String ID;
    private String name = "";
    private String description = "";

    private BamItemList children;

    private JLabel titleLabel;
    private JButton deleteButton;
    private GridPanel headerPanel;
    private RowColPanel contentPanel;

    public BamItem(ITEM_TYPE type, String uuid) {
        // super(AXIS.COL);
        TYPE = type;
        ID = uuid;

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
        deleteButton = new JButton("Supprimer");

        headerPanel.insertChild(titleLabel, 0, 0);
        headerPanel.insertChild(deleteButton, 1, 0);

        this.children = new BamItemList();
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

    public void hasChanged() {
        for (BamItem child : this.children) {
            child.parentHasChanged(this);
        }
    }

    public void addBamItemChild(BamItem childBamItem) {
        children.add(childBamItem);
    }

    public void removeBamItemChild(BamItem childBamItem) {
        children.remove(childBamItem);
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

    public abstract void parentHasChanged(BamItem parent);

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
        // json.put("backups", backups);
        return json;
    }

    public void fromFullJSON(JSONObject json) {
        name = json.getString("name");
        description = json.getString("description");

        // JSONObject backupsJson = json.getJSONObject("backups");
        // Iterator<String> it = backupsJson.keys();
        // while (it.hasNext()) {
        // String key = it.next();
        // backups.put(key, backupsJson.getString(key));
        // }

        fromJSON(json.getJSONObject("content"));
    }

    @Override
    public String toString() {
        return "BamItem | " + TYPE + " | " + name + " (" + ID + ")";
    }

    // private HashMap<String, String> backups = new HashMap<>();

    // public void createBackup(String id) {
    // backups.put(id, toJSON().toString());
    // }

    // public boolean hasBackup(String id) {
    // return backups.containsKey(id);
    // }

    // public JSONObject getBackup(String id) {
    // String backupString = backups.get(id);
    // return backupString == null ? null : new JSONObject(backupString);
    // }

    // public void deleteBackup(String id) {
    // backups.remove(id);
    // }

    // public boolean isMatchingWith(String backupId, String[] keys, boolean
    // exclude) {
    // if (!hasBackup(backupId)) {
    // return false;
    // }
    // return isMatchingWith(getBackup(backupId), keys, exclude);
    // }

    public static boolean areMatching(JSONObject jsonA, JSONObject jsonB, String[] keys, boolean exclude) {
        // JSONObject currentStateJson = toJSON();
        // JSONObject compareStateJson = json;

        if (exclude) {
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

    // @Deprecated
    // public boolean isBackupInSyncIgnoringKeys(String id, String[] keysToIgnore) {
    // JSONObject currentStateJson = toJSON();
    // JSONObject backupStateJson = getBackup(id);
    // if (backupStateJson == null) {
    // System.out.println("no backup with id '" + id + "'!");
    // return true;
    // }

    // for (String key : keysToIgnore) {
    // if (currentStateJson.has(key)) {
    // currentStateJson.remove(key);
    // }
    // if (backupStateJson.has(key)) {
    // backupStateJson.remove(key);
    // }
    // }

    // String currentState = currentStateJson.toString();
    // String backupState = backupStateJson.toString();

    // System.out.println("***********************************");
    // System.out.println(currentState);
    // System.out.println("--");
    // System.out.println(backupState);
    // System.out.println("***********************************");

    // return currentState.equals(backupState);
    // }

    // @Deprecated
    // public boolean isBackupInSyncIncludingKeys(String id, String[] keysToInclude)
    // {
    // JSONObject currentStateJson = toJSON();
    // JSONObject backupStateJson = getBackup(id);
    // if (backupStateJson == null) {
    // System.out.println("no backup with id '" + id + "'!");
    // return true;
    // }

    // JSONObject filteredCurrentStateJson = new JSONObject();
    // JSONObject filteredBackupStateJson = new JSONObject();
    // for (String key : keysToInclude) {
    // if (currentStateJson.has(key)) {
    // filteredCurrentStateJson.put(key, currentStateJson.get(key));
    // }
    // if (backupStateJson.has(key)) {
    // filteredBackupStateJson.put(key, backupStateJson.get(key));
    // }
    // }
    // currentStateJson = filteredCurrentStateJson;
    // backupStateJson = filteredBackupStateJson;

    // String currentState = currentStateJson.toString();
    // String backupState = backupStateJson.toString();
    // return currentState.equals(backupState);
    // }

    // @Deprecated
    // public boolean isBackupInSync(String id) {
    // JSONObject currentStateJson = toJSON();
    // JSONObject backupStateJson = getBackup(id);
    // if (backupStateJson == null) {
    // System.out.println("no backup with id '" + id + "'!");
    // return true;
    // }

    // String currentState = currentStateJson.toString();
    // String backupState = backupStateJson.toString();
    // return currentState.equals(backupState);
    // }
}
