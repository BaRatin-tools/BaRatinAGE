package org.baratinage.utils.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.baratinage.utils.ConsoleLogger;

public class DirUtils {

    public static List<File> filterFiles(List<File> files, boolean includeExtensions, String... extensions) {
        return filterFiles(files.toArray(new File[files.size()]), includeExtensions, extensions);
    }

    public static List<File> filterFiles(File[] files, boolean includeExtensions, String... extensions) {
        List<File> filesInclude = new ArrayList<>();
        List<File> filesExclude = new ArrayList<>();
        for (File file : files) {
            for (String ext : extensions) {
                if (file.getName().endsWith("." + ext)) {
                    filesInclude.add(file);
                } else {
                    filesExclude.add(file);
                }
            }
        }
        return includeExtensions ? filesInclude : filesExclude;
    }

    public static List<File> filterFilesByAge(List<File> files, TimeUnit timeUnit, long lastModified) {
        return filterFilesByAge(files, lastModified);
    }

    public static List<File> filterFilesByAge(File[] files, TimeUnit timeUnit, long lastModified) {
        return filterFilesByAge(files, lastModified);
    }

    public static List<File> filterFilesByAge(List<File> files, long lastModifiedMiliseconds) {
        return filterFilesByAge(files.toArray(new File[files.size()]), lastModifiedMiliseconds);
    }

    public static List<File> filterFilesByAge(File[] files, long lastModifiedMiliseconds) {
        List<File> oldFiles = new ArrayList<>();
        for (File file : files) {
            try {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                long elapsed = System.currentTimeMillis() - attr.lastModifiedTime().toMillis();
                if (elapsed > lastModifiedMiliseconds) {
                    oldFiles.add(file);
                }
            } catch (IOException e) {
                ConsoleLogger.error(e);
            }
        }
        return oldFiles;
    }

    public static boolean removeFiles(List<File> files) {
        return removeFiles(files.toArray(new File[files.size()]));
    }

    public static boolean removeFiles(File[] files) {
        boolean success = true;
        for (File file : files) {
            if (file.exists()) {
                if (!file.delete()) {
                    success = false;
                }
            }
        }
        return success;
    }

    public static File createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            ConsoleLogger.log("Creating directory '" + dirPath + "'... ");
            dirFile.mkdirs();
        }
        return dirFile;
    }

    public static boolean deleteDirContent(String dirPath) {
        return deleteDirContent(new File(dirPath));
    }

    public static boolean deleteDirContent(File dirPath) {
        File[] allContents = dirPath.listFiles();
        boolean success = true;
        if (allContents != null) {
            for (File file : allContents) {
                success = success && deleteDir(file);
            }
        }
        return success;
    }

    public static boolean deleteDir(File dirPath) {
        File[] allContents = dirPath.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDir(file);
            }
        }
        boolean success = dirPath.delete();
        if (!success) {
            ConsoleLogger.error("Failed to delete '" + dirPath + "'!");
        }
        return success;
    }

    public static boolean deleteDir(String dirPath) {
        return deleteDir(new File(dirPath));
    }

    public static Path parsePathFromUnknownOSorigin(String rawPath) {
        String[] osSplitChars = new String[] { "\\\\", "/" };
        int maxNumberOfItems = -1;
        String root = rawPath;
        String[] bestSplit = new String[] {};
        for (String splitChar : osSplitChars) {
            String[] splitRes = rawPath.split(splitChar);
            int n = splitRes.length;
            if (n > 1) {
                if (n > maxNumberOfItems) {
                    maxNumberOfItems = n;
                    root = "";
                    bestSplit = splitRes;
                }
            }
        }
        return Path.of(root, bestSplit);
    }

}
