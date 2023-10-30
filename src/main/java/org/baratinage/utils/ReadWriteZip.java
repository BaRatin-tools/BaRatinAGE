package org.baratinage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ReadWriteZip {

    static public boolean unzip(String zipFilePath, String targetDirPath) {
        try {
            InputStream is = new FileInputStream(zipFilePath);
            Path targetDir = Path.of(targetDirPath);
            unzip(is, targetDir);
            return true;
        } catch (RuntimeException re) {
            System.out.println(re);
            return false;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }

    private static void unzip(InputStream is, Path targetDir) throws IOException {
        // copy/pasted, thanks to: https://stackoverflow.com/a/59581898
        targetDir = targetDir.toAbsolutePath();
        try (ZipInputStream zipIn = new ZipInputStream(is)) {
            for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null;) {
                Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
                if (!resolvedPath.startsWith(targetDir)) {
                    // see: https://snyk.io/research/zip-slip-vulnerability
                    throw new RuntimeException("Entry with an illegal path: "
                            + ze.getName());
                }
                if (ze.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zipIn, resolvedPath);
                }
            }
        }
    }

    static public boolean flatZip(String zipFilePath, String... sourceDirs) {
        File zipFile = new File(zipFilePath.toString());
        // try {
        FileOutputStream zipFileOutStream;
        try {
            zipFileOutStream = new FileOutputStream(zipFile);
        } catch (FileNotFoundException e) {
            System.err.println("ReadWriteZip Error: Cannot create output zipfile '" + zipFilePath + "'!");
            e.printStackTrace();
            return false;
        }
        ZipOutputStream zipOutStream = new ZipOutputStream(zipFileOutStream);

        boolean success = true;
        for (String source : sourceDirs) {
            success &= flatZip(zipOutStream, new File(source));
        }
        try {
            zipOutStream.close();
        } catch (IOException e) {
            System.err.println("ReadWriteZip Error: Cannot close output zipfile '" + zipFilePath + "'!");
            e.printStackTrace();
            return false;
        }
        return success;
    }

    static private boolean flatZip(ZipOutputStream zipOutStream, File fileOrDir) {
        boolean success = true;
        if (fileOrDir.isDirectory()) {
            File[] innerFiles = fileOrDir.listFiles();
            if (innerFiles != null) {
                for (File f : innerFiles) {
                    if (!flatZip(zipOutStream, f)) {
                        success = false;
                    }
                }
            }

        } else if (fileOrDir.isFile()) {
            System.out.println("ReadWriteZip: Zipping file '" + fileOrDir + "'...");
            ZipEntry ze = new ZipEntry(fileOrDir.getName());
            try {
                zipOutStream.putNextEntry(ze);
                Files.copy(fileOrDir.toPath(), zipOutStream);
            } catch (IOException e) {
                System.err.println("ReadWriteZip Error: Failed to add file '" + fileOrDir + "'!");
                success = false;
            }
        }

        return success;
    }
}
