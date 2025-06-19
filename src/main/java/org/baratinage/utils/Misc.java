package org.baratinage.utils;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.baratinage.translation.T;
import org.baratinage.utils.fs.ReadFile;

public class Misc {

    public static void setMinimumSize(JComponent component, Integer width, Integer height) {
        Dimension dim = component.getMinimumSize();
        if (width != null) {
            dim.width = width;
        }
        if (height != null) {
            dim.height = height;
        }
        component.setMinimumSize(dim);
    }

    public static String formatNumber(double num) {
        return formatNumber(num, false);
    }

    public static String formatNumber(double num, boolean lossless) {
        if (num == (long) num) {
            return String.valueOf((long) num);
        }
        if (lossless) {
            return new DecimalFormat("#.##########").format(num);
        }
        double absNum = Math.abs(num);
        DecimalFormat df;
        if (absNum < 0.001 || absNum >= 10000) {
            df = new DecimalFormat("0.###E0");
        } else if (absNum < 0.1) {
            df = new DecimalFormat("0.####");
        } else if (absNum < 1) {
            df = new DecimalFormat("0.###");
        } else if (absNum < 10) {
            df = new DecimalFormat("0.##");
        } else if (absNum < 100) {
            df = new DecimalFormat("0.#");
        } else {
            df = new DecimalFormat("0");
        }
        return df.format(num);
    }

    public static String sanitizeName(String input) {
        String sanitizedName = input.replaceAll("[^a-zA-Z0-9]", "_");
        if (sanitizedName.isEmpty()) {
            sanitizedName = "_";
        }
        sanitizedName = sanitizedName.trim();
        return sanitizedName;
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
        String id = Misc.getTimeStamp() + "_" + Misc.getId();
        return id;
    }

