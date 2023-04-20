package org.baratinage.ui.baratin;

import javax.swing.JLabel;

import org.baratinage.ui.component.TextField;
import org.baratinage.ui.container.GridPanel;

public class NameDescriptionItemHeader extends GridPanel {

    JLabel nameFieldLabel;
    TextField nameField;
    JLabel descFieldLabel;
    TextField descField;

    public NameDescriptionItemHeader() {
        super();
        setGap(5);
        setPadding(5);
        setRowWeight(0, 1);
        setColWeight(1, 1);

        nameFieldLabel = new JLabel("Name");
        nameField = new TextField();
        nameField.addChangeListener(nt -> {
            firePropertyChange("name", null, nt);
        });

        descFieldLabel = new JLabel("Description");
        descField = new TextField();
        descField.addChangeListener(nt -> {
            firePropertyChange("description", null, nt);
        });

        insertChild(nameFieldLabel, 0, 0);
        insertChild(nameField, 1, 0);
        insertChild(descFieldLabel, 0, 1);
        insertChild(descField, 1, 1);
    }

    public void setName(String name) {
        nameField.setText(name);
    }

    public String getName() {
        return nameField == null ? "" : nameField.getText();
    }

    public void setDescription(String name) {
        descField.setText(name);
    }

    public String getDescription() {
        return descField.getText();
    }

    public void setNameFieldLabel(String label) {
        nameFieldLabel.setText(label);
    }

    public void setDescriptionFieldLabel(String label) {
        descFieldLabel.setText(label);
    }
}
