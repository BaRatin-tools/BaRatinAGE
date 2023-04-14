package org.baratinage.ui.baratin;

import java.awt.Component;

import javax.swing.JSeparator;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.container.RowColPanel;

public abstract class BaRatinItem extends BamItem {

    private NameDescriptionItemHeader header;
    private RowColPanel contentContainer;
    private RowColPanel content;

    public BaRatinItem(int type) {
        super(type);

        header = new NameDescriptionItemHeader();
        header.addPropertyChangeListener(e -> {
            if (e.getPropertyName().equals("name")) {
                String newName = (String) e.getNewValue();
                super.setName(newName); // must use super!
                setTitle(newName);
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
        setTitle(name);
        header.setName(name);
    }
}
