package org.baratinage.ui.baratin;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.commons.ExplorerItem;
import org.baratinage.ui.lg.Lg;

import org.json.JSONArray;
import org.json.JSONObject;

public class BaratinProject extends BamProject {

    private JMenu projectMenu;

    public BaratinProject() {
        super();

        if (AppConfig.AC.APP_MAIN_FRAME.baratinMenu == null) {
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu = new JMenu();
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu.setText("BaRatin");
            AppConfig.AC.APP_MAIN_FRAME.mainMenuBar.add(AppConfig.AC.APP_MAIN_FRAME.baratinMenu);
        } else {
            AppConfig.AC.APP_MAIN_FRAME.baratinMenu.removeAll();
        }
        projectMenu = AppConfig.AC.APP_MAIN_FRAME.baratinMenu;

        BamItemType.HYDRAULIC_CONFIG.setBuilderFunction((String uuid) -> {
            return new HydraulicConfiguration(uuid, this);
        });
        BamItemType.GAUGINGS.setBuilderFunction((String uuid) -> {
            return new Gaugings(uuid, this);
        });
        BamItemType.STRUCTURAL_ERROR.setBuilderFunction((String uuid) -> {
            return new StructuralError(uuid, this);
        });
        BamItemType.RATING_CURVE.setBuilderFunction((String uuid) -> {
            return new RatingCurve(uuid, this);
        });
        BamItemType.LIMNIGRAPH.setBuilderFunction((String uuid) -> {
            return new Limnigraph(uuid, this);
        });
        BamItemType.HYDROGRAPH.setBuilderFunction((String uuid) -> {
            return new Hydrograph(uuid, this);
        });

        initBamItemType(
                BamItemType.HYDRAULIC_CONFIG,
                "hydraulic_config",
                "create_hydraulic_config");

        initBamItemType(
                BamItemType.GAUGINGS,
                "gaugings",
                "create_gaugings");

        initBamItemType(
                BamItemType.STRUCTURAL_ERROR,
                "structural_error_model",
                "create_structural_error_model");

        initBamItemType(
                BamItemType.RATING_CURVE,
                "rating_curve",
                "create_rating_curve");

        initBamItemType(
                BamItemType.LIMNIGRAPH,
                "limnigraph",
                "create_limnigraph");

        initBamItemType(
                BamItemType.HYDROGRAPH,
                "hydrograph",
                "create_hydrograph");
    }

    private void initBamItemType(BamItemType itemType, String lgKey, String lgCreateItemKey) {

        ActionListener onAdd = (e) -> {
            addBamItem(itemType);
        };

        JMenuItem menuButton = new JMenuItem();
        Lg.register(menuButton, lgCreateItemKey);
        menuButton.setIcon(itemType.getAddIcon());
        menuButton.addActionListener(onAdd);
        projectMenu.add(menuButton);

        JButton toolbarButton = new JButton(itemType.getAddIcon());
        toolbarButton.addActionListener(onAdd);
        toolBar.add(toolbarButton);

        ExplorerItem explorerItem = new ExplorerItem(
                itemType.id,
                itemType.id,
                itemType.getIcon());
        this.explorer.appendItem(explorerItem);
        Lg.register(explorerItem, () -> {
            explorerItem.label = Lg.text(lgKey);
            explorer.updateItemView(explorerItem);
        });
    }

    public void addDefaultBamItems() {
        addBamItem(BamItemType.STRUCTURAL_ERROR);
        addBamItem(BamItemType.HYDRAULIC_CONFIG);
    }

    public BamItem addBamItem(BamItem item) {

        ExplorerItem explorerItem = new ExplorerItem(
                item.ID,
                item.bamItemNameField.getText(),
                item.TYPE.getIcon(),
                explorer.getItem(item.TYPE.id));

        addItem(item, explorerItem);

        Lg.register(item.bamItemTypeLabel, item.TYPE.id);

        item.bamItemTypeLabel.setIcon(item.TYPE.getIcon());
        item.cloneButton.addActionListener((e) -> {
            BamItem clonedItem = item.clone();
            clonedItem.setCopyName();
            addBamItem(clonedItem);
        });

        return item;
    }

    public BamItem addBamItem(BamItemType type, String uuid) {
        BamItem item = type.buildBamItem(uuid);
        item.bamItemNameField.setText(BAM_ITEMS.getDefaultName(type));
        return addBamItem(item);
    }

    public BamItem addBamItem(BamItemType type) {
        BamItem item = type.buildBamItem();
        item.bamItemNameField.setText(BAM_ITEMS.getDefaultName(type));
        return addBamItem(item);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("model", "baratin");
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        JSONArray items = json.getJSONArray("items");

        BamItemType[][] steps = new BamItemType[][] {
                new BamItemType[] {
                        BamItemType.HYDRAULIC_CONFIG,
                        BamItemType.GAUGINGS,
                        BamItemType.STRUCTURAL_ERROR,
                        BamItemType.LIMNIGRAPH
                },
                new BamItemType[] {
                        BamItemType.RATING_CURVE
                },
                new BamItemType[] {
                        BamItemType.HYDROGRAPH
                }
        };

        for (BamItemType[] step : steps) {
            for (Object item : items) {

                JSONObject jsonObj = (JSONObject) item;
                BamItemType itemType = BamItemType.valueOf(jsonObj.getString("type"));
                String uuid = jsonObj.getString("uuid");

                BamItem bamItem;
                if (itemType.matchOneOf(step)) {
                    bamItem = addBamItem(itemType, uuid);
                } else {
                    continue;
                }
                bamItem.fromFullJSON(jsonObj);
            }
        }

    }

    private String projectPath = null;

    @Override
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public String getProjectPath() {
        return projectPath;
    }
}
