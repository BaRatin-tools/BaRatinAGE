package org.baratinage.project_importer;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.baratin.BaratinProject;
import org.baratinage.ui.baratin.HydraulicConfiguration;
import org.baratinage.ui.baratin.Hydrograph;
import org.baratinage.ui.baratin.RatingCurve;
import org.baratinage.ui.baratin.gaugings.GaugingsDataset;
import org.baratinage.ui.baratin.limnigraph.LimnigraphDataset;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.ProgressFrame;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.DateTime;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.DirUtils;
import org.baratinage.utils.fs.ReadWriteZip;
import org.json.JSONArray;
import org.json.JSONObject;

public class BaratinageV2Importer implements IProjectImporter {

    private boolean importCanceled;
    private Path TEMP_DIR;
    private ProgressFrame mainProgressFrame;
    private JLabel mainProgressLabel;
    private BaratinProject project;
    private String barZipFilePath;
    private HashMap<String, String> strucErrorBamItemIds;
    private Consumer<BamProject> finalBamProjectConsumer;

    private record Task(int weight, ICallbackRunnable runnable) {
    };

    private List<Task> taskList;

    public BaratinageV2Importer() {
        String id = Misc.getTimeStampedId();
        TEMP_DIR = Path.of(System.getProperty("java.io.tmpdir"),
                "baratinage",
                "convertion_" + id);
        mainProgressFrame = new ProgressFrame();
        mainProgressFrame.addOnCancelAction(() -> {
            importCanceled = true;
        });
        mainProgressLabel = new JLabel();
    }

    private interface ICallbackRunnable {
        public void run(Consumer<Float> onProgress, Runnable onDone, Runnable onError);
    }

    private interface IBamItemAdder {
        public void addBamItem(File folder, Consumer<Float> onProgress, Runnable onDone, Runnable onError);
    }

    private void addBamItems(File parentFolder, IBamItemAdder bamItemAdderFromFolder, BamItemType type) {

        if (parentFolder != null) {
            File[] folders = parentFolder.listFiles();

            String messageTemplate = "<html><b>%s</b> - %s</html>";

            if (folders != null) {

                for (File f : folders) {
                    taskList.add(new Task(0, (onProgress, onDone, onError) -> {
                        try {
                            mainProgressLabel.setIcon(type.getIcon());
                            mainProgressLabel.setText(
                                    String.format(messageTemplate,
                                            T.text(type.id),
                                            BaratinageV2Builders.getBamItemNameFromFolder(f)));
                            onDone.run();
                        } catch (Exception e) {
                            ConsoleLogger.error("Error import item '" + type + "' from '" + f + "'...\n" + e);
                            CommonDialog.errorDialog(T.text("import_v2_project_error"));
                            mainProgressFrame.done();
                        }
                    }));

                    taskList.add(new Task(100, (onProgress, onDone, onError) -> {

                        ConsoleLogger.log("importing V2 project item from '" + f.getPath() + "'...");

                        bamItemAdderFromFolder.addBamItem(f, onProgress, onDone, onError);

                    }));
                }
            }
        }
    }

    @Override
    public void importProject(String barZipFilePath, Consumer<BamProject> bamProjectConsumer) {

        this.barZipFilePath = barZipFilePath;
        taskList = new ArrayList<>();
        project = new BaratinProject();
        strucErrorBamItemIds = new HashMap<>();
        finalBamProjectConsumer = bamProjectConsumer;

        ReadWriteZip.unzip(barZipFilePath, TEMP_DIR.toString());

        File tempDirFile = TEMP_DIR.toFile();
        File[] allSubFiles = tempDirFile.listFiles();

        File hydraulicConfigFolder = BaratinageV2Builders.getSubFolder(allSubFiles, "HydraulicConfiguration");
        addBamItems(hydraulicConfigFolder, this::addHydraulicConfiguration, BamItemType.HYDRAULIC_CONFIG);

        File gaugingsFolder = BaratinageV2Builders.getSubFolder(allSubFiles, "GaugingSet");
        addBamItems(gaugingsFolder, this::addGaugingSet, BamItemType.GAUGINGS);

        File ratingCurveFolder = BaratinageV2Builders.getSubFolder(allSubFiles, "RatingCurve");
        addBamItems(ratingCurveFolder, this::addRatingCurve, BamItemType.RATING_CURVE);

        File limnigraphFolder = BaratinageV2Builders.getSubFolder(allSubFiles, "Limnigraph");
        addBamItems(limnigraphFolder, this::addLimnigraph, BamItemType.LIMNIGRAPH);

        File hydrographFolder = BaratinageV2Builders.getSubFolder(allSubFiles, "Hydrograph");
        addBamItems(hydrographFolder, this::addHydrograph, BamItemType.HYDROGRAPH);

        runTasks();
    }

