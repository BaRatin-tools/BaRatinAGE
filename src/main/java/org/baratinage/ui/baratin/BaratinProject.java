package org.baratinage.ui.baratin;

import javax.swing.JMenu;

import org.baratinage.ui.AppConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.json.JSONArray;
import org.json.JSONObject;

public class BaratinProject extends BamProject {

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

        initBamItemType(
                BamItemType.HYDRAULIC_CONFIG,
                (String uuid) -> {
                    return new HydraulicConfiguration(uuid, this);
                });

        initBamItemType(
                BamItemType.GAUGINGS,
                (String uuid) -> {
                    return new Gaugings(uuid, this);
                });

        initBamItemType(
                BamItemType.STRUCTURAL_ERROR,
                (String uuid) -> {

                    return new StructuralErrorModelBamItem(
                            uuid,
                            this,
                            new String[] { "Q", "m<sup>3</sup>.s<sup>-1</sup>" });
                });

        initBamItemType(
                BamItemType.RATING_CURVE,
                (String uuid) -> {
                    return new RatingCurve(uuid, this);
                });

        initBamItemType(
                BamItemType.LIMNIGRAPH,
                (String uuid) -> {
                    return new Limnigraph(uuid, this);
                });

        initBamItemType(
                BamItemType.HYDROGRAPH,
                (String uuid) -> {
                    return new Hydrograph(uuid, this);
                });
    }

    public void addDefaultBamItems() {
        addBamItem(BamItemType.STRUCTURAL_ERROR);
        addBamItem(BamItemType.HYDRAULIC_CONFIG);
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
                    System.out.println(
                            "BaRatinProject: importing item of type '" + itemType + "' with id '" + uuid + "'...");
                    bamItem = addBamItem(itemType, uuid);
                } else {
                    continue;
                }
                bamItem.fromFullJSON(jsonObj);
            }
        }
        System.out.println("BaratinProject: all bam items imported with success.");
    }

}
