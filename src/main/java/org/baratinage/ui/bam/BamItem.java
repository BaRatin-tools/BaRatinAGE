package org.baratinage.ui.bam;

import java.util.ArrayList;
import java.util.List;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.component.Title;
import org.baratinage.ui.container.GridPanel;
import org.baratinage.ui.container.RowColPanel;

abstract public class BamItem extends GridPanel {

    public final BamItemType TYPE;
    public final String ID;
    public final BamProject PROJECT;

    public final SimpleTextField bamItemNameField;
    public final SimpleTextField bamItemDescriptionField;

    private final Title bamItemTypeLabel;
    public final JButton cloneButton;
    public final JButton deleteButton;

    private GridPanel headerPanel;
    private RowColPanel contentPanel;

    public BamItem(BamItemType type, String uuid, BamProject project) {
        TYPE = type;
        ID = uuid;
        PROJECT = project;

        bamItemTypeLabel = new Title();
        bamItemNameField = new SimpleTextField();
        bamItemDescriptionField = new SimpleTextField();

        cloneButton = getCloneBamItemBtn(new JButton(), true, true);
        deleteButton = getDeleteBamItemBtn(new JButton(), false, true);

        headerPanel = new GridPanel();
        headerPanel.setGap(5);
        headerPanel.setPadding(5);
        headerPanel.setColWeight(1, 1);
        headerPanel.setColWeight(2, 5);

        headerPanel.insertChild(bamItemTypeLabel, 0, 0);
        headerPanel.insertChild(bamItemNameField, 1, 0);
        headerPanel.insertChild(bamItemDescriptionField, 2, 0);
        headerPanel.insertChild(cloneButton, 3, 0);
        headerPanel.insertChild(deleteButton, 4, 0);

        contentPanel = new RowColPanel();

        insertChild(headerPanel, 0, 0);
        insertChild(new SimpleSep(), 0, 1);
        insertChild(contentPanel, 0, 2);

        setColWeight(0, 1);
        setRowWeight(2, 1);

        bamItemNameField.addChangeListener((e) -> {
            String newName = bamItemNameField.getText();
            ExplorerItem explorerItem = PROJECT.EXPLORER.getItem(ID);
            if (explorerItem != null) {
                explorerItem.label = newName;
                PROJECT.EXPLORER.updateItemView(explorerItem);
            }

        });
        bamItemNameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (bamItemNameField.getText().equals("")) {
                    bamItemNameField.setText(T.text("untitled"));
                }
            }
        });

        bamItemTypeLabel.setIcon(TYPE.getIcon());
        T.t(this, bamItemTypeLabel, false, TYPE.id);
        T.t(this, () -> {
            bamItemNameField.setPlaceholder(T.text("name"));
            bamItemDescriptionField.setPlaceholder(T.text("description"));
        });
    }

    public void setContent(Component component) {
        this.contentPanel.clear();
        this.contentPanel.appendChild(component);
    }

    public abstract BamConfig save(boolean writeFiles);

    public abstract void load(BamConfig config);

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

    public void setCopyName() {
        String oldName = bamItemNameField.getText();
        String newName = T.text("copy_of", oldName);
        bamItemNameField.setText(newName);
    }

    public <A extends AbstractButton> A getCloneBamItemBtn(A btn, boolean label, boolean tooltip) {
        btn.addActionListener((e) -> {
            BamItem clonedBamItem = PROJECT.addBamItem(TYPE);
            clonedBamItem.load(save(false));
            clonedBamItem.bamItemNameField.setText(bamItemNameField.getText());
            clonedBamItem.bamItemDescriptionField.setText(bamItemDescriptionField.getText());
            clonedBamItem.setCopyName();
        });
        btn.setIcon(AppSetup.ICONS.COPY);
        if (label) {
            T.t(this, btn, false, "duplicate");
        }
        if (tooltip) {
            T.t(this, () -> {
                btn.setToolTipText(T.text("duplicate"));
            });
        }
        return btn;
    }

    public <A extends AbstractButton> A getDeleteBamItemBtn(A btn, boolean label, boolean tooltip) {
        btn.addActionListener((e) -> {
            int response = JOptionPane.showConfirmDialog(this,
                    T.html("delete_component_question", bamItemNameField.getText()),
                    T.text("warning"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                PROJECT.deleteBamItem(this);
            }
        });
        btn.setIcon(AppSetup.ICONS.TRASH);
        if (label) {
            T.t(this, btn, false, "delete");
        }
        if (tooltip) {
            T.t(this, () -> {
                btn.setToolTipText(T.text("delete"));
            });
        }
        return btn;
    }

    public static <A extends AbstractButton> A getAddBamItemBtn(
            A btn, BamProject project, BamItemType bamItemType,
            boolean label, boolean tooltip) {
        btn.addActionListener((e) -> {
            project.addBamItem(bamItemType);
        });
        btn.setIcon(bamItemType.getAddIcon());
        if (label) {
            T.t(project, btn, false, "create_" + bamItemType.id);
        }
        if (tooltip) {
            T.t(project, () -> {
                btn.setToolTipText(T.text("create_" + bamItemType.id));
            });
        }
        return btn;
    }

}