    private int currentTask = 0;
    private int nTotalProgress;
    private int nCurrentProgress;

    private void runTasks() {
        currentTask = 0;
        importCanceled = false;
        nTotalProgress = 0;
        nCurrentProgress = 0;
        for (Task t : taskList) {
            nTotalProgress += t.weight;
        }

        RowColPanel progressPanel = new RowColPanel(RowColPanel.AXIS.COL);
        progressPanel.setGap(10);
        mainProgressLabel.setIcon(AppSetup.ICONS.BARATINAGE);
        mainProgressLabel.setText("");

        JLabel mainLabel = new JLabel();
        String message = String.format("<html>%s<br><code>%s</code></html>",
                T.text("importing_baratinage_v2_project"),
                barZipFilePath);
        mainLabel.setText(message);
        progressPanel.appendChild(mainLabel, 0);
        progressPanel.appendChild(mainProgressLabel, 0);
        mainProgressFrame.openProgressFrame(
                AppSetup.MAIN_FRAME,
                progressPanel,
                T.text("importing"),
                0,
                nTotalProgress,
                true);

        runNextTask();
    }

    private void runNextTask() {
        if (currentTask < taskList.size()) {
            Task t = taskList.get(currentTask);
            t.runnable.run(
                    (progress) -> {
                        int percentProgress = (int) (progress * t.weight);
                        int p = nCurrentProgress + percentProgress;
                        mainProgressFrame.updateProgress(p);
                    },
                    () -> {
                        if (importCanceled) {
                            ConsoleLogger.log("Import canceled!");
                            return;
                        }
                        nCurrentProgress += t.weight;
                        currentTask++;

                        runNextTask();
                    },
                    () -> {
                        mainProgressFrame.cancel();
                        ConsoleLogger.error("An error occured while running task.");
                    });

        } else {
            cleanup();
            mainProgressFrame.done();
            finalBamProjectConsumer.accept(project);
            ConsoleLogger.log("Importing BaRatinAGE V2.x project done.");
        }
    }

    private void cleanup() {
        taskList = null;
        DirUtils.deleteDir(TEMP_DIR.toFile());
    }

    private void addHydrograph(File folder, Consumer<Float> onProgress, Runnable onDone, Runnable onError) {
        BamItem bamItem = project.addBamItem(BamItemType.HYDROGRAPH);

        String[] properties = BaratinageV2Builders.readConfigFileLines(folder, "Properties.txt");

        if (properties.length < 5) {
            ConsoleLogger.error(
                    "Properties.txt file should contain at least 5 rows (name, description, type?, name of rating curve, name of limngiraph).");
            onError.run();
            return;
        }

        // ***********************
        // name & description
        BaratinageV2Builders.setBamItemNameAndDescription(bamItem, properties);

        // ***********************
        // main config
        JSONObject json = new JSONObject();

        // > ratingCurve
        String rcId = BaratinageV2Builders.findBamItemIdFromName(project, properties[4], BamItemType.RATING_CURVE);
        JSONObject ratingCurve = BaratinageV2Builders.buildBamItemParentConfig(project, rcId);
        json.put("ratingCurve", ratingCurve);

        // > limnigraph
        String limniId = BaratinageV2Builders.findBamItemIdFromName(project, properties[3], BamItemType.LIMNIGRAPH);
        JSONObject limnigraph = BaratinageV2Builders.buildBamItemParentConfig(project, limniId);
        json.put("limnigraph", limnigraph);

        // > backup
        // *ignored*

        bamItem.load(new BamConfig(json));

        Hydrograph h = (Hydrograph) bamItem;
        h.runBam.description = String.format("<html><b>%s</b> %s</html>",
                T.text(BamItemType.HYDROGRAPH.id), bamItem.bamItemNameField.getText());

        h.runBam.runAsync(onProgress, onDone, onError);

    }

