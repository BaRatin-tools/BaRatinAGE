package org.baratinage.utils;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.baratinage.translation.T;

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
        Locale l = T.getLocale();
        LocalDateTime date = LocalDateTime.now();
        // SHORT format style omit seconds... which I need here.
        String text = date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(l));
        return text;
    }

    public static String getTimeStampedId() {
        String id = Misc.getTimeStamp() + "_" + UUID.randomUUID().toString().substring(0, 5);
        return id;
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
                        ConsoleLogger.log("Cannot parse into double");
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
            boolean success = dirFile.mkdirs();
            ConsoleLogger.log("Creating directory '" + dirPath + "'... " + (success ? "SUCCESS" : "FAIL"));
        }
    }

    public static void deleteDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            ConsoleLogger.error("Cannot delete directory '" + dirPath + "' because it doesn't exist! ");
            return;
        }
        for (File f : dirFile.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f.toString());
            } else {
                boolean success = true;
                try {
                    Files.delete(f.toPath());
                } catch (IOException e) {
                    ConsoleLogger.stackTrace(e);
                    success = false;
                }
                ConsoleLogger.log("Deleting file '" + f + "'... " + (success ? "SUCCESS" : "FAIL"));

            }
        }
        boolean success = dirFile.delete();
        ConsoleLogger.log("Deleting directory '" + dirPath + "'... " + (success ? "SUCCESS" : "FAILED"));
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

    public static void showOnScreen(int screen, JFrame frame) {
        // source: https://stackoverflow.com/a/39801137
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        int width = 0, height = 0;
        if (screen > -1 && screen < gd.length) {
            width = gd[screen].getDefaultConfiguration().getBounds().width;
            height = gd[screen].getDefaultConfiguration().getBounds().height;
            frame.setLocation(
                    ((width / 2) - (frame.getSize().width / 2)) + gd[screen].getDefaultConfiguration().getBounds().x,
                    ((height / 2) - (frame.getSize().height / 2)) + gd[screen].getDefaultConfiguration().getBounds().y);
            frame.setVisible(true);
        }
    }

    public static int getIndexGuess(String[] strings, int defaultIndex, String... patterns) {
        Predicate<String> allPredicates = (String str) -> true;
        for (String pattern : patterns) {
            allPredicates = allPredicates.and(Pattern.compile(pattern).asMatchPredicate());
        }
        for (int k = 0; k < strings.length; k++) {
            if (allPredicates.test(strings[k])) {
                return k;
            }
        }
        return defaultIndex;
    }

    public static double[] makeGrid(double low, double high, int n) {
        double step = (high - low) / ((double) n - 1);
        double[] grid = new double[n];
        for (int k = 0; k < n; k++) {
            grid[k] = low + step * k;
        }
        return grid;
    }

    public static double[] makeGrid(double low, double high, double step) {
        int n = (int) ((high - low) / step + 1);
        double[] grid = new double[n];
        for (int k = 0; k < n; k++) {
            grid[k] = low + step * k;
        }
        return grid;
    }
}
