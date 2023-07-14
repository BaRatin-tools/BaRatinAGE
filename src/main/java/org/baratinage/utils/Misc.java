package org.baratinage.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.baratinage.ui.lg.Lg;

public class Misc {

    // source: https://stackoverflow.com/a/24692712
    public static String sanitizeName(String name) {
        if (null == name) {
            return "";
        }

        if (File.separatorChar == '/') {
            name = name.replaceAll("[\u0000/]+", "").trim();
        } else {
            name = name.replaceAll("[\u0000-\u001f<>:\"/\\\\|?*\u007f]+", "").trim();
        }
        return name;
    }

    public static String getTimeStamp(String format) {
        return new SimpleDateFormat(format).format(new java.util.Date());
    }

    public static String getTimeStamp() {
        return getTimeStamp("yyyyMMdd_HHmmss");
    }

    public static String getLocalTimeStamp() {
        Locale l = Lg.getLocale();
        LocalDateTime date = LocalDateTime.now();
        // SHORT format style omit seconds... which I need here.
        String text = date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(l));
        return text;
    }

    public static String getTimeStampedId() {
        return Misc.getTimeStamp() + "_" + UUID.randomUUID().toString().substring(0, 5);
    }

    public static String getNextName(String defaultName, String[] allNames) {
        Set<Integer> usedInts = new HashSet<>();
        boolean containsDefault = false;
        for (String name : allNames) {
            if (name.equals(defaultName)) {
                containsDefault = true;
                continue;
            }
            String regex = defaultName + " \\((\\d+)\\)$";
            Matcher m = Pattern.compile(regex).matcher(name);
            while (m.find()) {
                if (m.groupCount() > 0) {
                    String nbr = m.group(1);
                    try {
                        int i = Integer.parseInt(nbr);
                        usedInts.add(i);
                    } catch (NumberFormatException e) {
                        System.out.println("Cannot parse into double");
                        continue;
                    }
                }
            }
        }
        if (!containsDefault) {
            return defaultName;
        }
        for (int k = 1; k < 100; k++) {
            if (!usedInts.contains(k)) {
                return defaultName + " (" + (k) + ")";
            }
        }
        return defaultName + " (?)";
    }

    public static void createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            System.out.println("Creating directory '" + dirPath + "'...");
            dirFile.mkdirs();
        }
    }

    public static void deleteDir(String dirPath) {
        File dirFile = new File(dirPath);
        for (File f : dirFile.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f.toString());
            } else {
                boolean success = f.delete();
                System.out.println("Deleting file '" + f + "'... " + (success ? "SUCCESS" : "FAILED"));

            }
        }
        boolean success = dirFile.delete();
        System.out.println("Deleting directory '" + dirPath + "'... " + (success ? "SUCCESS" : "FAILED"));
    }

}
