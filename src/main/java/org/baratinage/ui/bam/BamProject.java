package org.baratinage.ui.bam;

import javax.swing.BorderFactory;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import org.baratinage.ui.component.Explorer;
import org.baratinage.ui.component.ExplorerItem;
import org.baratinage.ui.container.RowColPanel;

public abstract class BamProject extends RowColPanel {

    protected BamItemList items;

    protected RowColPanel actionBar;
    protected JSplitPane content;

    protected Explorer explorer;
    protected RowColPanel currentPanel;

    public BamProject() {
        super(AXIS.COL);

        this.items = new BamItemList();
        this.actionBar = new RowColPanel(AXIS.ROW, ALIGN.START, ALIGN.STRETCH);
        this.actionBar.setPadding(5);
        this.actionBar.setGap(5);
        this.appendChild(this.actionBar, 0);

        this.content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.content.setBorder(BorderFactory.createEmptyBorder());
        this.appendChild(new JSeparator(), 0);
        this.appendChild(this.content, 1);

        this.explorer = new Explorer("Explorateur");
        this.setupExplorer();

        this.currentPanel = new RowColPanel(AXIS.COL);
        this.currentPanel.setGap(5);

        this.content.setLeftComponent(this.explorer);
        this.content.setRightComponent(this.currentPanel);
        this.content.setResizeWeight(0);

    }

    private void setupExplorer() {

        this.explorer.addTreeSelectionListener(e -> {
            ExplorerItem explorerItem = explorer.getLastSelectedPathComponent();
            if (explorerItem != null) {
                BamItem bamItem = findBamItem(explorerItem.id);
                if (bamItem != null) {
                    this.currentPanel.clear();
                    this.currentPanel.appendChild(bamItem, 1);

                } else {
                    this.currentPanel.clear();
                }
                this.updateUI();
            }
        });

    }

    public BamItemList getBamItems() {
        return this.items;
    }

    public void addItem(BamItem bamItem, ExplorerItem explorerItem) {

        items.add(bamItem);
        // bamItem.updateSiblings(items);

        bamItem.addPropertyChangeListener((p) -> {
            if (p.getPropertyName().equals("bamItemName")) {
                String newName = (String) p.getNewValue();
                if (newName.equals("")) {
                    newName = "<html><div style='color: red; font-style: italic'>Sansnom</div></html>";
                }
                explorerItem.label = newName;
                explorer.updateItemView(explorerItem);
            }
        });

        bamItem.addDeleteAction(e -> {
            deleteItem(bamItem, explorerItem);
        });

        this.explorer.appendItem(explorerItem);
        this.explorer.selectItem(explorerItem);

    }

    public void deleteItem(BamItem bamItem, ExplorerItem explorerItem) {
        items.remove(bamItem);
        this.explorer.removeItem(explorerItem);
        this.explorer.selectItem(explorerItem.parentItem);
    }

    public BamItem findBamItem(String id) {
        for (BamItem item : this.items) {
            if (item.getUUID().equals(id)) {
                return item;
            }
        }
        return null;
    }
}
