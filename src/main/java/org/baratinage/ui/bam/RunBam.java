package org.baratinage.ui.bam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.baratinage.App;
import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalibrationResult;
import org.baratinage.jbam.PredictionResult;

public abstract class RunBam {

    // FIXME: should be stored at the much higher level (Project or App level)
    protected final String bamRunZipFileName = UUID.randomUUID().toString() + ".zip";
    protected final Path runZipFile = Path.of(App.TEMP_DIR, bamRunZipFileName);
    protected BaM bam;
    protected Path workspace;
    protected boolean isConfigured = false;
    protected boolean hasResults = false;

    public void run() {
        if (!isConfigured) {
            System.err.println("Cannot run BaM if configure() method has not been called first!");
            return;
        }
        try {
            bam.run(workspace.toString(), txt -> {
                System.out.println("log => " + txt);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        bam.readResults(workspace.toString());
        hasResults = true;

        try {
            backupBamRun();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void backupBamRun() throws IOException {
        File zipFile = new File(runZipFile.toString());
        FileOutputStream zipFileOutStream = new FileOutputStream(zipFile);
        ZipOutputStream zipOutStream = new ZipOutputStream(zipFileOutStream);

        File[] files = workspace.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                System.out.println("File '" + f + "'.");
                ZipEntry ze = new ZipEntry(f.getName());
                zipOutStream.putNextEntry(ze);
                Files.copy(f.toPath(), zipOutStream);
            }
        }
        zipOutStream.close();
    }

    public String getBamRunZipFileName() {
        return this.bamRunZipFileName;
    }

    public BaM getBaM() {
        return bam;
    }

    public CalibrationResult getCalibrationResult() {
        if (!hasResults)
            return null;
        return bam.getCalibrationResults();
    }

    public PredictionResult[] getPredictionResults() {
        if (!hasResults)
            return null;
        return bam.getPredictionResults();
    }
}