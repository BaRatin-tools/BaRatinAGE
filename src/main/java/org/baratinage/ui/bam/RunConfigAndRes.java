package org.baratinage.ui.bam;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.baratinage.AppSetup;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.ReadWriteZip;

public class RunConfigAndRes extends BaM {

    public final String id;
    private final Path workspace;

    public static RunConfigAndRes buildFromWorkspace(String id, Path workspacePath) {

        File mainConfigFile = Path.of(workspacePath.toString(), BamFilesHelpers.CONFIG_BAM).toFile();
        // mainConfigFile.renameTo(Path.of(BamFilesHelpers.EXE_DIR,
        // BamFilesHelpers.CONFIG_BAM).toFile());

        BaM bam = BaM.buildFromWorkspace(mainConfigFile.getAbsolutePath(), workspacePath.toString());

        return new RunConfigAndRes(id, workspacePath, bam);
    }

    public static RunConfigAndRes buildFromTempZipArchive(String id) {
        String zipName = id + ".zip";
        Path zipPath = Path.of(AppSetup.PATH_APP_TEMP_DIR, zipName);
        Path workspacePath = Path.of(AppSetup.PATH_BAM_WORKSPACE_DIR, id);
        ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());
        return buildFromWorkspace(id, workspacePath);
    }

    public RunConfigAndRes createCopy(String id) {
        return new RunConfigAndRes(id, this.workspace, this);
    }

    private RunConfigAndRes(String id, Path workspace, BaM bam) {
        super(
                bam.getCalibrationConfig(),
                bam.getPredictionConfigs(),
                bam.getRunOptions(),
                bam.getCalibrationResults(),
                bam.getPredictionResults());
        this.id = id;
        this.workspace = workspace;
    }

    public String zipRun(boolean writeToFile, String... filenamesToIgnore) {
        if (!workspace.toFile().exists()) {
            toFiles(workspace.toString());
        }
        List<String> _filenamesToIgnore = new ArrayList<>(Arrays.asList(filenamesToIgnore));
        _filenamesToIgnore.add(BamFilesHelpers.RESULTS_MCMC);
        _filenamesToIgnore.add(BamFilesHelpers.RESULTS_OUTPUT_SPAG);
        String zipName = id + ".zip";
        Path zipPath = Path.of(AppSetup.PATH_APP_TEMP_DIR, zipName);
        if (writeToFile) {
            // each run being unique, if the zip file exist, there is no need
            // to recreate it; no modification could have occured
            List<String> filesToZip = new ArrayList<>();
            for (File f : workspace.toFile().listFiles()) {
                String filename = f.getName();
                if (matchTemplates(filename, _filenamesToIgnore)) {
                    ConsoleLogger.log("BaM file '%s' was ignored.".formatted(filename));
                    continue;
                }
                filesToZip.add(f.getName());
            }
            String baseDir = workspace.toString();
            String[] filesToZipFullPath = filesToZip
                    .stream()
                    .map(f -> Path.of(baseDir, f).toString())
                    .toArray(String[]::new);
            ReadWriteZip.flatZip(zipPath.toString(), filesToZipFullPath);
        }
        return zipPath.toString();
    }

    private final static boolean matchTemplates(String filename, List<String> templates) {
        for (String t : templates) {
            if (Misc.matchesTemplate(t, filename)) {
                return true;
            }
        }
        return false;
    }
}
//