    private void addLimnigraph(File folder, Consumer<Float> onProgress, Runnable onDone, Runnable onError) {
        BamItem bamItem = project.addBamItem(BamItemType.LIMNIGRAPH);

        String[] properties = BaratinageV2Builders.readConfigFileLines(folder, "Properties.txt");

        if (properties.length < 3) {
            ConsoleLogger.error(
                    "Properties.txt file should contain at least 3 rows (name, description, path to source limnigraph file).");
            onError.run();
            return;
        }

        // ***********************
        // name & description
        BaratinageV2Builders.setBamItemNameAndDescription(bamItem, properties);

        // ***********************
        // main config
        JSONObject json = new JSONObject();
        // > limniDataset

        List<double[]> limnigraph = BaratinageV2Builders.readMatrixConfigFile(folder, "Limnigraph.txt");

        if (limnigraph.size() < 11) {
            ConsoleLogger.error(
                    "Limnigraph.txt file should contain at least 11 columns.");
            onError.run();
            return;
        }
        int nTimeSteps = limnigraph.get(0).length;
        if (nTimeSteps <= 0) {
            ConsoleLogger.error(
                    "Limnigraph.txt file should contain at least 1 row.");
            onError.run();
            return;
        }

        String dataFileSourceName = "";
        if (!properties[2].equals("")) {
            Path dataFileSourcePath = DirUtils.parsePathFromUnknownOSorigin(properties[2]);
            dataFileSourceName = dataFileSourcePath.getFileName().toString();
        }

        LocalDateTime[] dateTime = DateTime.ymdhmsDoubleToTimeVector(
                limnigraph.get(0),
                limnigraph.get(1),
                limnigraph.get(2),
                limnigraph.get(3),
                limnigraph.get(4),
                limnigraph.get(5));
        double[] stage = limnigraph.get(6);
        double[] nonSysErrStd = limnigraph.get(7);
        double[] sysErrStd = limnigraph.get(9);
        int[] sysErrInd = new int[stage.length];
        for (int k = 0; k < stage.length; k++) {
            sysErrInd[k] = (int) limnigraph.get(8)[k];
        }
        LimnigraphDataset ld = new LimnigraphDataset(dataFileSourceName, dateTime, stage, nonSysErrStd,
                sysErrStd, sysErrInd);

        if (ld != null && ld.getNumberOfColumns() >= 1) {
            if (!ld.hasStageErrMatrix()) {
                ld.computeErroMatrix(AppSetup.CONFIG.N_SAMPLES.get());
            }
        }

        JSONObject limniDataset = ld.save(true).toJSON();

        json.put("limniDataset", limniDataset);

        bamItem.load(new BamConfig(json));

        onDone.run();
    }

