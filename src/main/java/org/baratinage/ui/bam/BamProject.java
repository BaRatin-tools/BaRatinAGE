package org.baratinage.ui.bam;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Function;

import javax.swing.JMenuItem;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.Explorer;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SplitContainer;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.json.JSONCompare;
import org.baratinage.utils.json.JSONCompareResult;
import org.baratinage.utils.json.JSONFilter;
import org.json.JSONObject;

public abstract class BamProject extends SimpleFlowPanel {

    public final String ID;
    public final BamProjectType PROJECT_TYPE;
    public final BamItemList BAM_ITEMS;

    protected final Explorer EXPLORER;

    private String projectPath = null;

    protected final SplitContainer content;
    protected final SimpleFlowPanel currentPanel;

    private BamConfig lastSavedConfig;
    private boolean unsavedChanges = false;

    public BamProject(BamProjectType projectType) {
        super(true);

        // main instance public object
        ID = Misc.getTimeStampedId();
        PROJECT_TYPE = projectType;
        BAM_ITEMS = new BamItemList();

        // setting explorer
        EXPLORER = new Explorer();

        setupExplorer();

        // inialialize current panel, place holder for bam item panels
        currentPanel = new SimpleFlowPanel(true);
        currentPanel.setGap(5);

        // final layout
        content = new SplitContainer(EXPLORER, currentPanel, true);
        addChild(content, true);
        content.setLeftComponent(EXPLORER);
        content.setRightComponent(currentPanel);
        content.setResizeWeight(0);

        // inialialize last save record
        lastSavedConfig = null;
    }

    public void setLastSavedConfig() {
        lastSavedConfig = BamProjectSaver.getBamConfig(this);
    }

    public boolean checkUnsavedChange() {
        if (lastSavedConfig == null) {
            unsavedChanges = true;
            return true;
        }
        BamConfig currentConfig = BamProjectSaver.getBamConfig(this);
        JSONFilter filter = new JSONFilter(true, true, "selectedItemId");
        JSONObject curr = filter.apply(currentConfig.JSON);
        JSONObject saved = filter.apply(lastSavedConfig.JSON);
        JSONCompareResult compareRes = JSONCompare.compare(curr, saved);
        if (!compareRes.matching() != unsavedChanges) {
            unsavedChanges = !compareRes.matching();
            return true;
        }
        return false;
    }

    public boolean hasUnsavedChange() {
        return unsavedChanges;
    }

    private void setupExplorer() {

        EXPLORER.addTreeSelectionListener(e -> {
            ExplorerItem explorerItem = EXPLORER.getLastSelectedPathComponent();
            if (explorerItem != null) {
                BamItem bamItem = getBamItem(explorerItem.id);
                if (bamItem != null) {
                    currentPanel.removeAll();
                    currentPanel.addChild(bamItem, true);
                } else {
                    ConsoleLogger.log("selected BamItem is null");
                    currentPanel.removeAll();
                }
                updateUI();
            }
        });

    }

    public void setCurrentBamItem(BamItem bamItem) {
        ExplorerItem item = EXPLORER.getItem(bamItem.ID);
        if (item != null) {
            EXPLORER.selectItem(item);
        }
    }

    private record ProjectBamItem(BamItemType type, String categoryId, Function<String, BamItem> builder) {
    };

    private final HashMap<BamItemType, ProjectBamItem> projectBamItems = new HashMap<>();

    protected void initBamItemType(
            BamItemType itemType,
            String categoryId,
            Function<String, BamItem> bamItemBuilder) {

        ExplorerItem categoryItem = EXPLORER.getItem(categoryId);
        if (categoryItem == null) {
            ExplorerItem newCategoryItem = new ExplorerItem(
                    categoryId,
                    "",
                    AppSetup.ICONS.getCustomAppImageIcon(categoryId + ".svg"));
            T.t(this, () -> {
                newCategoryItem.label = T.text(categoryId);
                EXPLORER.updateItemView(newCategoryItem);
            });
            categoryItem = newCategoryItem;
            EXPLORER.appendItem(categoryItem);
        }

        projectBamItems.put(itemType, new ProjectBamItem(itemType, categoryId, bamItemBuilder));

    }

    protected BamItem addBamItem(BamItem bamItem) {

        ProjectBamItem pBamItem = projectBamItems.get(bamItem.TYPE);
        if (pBamItem == null) {
            ConsoleLogger.error("Cannot find item type '" + bamItem.TYPE + "'!");
            return null;
        }

        ExplorerItem explorerItem = new ExplorerItem(
                bamItem.ID,
                bamItem.bamItemNameField.getText(),
                bamItem.TYPE.getIcon(),
                EXPLORER.getItem(pBamItem.categoryId));

        T.updateHierarchy(this, bamItem);

        BAM_ITEMS.add(bamItem);

        explorerItem.contextMenu.add(
                BamItem.getAddBamItemBtn(
                        new JMenuItem(),
                        this,
                        bamItem.TYPE,
                        true, true));

        explorerItem.contextMenu.add(bamItem.getCloneBamItemBtn(
                new JMenuItem(),
                true, true));
        explorerItem.contextMenu.add(bamItem.getDeleteBamItemBtn(
                new JMenuItem(),
                true, true));

        EXPLORER.appendItem(explorerItem);
        EXPLORER.selectItem(explorerItem);

        return bamItem;

    }

    public BamItem addBamItem(BamItemType itemType, String uuid) {
        ProjectBamItem pBamItem = projectBamItems.get(itemType);
        if (pBamItem == null) {
            ConsoleLogger.error("Cannot find item type '" + itemType + "'!");
            return null;
        }
        BamItem bamItem = pBamItem.builder.apply(uuid);
        bamItem.bamItemNameField.setText(BAM_ITEMS.getDefaultName(itemType));
        return addBamItem(bamItem);
    }

    public BamItem addBamItem(BamItemType type) {
        return addBamItem(type, Misc.getTimeStampedId());
    }

    protected void deleteBamItem(BamItem bamItem) {
        ExplorerItem explorerItem = EXPLORER.getItem(bamItem.ID);
        BAM_ITEMS.remove(bamItem);
        EXPLORER.removeItem(explorerItem);
        EXPLORER.selectItem(explorerItem.parentItem);
        T.clear(bamItem);
    }

    public BamItem getBamItem(String id) {
        for (BamItem item : BAM_ITEMS) {
            if (item.ID.equals(id)) {
                return item;
            }
        }
        return null;
    }

    // needs to return the item in the order in wich they must be loaded
    public abstract BamItemList getOrderedBamItemList();

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getProjectName() {
        if (projectPath == null) {
            return "";
        }
        String fileName = Path.of(projectPath).getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileName = fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
}
