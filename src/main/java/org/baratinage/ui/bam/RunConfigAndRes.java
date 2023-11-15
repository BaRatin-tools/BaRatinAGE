package org.baratinage.ui.bam;

import java.io.File;
import java.nio.file.Path;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.AppConfig;
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

    public static RunConfigAndRes buildFromZipArchive(String id, Path zipPath) {

        Path workspacePath = Path.of(AppConfig.AC.BAM_WORKSPACE_ROOT, id);
        String zipName = id + ".zip";
        zipPath = Path.of(AppConfig.AC.APP_TEMP_DIR, zipName);

        ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());

        return buildFromWorkspace(id, workspacePath);
    }

    public static RunConfigAndRes buildFromTempZipArchive(String id) {
        String zipName = id + ".zip";
        Path zipPath = Path.of(AppConfig.AC.APP_TEMP_DIR, zipName);
        return buildFromZipArchive(id, zipPath);
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

    public String zipRun(boolean writeToFile) {
        if (!workspace.toFile().exists()) {
            toFiles(workspace.toString());
        }
        String zipName = id + ".zip";
        Path zipPath = Path.of(AppConfig.AC.APP_TEMP_DIR, zipName);
        if (!zipPath.toFile().exists() && writeToFile) {
            // each run being unique, if the zip file exist, there is no need
            // to recreate it; no modification could have occured
            ReadWriteZip.flatZip(zipPath.toString(), workspace.toString());
        }
        return zipPath.toString();
    }
}
//