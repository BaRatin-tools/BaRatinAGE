package org.baratinage.utils;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// FIXME: should zip/unzip handle recursion?
public class ReadWriteZip {

    static public boolean unzip(String zipFilePath, String targetDirPath) {
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

    static public boolean zip(String zipFilePath, String sourceDirPath) {
        File zipFile = new File(zipFilePath.toString());
        try {
            FileOutputStream zipFileOutStream = new FileOutputStream(zipFile);
            ZipOutputStream zipOutStream = new ZipOutputStream(zipFileOutStream);
            File[] files = new File(sourceDirPath).listFiles();
            if (files != null) {
                for (File f : files) {
                    System.out.println("File '" + f + "'.");
                    ZipEntry ze = new ZipEntry(f.getName());

                    zipOutStream.putNextEntry(ze);

                    Files.copy(f.toPath(), zipOutStream);
                }
            }
            zipOutStream.close();
        } catch (IOException e) {
            System.err.println("Error while zipping directory!");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