    private void addRatingCurve(File folder, Consumer<Float> onProgress, Runnable onDone, Runnable onError) {

        BamItem bamItem = project.addBamItem(BamItemType.RATING_CURVE);

        String[] properties = BaratinageV2Builders.readConfigFileLines(folder, "Properties.txt");

        if (properties.length < 5) {
            ConsoleLogger.error(
                    "Properties.txt file should contain at least 5 rows (name, description, hydrau conf name, gauging set name, struc error name).");
            onError.run();
            return;
        }

        // ***********************
        // name & description
        BaratinageV2Builders.setBamItemNameAndDescription(bamItem, properties);

        // ***********************
        // main config
        JSONObject json = new JSONObject();

        // > hydrauConfig
        String hcId = BaratinageV2Builders.findBamItemIdFromName(project, properties[2], BamItemType.HYDRAULIC_CONFIG);
        JSONObject hydrauConfig = BaratinageV2Builders.buildBamItemParentConfig(project, hcId);
        json.put("hydrauConfig", hydrauConfig);

        // > gaugings
        String gsId = BaratinageV2Builders.findBamItemIdFromName(project, properties[3], BamItemType.GAUGINGS);
        JSONObject gaugings = BaratinageV2Builders.buildBamItemParentConfig(project, gsId);
        json.put("gaugings", gaugings);

        // > structError
        String strucErrorType = properties[4];
        String strucErrorId = null;
        if (!strucErrorType.equals("")) {
            strucErrorId = strucErrorBamItemIds.get(strucErrorType);
            if (strucErrorId == null) {
                strucErrorId = addStructuralError("Remnant_Linear");
                strucErrorBamItemIds.put(strucErrorType, strucErrorId);
            }
            JSONObject structError = new JSONObject();
            structError.put("bamItemId", strucErrorId);
            json.put("structError", structError);
        }

        // > stageGridConfig
        JSONObject stageGridConfig = BaratinageV2Builders.buildStageGridConfigFromFile(folder, false);
        json.put("stageGridConfig", stageGridConfig);

        // > bamRunId && > backup
        bamItem.load(new BamConfig(json));

        bamItem.load(new BamConfig(json));

        RatingCurve rc = (RatingCurve) bamItem;
        rc.runBam.description = String.format("<html><b>%s</b> %s</html>",
                T.text(BamItemType.RATING_CURVE.id), bamItem.bamItemNameField.getText());

        rc.runBam.runAsync(onProgress, onDone, onError);
    }

    private String addStructuralError(String type) {

        BamItem bamItem = project.addBamItem(BamItemType.STRUCTURAL_ERROR);
        JSONObject json = new JSONObject();

        // by default Remnant_Linear case: g1 + g2*Q
        Parameter gamma1 = new Parameter("gamma1", 1,
                new Distribution(DistributionType.UNIFORM, 0, 10000));
        Parameter gamma2 = new Parameter("gamma2", 0.1,
                new Distribution(DistributionType.UNIFORM, 0, 1));
        String name = "Remnant_Linear";

        if (type.equals("Remnant_Proportional")) { // g2*Q
            gamma1 = new Parameter("gamma1", 0,
                    new Distribution(DistributionType.FIXED));
            name = "Remnant_Proportional";
        } else if (type.equals("Remnant_Constant")) { // g1
            gamma2 = new Parameter("gamma2", 0,
                    new Distribution(DistributionType.FIXED));
            name = "Remnant_Constant";
        }

        JSONArray strucErrModelPanels = new JSONArray();
        // only one output: Q
        strucErrModelPanels.put(0, BaratinageV2Builders.buildPriorControlPanelConfig(true, gamma1, gamma2));
        json.put("strucErrModelPanels", strucErrModelPanels);

        bamItem.bamItemNameField.setText(name);
        bamItem.load(new BamConfig(json));
        return bamItem.ID;
    }

    private void addGaugingSet(File folder, Consumer<Float> onProgress, Runnable onDone, Runnable onError) {
        BamItem bamItem = project.addBamItem(BamItemType.GAUGINGS);

        List<double[]> gaugings = BaratinageV2Builders.readMatrixConfigFile(folder, "Gaugings.txt");
        String[] properties = BaratinageV2Builders.readConfigFileLines(folder, "Properties.txt");

        // ***********************
        // name & description

        if (properties.length < 3) {
            ConsoleLogger.error(
                    "Properties.txt file should contain at least 3 rows (name, description, gauging source file name).");
            onError.run();
            return;
        }

        BaratinageV2Builders.setBamItemNameAndDescription(bamItem, properties);

        String sourceFileName = properties[2];
        sourceFileName = Path.of(sourceFileName).getFileName().toString();

        // ***********************
        // gaugingDataset

        if (gaugings.size() < 10) {
            ConsoleLogger.error(
                    "Gaugings.txt file should contain at least 10 columns.");
            onError.run();
            return;
        }
        int nGaugings = gaugings.get(0).length;
        if (nGaugings <= 0) {
            ConsoleLogger.error(
                    "Gaugings.txt file should contain at least 1 gauging.");
            onError.run();
            return;
        }

        GaugingsDataset gds = new GaugingsDataset(sourceFileName, gaugings.get(6), gaugings.get(8), gaugings.get(9));

        Boolean[] activeState = new Boolean[nGaugings];
        for (int k = 0; k < nGaugings; k++) {
            activeState[k] = gaugings.get(10)[k] == 1.0d ? true : false;
        }

        gds.updateActiveStateValues(activeState);

        JSONObject gaugingDataset = gds.save(true).toJSON();

        JSONObject json = new JSONObject();
        json.put("gaugingDataset", gaugingDataset);

        bamItem.load(new BamConfig(json));

        onDone.run();
    }