    public static String getId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    public static String getNextName(String defaultName, String[] allNames) {
        Set<Integer> usedInts = new HashSet<>();
        for (String name : allNames) {
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
        for (int k = 1; k < 100; k++) {
            if (!usedInts.contains(k)) {
                return defaultName + " (" + (k) + ")";
            }
        }
        return defaultName + " (?)";
    }

    @Deprecated
    public static void createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            ConsoleLogger.log("Creating directory '" + dirPath + "'... ");
            dirFile.mkdirs();
        }
    }

    @Deprecated
    public static boolean deleteDirContent(String dirPath) {
        return deleteDirContent(new File(dirPath));
    }

    @Deprecated
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

    @Deprecated
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

    @Deprecated
    public static boolean deleteDir(String dirPath) {
        return deleteDir(new File(dirPath));
    }

    @Deprecated
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

    public static boolean containsMissingValue(double[] arr) {
        int n = arr.length;
        for (int k = 0; k < n; k++) {
            if (Double.isNaN(arr[k])) {
                return true;
            }
        }
        return false;
    }

    public static TreeSet<Integer> getMissingValuesIndices(double[]... arrays) {
        TreeSet<Integer> mvIndices = new TreeSet<>();
        for (double[] arr : arrays) {
            int n = arr.length;
            for (int k = 0; k < n; k++) {
                if (Double.isNaN(arr[k])) {
                    mvIndices.add(k);
                }
            }
        }
        return mvIndices;
    }

    public static List<double[]> removeMissingValues(List<double[]> data, TreeSet<Integer> mvIndices) {
        int nCol = data.size();
        if (nCol == 0) {
            ConsoleLogger.warn("Empty data array, cannot remove missing values. Returning original data.");
            return data;
        }
        int nRow = data.get(0).length;
        int nMv = mvIndices.size();
        int nRowNoMv = nRow - nMv;
        if (nRowNoMv <= 0) {
            ConsoleLogger.warn("Too many missing values. Returning an empty List.");
            return new ArrayList<>();
        }
        List<double[]> noMvData = new ArrayList<>();
        for (int i = 0; i < nCol; i++) {
            noMvData.add(new double[nRowNoMv]);
        }
        int k = 0;
        for (int j = 0; j < nRow; j++) {
            if (!mvIndices.contains(j)) {
                for (int i = 0; i < nCol; i++) {
                    noMvData.get(i)[k] = data.get(i)[j];
                }
                k++;
            }
        }
        return noMvData;
    }

    public static List<double[]> insertMissingValues(List<double[]> data, TreeSet<Integer> mvIndices) {
        int nMv = mvIndices.size();
        if (nMv == 0) {
            return data;
        }
        List<double[]> mvData = new ArrayList<>();
        int nCol = data.size();
        if (nCol == 0) {
            ConsoleLogger.warn("Empty data array, cannot insert missing values. Returning original data.");
            return data;
        }
        int nRow = data.get(0).length;
        for (int k = 0; k < nCol; k++) {
            double[] arr = new double[nRow + nMv];
            mvData.add(arr);
        }
        int indexNoMv = 0;
        for (int j = 0; j < nRow + nMv; j++) {
            if (mvIndices.contains(j)) {
                for (int i = 0; i < nCol; i++) {
                    mvData.get(i)[j] = Double.NaN;
                }
            } else {
                for (int i = 0; i < nCol; i++) {
                    mvData.get(i)[j] = data.get(i)[indexNoMv];
                }
                indexNoMv++;
            }

        }
        return mvData;
    }

    public static List<double[]> stringToDoubleMatrix(List<String> stringMatrix, String sep) {
        List<double[]> matrixRowWise = new ArrayList<>();
        for (int i = 0; i < stringMatrix.size(); i++) {
            String[] valuesStr = ReadFile.parseString(stringMatrix.get(i), sep, true);
            double[] ValuesDbl = new double[valuesStr.length];
            for (int j = 0; j < valuesStr.length; j++) {
                ValuesDbl[j] = ReadFile.toDouble(valuesStr[j]);
            }
            matrixRowWise.add(ValuesDbl);
        }
        return matrixRowWise;
    }

    public static List<double[]> transposeDoubleMatrix(List<double[]> matrix) {
        if (matrix.size() == 0) {
            ConsoleLogger.error("Cannot transpose an empty matrix");
            return matrix;
        }
        int nCol = matrix.get(0).length;
        for (int k = 0; k < matrix.size(); k++) {
            if (matrix.get(k).length != nCol) {
                throw new IllegalArgumentException(
                        "Transposition impossible because the input matrix has columns of various length");
            }
        }
        List<double[]> transposed = new ArrayList<>();
        int nRow = matrix.size();
        for (int i = 0; i < nCol; i++) {
            double[] col = new double[nRow];
            for (int j = 0; j < matrix.size(); j++) {
                col[j] = matrix.get(j)[i];
            }
            transposed.add(col);
        }
        return transposed;
    }

    public static String doubleArrToStringArg(double... values) {
        int n = values.length;
        String[] valuesString = new String[n];
        for (int k = 0; k < n; k++) {
            valuesString[k] = Double.toString(values[k]);
        }
        return String.join(",", valuesString);
    }

    public static String createIntegerStringList(List<Integer> indices, int maxValues) {
        List<Integer> list = new ArrayList<>(indices);
        Collections.sort(list);
        String result;
        if (list.size() <= maxValues) {
            result = list.stream().map(String::valueOf).collect(Collectors.joining(", "));
        } else {
            result = String.format("%s, %s, ..., %s, %s",
                    list.get(0), list.get(1),
                    list.get(list.size() - 2), list.get(list.size() - 1));
        }
        return result;
    }

    public static String formatKilobitesSize(double sizeInKb) {
        if (sizeInKb <= 0) {
            return "0 B";
        }

        int sizeInBytes = (int) sizeInKb * 1024;

        final String[] units = { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));

        return String.format("%.1f %s", sizeInBytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static double[] ones(int n) {
        double[] d = new double[n];
        for (int k = 0; k < n; k++) {
            d[k] = 1;
        }
        return d;
    }

    public static Integer[] range(int start, int end) {
        int n = end - start;
        Integer[] result = new Integer[n];
        for (int k = start; k < n; k++) {
            result[k] = k;
        }
        return result;
    }

    public static double[] reorderArray(Integer[] indices, double[] array) {
        if (array.length != indices.length) {
            ConsoleLogger.error("'array' and 'indices' must have the same length!");
            return null;
        }
        int n = array.length;
        double[] reordered = new double[n];
        for (int k = 0; k < n; k++) {
            reordered[k] = array[indices[k]];
        }
        return reordered;
    }
}
