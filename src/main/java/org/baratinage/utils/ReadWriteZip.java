package org.baratinage.utils;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ReadWriteZip {
    // static File InputStreamToFile(InputStream stream) {

    // }

    static public boolean unzip(String zipFilePath, String targetDirPath) {
        // File tarderDir = new File(targetDirPath);
        // tarderDir.mkdirs();
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String targetFilePath = Path.of(targetDirPath, zipEntry.getName()).toString();
                BufferedOutputStream bufOutStream = new BufferedOutputStream(new FileOutputStream(targetFilePath));
                byte[] bytesIn = new byte[1024];
                int read = 0;
                while ((read = zipInputStream.read(bytesIn)) != -1) {
                    bufOutStream.write(bytesIn, 0, read);
                }
                bufOutStream.close();
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            System.err.println("Error while reading bam (zip) file... Aborting.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