    private void addHydraulicConfiguration(File folder, Consumer<Float> onProgress, Runnable onDone,
            Runnable onError) {

        BamItem bamItem = project.addBamItem(BamItemType.HYDRAULIC_CONFIG);

        String[] properties = BaratinageV2Builders.readConfigFileLines(folder, "Properties.txt");

        List<double[]> matrix = BaratinageV2Builders.readMatrixConfigFile(folder, "BonnifaitMatrix.txt");

        // ***********************
        // name & description
        if (properties.length < 3) {
            ConsoleLogger.error("Properties.txt file should contain at least 3 rows (name, description, ncontrols).");
            onError.run();
            return;
        }
        BaratinageV2Builders.setBamItemNameAndDescription(bamItem, properties);

        int nControls = Integer.parseInt(properties[2]);

        // ***********************
        // main configuration
        JSONObject json = new JSONObject();
        // > controlMatrix
        JSONObject controlMatrix = new JSONObject();
        json.put("controlMatrix", controlMatrix);
        // > > controlMatrixString

        int nCol = matrix.size();
        if (nCol <= 0) {
            ConsoleLogger.error("control matrix should contain at least one column.");
            onError.run();
            return;
        }
        int nRow = matrix.get(0).length;
        if (nCol != nRow) {
            ConsoleLogger.error("control matrix should be a square!");
            onError.run();
            return;
        }
        if (nCol != nControls) {
            ConsoleLogger.error("control matrix should have the same number of columns/rows as the number of controls");
            onError.run();
            return;
        }
        String stringMatrix = "";
        for (int i = 0; i < nCol; i++) {
            for (int j = 0; j < nRow; j++) {
                // FIXME: working with my example but weird, it is likely wrong
                stringMatrix += (matrix.get(j)[i] == 1.0d ? "0" : "1");
            }
            stringMatrix += ";";
        }
        controlMatrix.put("controlMatrixString", stringMatrix);

        // > > isReversed
        controlMatrix.put("isReversed", true);

        // > hydraulicControls > controls[]
        JSONObject hydraulicControls = new JSONObject();
        json.put("hydraulicControls", hydraulicControls);
        JSONArray controls = new JSONArray();
        hydraulicControls.put("controls", controls);
        for (int k = 0; k < nControls; k++) {
            JSONObject oneHydraulicControl = BaratinageV2Builders.buildOneHydraulicControlConfig(folder, k);
            if (oneHydraulicControl != null) {
                controls.put(k, oneHydraulicControl);
            }
        }
        // > stageGridConfig
        JSONObject stageGridConfig = BaratinageV2Builders.buildStageGridConfigFromFile(folder, true);
        json.put("stageGridConfig", stageGridConfig);

        // backup
        // *ignored*

        // bamRunId
        bamItem.load(new BamConfig(json));

        HydraulicConfiguration hc = (HydraulicConfiguration) bamItem;
        hc.runBam.description = String.format("<html><b>%s</b> %s</html>",
                T.text(BamItemType.HYDRAULIC_CONFIG.id), bamItem.bamItemNameField.getText());

        hc.runBam.runAsync(onProgress, onDone, onError);
    }

}
