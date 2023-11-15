package org.baratinage.utils.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
                e.printStackTrace();
            }
        }
        return oldFiles;
    }

}
