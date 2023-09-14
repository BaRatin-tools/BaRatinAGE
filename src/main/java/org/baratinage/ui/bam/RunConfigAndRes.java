package org.baratinage.ui.bam;

import java.io.File;
import java.nio.file.Path;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.AppConfig;
import org.baratinage.utils.ReadWriteZip;

public class RunConfigAndRes extends BaM {

    public final String id;

    public static RunConfigAndRes buildFromWorkspace(String id, Path workspacePath) {

        File mainConfigFile = Path.of(workspacePath.toString(), BamFilesHelpers.CONFIG_BAM).toFile();
        mainConfigFile.renameTo(Path.of(BamFilesHelpers.EXE_DIR, BamFilesHelpers.CONFIG_BAM).toFile());

        BaM bam = BaM.readBaM(mainConfigFile.getAbsolutePath(), workspacePath.toString());

        return new RunConfigAndRes(id, bam);
    }

    public static RunConfigAndRes buildFromZipArchive(String id, Path zipPath) {

        Path workspacePath = Path.of(AppConfig.AC.BAM_WORKSPACE_ROOT, id);
        String zipName = id + ".zip";
        zipPath = Path.of(AppConfig.AC.APP_TEMP_DIR, zipName);

        ReadWriteZip.unzip(zipPath.toString(), workspacePath.toString());

        return buildFromWorkspace(id, workspacePath);
    }

    private RunConfigAndRes(String id, BaM bam) {
        super(
                bam.getCalibrationConfig(),
                bam.getPredictionConfigs(),
                bam.getRunOptions(),
                bam.getCalibrationResults(),
                bam.getPredictionResults());
        this.id = id;
    }
}
