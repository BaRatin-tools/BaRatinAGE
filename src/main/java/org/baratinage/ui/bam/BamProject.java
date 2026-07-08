package org.baratinage.ui.bam;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

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
import org.baratinage.utils.perf.Performance;
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
            return true;
        }
        Performance.startTimeMonitoring("checkUnsavedChange");
        BamConfig currentConfig = BamProjectSaver.getBamConfig(this);
        JSONFilter filter = new JSONFilter(true, true, "selectedItemId");
        JSONObject curr = filter.apply(currentConfig.JSON);
        JSONObject saved = filter.apply(lastSavedConfig.JSON);
        JSONCompareResult compareRes = JSONCompare.compare(curr, saved);
        Performance.endTimeMonitoring("checkUnsavedChange");
        if (!compareRes.matching()) {
            return true;
        }
        return false;
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

        AppSetup.SHORTCUTS.setFocusDepedentContext(EXPLORER, EXPLORER);

        AppSetup.SHORTCUTS.setBindingAction("explorer.delete", EXPLORER, () -> {
            List<BamItem> selectedBamItems = getSelectedBamItems();
            if (selectedBamItems.size() > 0) {
                String bamItemNames = selectedBamItems
                        .stream()
                        .map(e -> e.bamItemNameField.getText())
                        .collect(Collectors.joining("</li><li>", "<ul><li>", "</li></ul>"));
                int response = JOptionPane.showConfirmDialog(this,
                        T.html("delete_component_question", bamItemNames),
                        T.text("warning"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    for (BamItem bamItem : selectedBamItems) {
                        deleteBamItem(bamItem);
                    }
                }
            }
        });

        AppSetup.SHORTCUTS.setBindingAction("explorer.duplicate", EXPLORER, () -> {
            List<BamItem> selectedBamItems = getSelectedBamItems();
            for (BamItem item : selectedBamItems) {
                item.getCloneRunnable().run();
            }
        });

    }

    public JPopupMenu createExplorerContextMenu() {
        List<BamItem> selectedItems = getSelectedBamItems();
        JPopupMenu menu = new JPopupMenu();
        Set<BamItemType> bamItemTypes = new HashSet<>();
        for (BamItem item : selectedItems) {
            bamItemTypes.add(item.TYPE);
        }
        for (BamItemType type : bamItemTypes) {
            JMenuItem menuItem = AppSetup.SHORTCUTS.createMenuItem("global.create_%s".formatted(type.id));
            menuItem.setText(T.text("create_%s".formatted(type.id)));
            menuItem.setIcon(type.getAddIcon());
            menu.add(menuItem);
        }
        JMenuItem duplicateMenuItem = AppSetup.SHORTCUTS.createMenuItem("explorer.duplicate", EXPLORER);
        duplicateMenuItem.setText(T.text("duplicate_selected_components"));
        duplicateMenuItem.setIcon(AppSetup.ICONS.COPY);
        JMenuItem removeMenuItem = AppSetup.SHORTCUTS.createMenuItem("explorer.delete", EXPLORER);
        removeMenuItem.setText(T.text("remove_selected_components"));
        removeMenuItem.setIcon(AppSetup.ICONS.TRASH);
        duplicateMenuItem.setEnabled(selectedItems.size() > 0);
        removeMenuItem.setEnabled(selectedItems.size() > 0);
        menu.add(duplicateMenuItem);
        menu.add(removeMenuItem);

        return menu;
    }

    private List<BamItem> getSelectedBamItems() {
        return EXPLORER
                .getSelectedExplorerItems()
                .stream()
                .map(e -> getBamItem(e.id))
                .filter(e -> e != null)
                .collect(Collectors.toList());
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

        AppSetup.SHORTCUTS.setBindingAction("global.create_%s".formatted(itemType.id),
                () -> {
                    addBamItem(itemType);
                });

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

        EXPLORER.appendItem(explorerItem);
        EXPLORER.selectItem(explorerItem);

        return bamItem;

    }

    public BamItem addBamItem(BamItemType itemType, String uuid) {
        return addBamItem(buildBamItem(itemType, uuid));
    }

    public BamItem addBamItem(BamItemType type) {
        return addBamItem(type, Misc.getTimeStampedId());
    }

    public BamItem buildBamItem(BamItemType itemType, String uuid) {
        ProjectBamItem pBamItem = projectBamItems.get(itemType);
        if (pBamItem == null) {
            ConsoleLogger.error("Cannot find item type '" + itemType + "'!");
            return null;
        }
        BamItem bamItem = pBamItem.builder.apply(uuid);
        bamItem.bamItemNameField.setText(BAM_ITEMS.getDefaultName(itemType));
        return bamItem;
    }

    public BamItem builBamItem(BamItemType itemType) {
        return buildBamItem(itemType, Misc.getTimeStampedId());
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
