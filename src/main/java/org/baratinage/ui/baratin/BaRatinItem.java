package org.baratinage.ui.baratin;

import java.awt.Component;

import javax.swing.JSeparator;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.container.RowColPanel;

public abstract class BaRatinItem extends BamItem {

    private NameDescriptionItemHeader header;
    private RowColPanel contentContainer;
    private RowColPanel content;

    public BaRatinItem(BamItemType type, String uuid, BaratinProject project) {
        super(type, uuid, project);

        header = new NameDescriptionItemHeader();
        header.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("name")) {
                String newName = (String) e.getNewValue();
                super.setName(newName);// must use super!
            }
        });
        header.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("description")) {
                String newDesc = (String) e.getNewValue();
                super.setDescription(newDesc);// must use super!
            }
        });

        contentContainer = new RowColPanel(RowColPanel.AXIS.COL);
        contentContainer.appendChild(header, 0);
        contentContainer.appendChild(new JSeparator(), 0);

        content = new RowColPanel();

        contentContainer.appendChild(content, 1);

        super.setContent(contentContainer);

    }

    protected void setNameFieldLabel(String label) {
        header.setNameFieldLabel(label);
    }

    protected void setDescriptionFieldLabel(String label) {
        header.setDescriptionFieldLabel(label);
    }

    @Override
    public void setContent(Component component) {
        content.clear();
        content.appendChild(component, 1);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        header.setName(name);
    }

    @Override
    public void setDescription(String desc) {
        super.setDescription(desc);
        header.setDescription(desc);
    }
}
