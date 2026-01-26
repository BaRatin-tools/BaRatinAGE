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

    /**
     * Sets the minimum size of a Swing component.
     * <p>
     * If {@code width} or {@code height} are {@code null}, the existing
     * dimension is preserved for that axis.
     * </p>
     *
     * @param component the component to modify
     * @param width     the new minimum width, or {@code null} to keep current
     * @param height    the new minimum height, or {@code null} to keep current
     */
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

    /**
     * Sets the preferred size of a Swing component.
     * <p>
     * If {@code width} or {@code height} are {@code null}, the existing
     * dimension is preserved for that axis.
     * </p>
     *
     * @param component the component to modify
     * @param width     the preferred width, or {@code null} to keep current
     * @param height    the preferred height, or {@code null} to keep current
     */
    public static void setPreferredSize(JComponent component, Integer width, Integer height) {
        Dimension dim = component.getMinimumSize();
        if (width != null) {
            dim.width = width;
        }
        if (height != null) {
            dim.height = height;
        }
        component.setPreferredSize(dim);
    }

    /**
     * Format a number with up to 10 decimals, trimming trailing zeros.
     * Uses a fixed pattern: '#.##########'.
     *
     * @param num value to format
     * @return formatted string
     */
    public static String formatNumber(double num) {
        return new DecimalFormat("#.##########").format(num);
    }

    /**
     * Format a number using a specified number of significant digits.
     * Delegates to the more general formatter with default decimalPlaces and
     * scientific notation disabled.
     *
     * @param num     value to format
     * @param nSignif number of significant digits
     * @return formatted string
     */
    public static String formatNumber(double num, int nSignif) {
        return formatNumber(num, nSignif, false, false);
    }

    private static int decimalPlacesForSignificant(double num, int precision) {
        if (num == 0)
            return precision - 1;
        int magnitude = (int) Math.floor(Math.log10(Math.abs(num)));
        return Math.max(0, precision - magnitude - 1);
    }

    /**
     * General number formatter.
     * <ul>
     * <li>If {@code decimalPlaces} is false, formats with a variable number of
     * significant digits unless {@code scientific} is true.</li>
     * <li>If {@code decimalPlaces} is true, formats with a fixed number of
     * decimals or scientific notation depending on {@code scientific}.</li>
     * </ul>
     * </p>
     *
     * @param num           the value to format
     * @param precision     number of significant digits or decimals depending on
     *                      mode
     * @param decimalPlaces if true use a fixed number of decimals, else use
     *                      significant digits
     * @param scientific    whether to allow scientific notation
     * @return formatted string
     */
    public static String formatNumber(
            double num,
            int precision,
            boolean decimalPlaces,
            boolean scientific) {
        if (Double.isNaN(num) || Double.isInfinite(num)) {
            return String.valueOf(num);
        }

        DecimalFormat df;

        if (!decimalPlaces) {
            if (scientific) {
                String pattern = "0." + "#".repeat(Math.max(0, precision - 1)) + "E0";
                df = new DecimalFormat(pattern);
            } else {
                int decimals = decimalPlacesForSignificant(num, precision);
                String pattern = (decimals > 0)
                        ? "0." + "#".repeat(decimals)
                        : "0";
                df = new DecimalFormat(pattern);
                return df.format(num);

            }
        } else { // DECIMAL_PLACES
            String pattern = scientific
                    ? "0." + "0".repeat(precision) + "E0"
                    : "0." + "0".repeat(precision);
            df = new DecimalFormat(pattern);
        }

        return df.format(num);
    }

    /**
     * Format a number with a fixed precision and scientific threshold.
     * The number is formatted using {@code precision} with optional
     * {@code decimalPlaces} and scientific notation controlled by the
     * provided {@code sciLow} and {@code sciHigh} thresholds.
     *
     * @param num           value to format
     * @param precision     decimal places or significant digits depending on mode
     * @param decimalPlaces if true use fixed decimals
     * @param sciLow        lower bound for using scientific notation
     * @param sciHigh       upper bound for using scientific notation
     * @return formatted string
     */
    public static String formatNumber(
            double num,
            int precision,
            boolean decimalPlaces,
            double sciLow,
            double sciHigh) {
        if (num == 0) {
            return decimalPlaces
                    ? String.format("%." + precision + "f", 0.0)
                    : "0";
        }
        double abs = Math.abs(num);
        boolean scientific = abs <= sciLow || abs >= sciHigh;
        return formatNumber(num, precision, decimalPlaces, scientific);
    }

    /**
     * Sanitize a name by replacing non-alphanumeric characters with '_'.
     * If the result is empty, returns "_". Trims surrounding whitespace.
     *
     * @param input raw name
     * @return sanitized name
     */
    public static String sanitizeName(String input) {
        String sanitizedName = input.replaceAll("[^a-zA-Z0-9]", "_");
        if (sanitizedName.isEmpty()) {
            sanitizedName = "_";
        }
        sanitizedName = sanitizedName.trim();
        return sanitizedName;
    }

    /**
     * Get a timestamp formatted with the given pattern.
     *
     * @param format SimpleDateFormat-compatible pattern
     * @return formatted timestamp
     */
    public static String getTimeStamp(String format) {
        return new SimpleDateFormat(format).format(new java.util.Date());
    }

    /**
     * Get a timestamp using the default pattern: yyyyMMdd_HHmmss.
     *
     * @return formatted timestamp
     */
    public static String getTimeStamp() {
        return getTimeStamp("yyyyMMdd_HHmmss");
    }

    /**
     * Get the local timestamp formatted in the current locale.
     * Uses MEDIUM style to include date and time components.
     *
     * @return localized timestamp string
     */
    public static String getLocalTimeStamp() {
        Locale l = T.getLocale();
        LocalDateTime date = LocalDateTime.now();
        // SHORT format style omit seconds... which I need here.
        String text = date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(l));
        return text;
    }

    /**
     * Generate an identifier composed of a timestamp and a short random id.
     *
     * @return combined timestamp+id string
     */
    public static String getTimeStampedId() {
        String id = Misc.getTimeStamp() + "_" + Misc.getId();
        return id;
    }

    /**
     * Generate a short 5-character pseudo-unique identifier.
     *
     * @return 5-character id
     */
    public static String getId() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    /**
     * Generate the next available name based on a default name and existing names.
     * It selects the smallest positive integer that makes the name unique in
     * the form "defaultName (n)".
     *
     * @param defaultName base name
     * @param allNames    existing names to avoid
     * @return unique next name
     */
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

    /**
     * Create a directory if it does not exist. Deprecated: use a higher-level
     * file management approach.
     *
     * @param dirPath directory path
     */
    @Deprecated
    public static void createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            ConsoleLogger.log("Creating directory '" + dirPath + "'... ");
            dirFile.mkdirs();
        }
    }

    /**
     * Delete all contents of a directory. Deprecated.
     *
     * @param dirPath directory path
     * @return success flag
     */
    @Deprecated
    public static boolean deleteDirContent(String dirPath) {
        return deleteDirContent(new File(dirPath));
    }

    /**
     * Delete all contents of a directory (File variant). Deprecated.
     *
     * @param dirPath directory as File
     * @return success flag
     */
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

    /**
     * Recursively delete a directory and all its contents. Deprecated.
     *
     * @param dirPath directory to delete
     * @return success flag
     */
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

    /**
     * Delete a directory by path. Deprecated.
     *
     * @param dirPath directory path
     * @return success flag
     */
    @Deprecated
    public static boolean deleteDir(String dirPath) {
        return deleteDir(new File(dirPath));
    }

    /**
     * Parse a Path from an unknown or mixed OS-origin path. Deprecated.
     *
     * @param rawPath input path string
     * @return Path object
     */
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

    /**
     * Position a frame on a specific screen and make it visible.
     *
     * @param screen screen index
     * @param frame  the frame to show
     */
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

    /**
     * Guess an index in {@code strings} that matches all provided regex
     * {@code patterns}.
     * If none match, returns {@code defaultIndex}.
     *
     * @param strings      array of strings to test
     * @param defaultIndex fallback index
     * @param patterns     one or more regex patterns that the selected string must
     *                     satisfy
     * @return index of first string matching all patterns or defaultIndex
     */
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

    /**
     * Check whether the provided array contains any NaN (missing value).
     *
     * @param arr array to inspect
     * @return true if any element is NaN
     */
    public static boolean containsMissingValue(double[] arr) {
        int n = arr.length;
        for (int k = 0; k < n; k++) {
            if (Double.isNaN(arr[k])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the set of indices that contain missing values (NaN) across multiple
     * arrays.
     *
     * @param arrays one or more double[] arrays
     * @return sorted set of indices containing NaN in any input array
     */
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

    /**
     * Remove values at the given missing-value indices from each column of data.
     *
     * @param data      list of columns (double[])
     * @param mvIndices indices to remove
     * @return new list with specified indices removed
     */
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

    /**
     * Insert NaN values at the given indices into each column, expanding the rows.
     *
     * @param data      list of columns (double[])
     * @param mvIndices indices where missing values should be inserted
     * @return new data with inserted missing values
     */
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

    /**
     * Convert a list of strings into a matrix of doubles using the given separator.
     * Each string is parsed as a row of numbers.
     *
     * @param stringMatrix list of rows as strings
     * @param sep          separator used to split values
     * @return matrix as a list of double[] rows
     */
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

    /**
     * Transpose a matrix represented as a list of double[] rows.
     * Throws an IllegalArgumentException if the input rows have inconsistent
     * lengths.
     *
     * @param matrix input matrix as list of rows
     * @return transposed matrix as a list of columns
     */
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

    /**
     * Convert a vararg array of doubles to a comma-separated string.
     *
     * @param values numbers to stringify
     * @return comma-separated representation
     */
    public static String doubleArrToStringArg(double... values) {
        int n = values.length;
        String[] valuesString = new String[n];
        for (int k = 0; k < n; k++) {
            valuesString[k] = Double.toString(values[k]);
        }
        return String.join(",", valuesString);
    }

    /**
     * Create a human-friendly string listing integers from a list, with optional
     * truncation using ellipsis when the list is long.
     *
     * @param indices   indices to display
     * @param maxValues maximum values to show before truncating
     * @return formatted string
     */
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

    /**
     * Format a size in kilobytes to a human-readable string with appropriate units.
     *
     * @param sizeInKb size in kilobytes
     * @return human-readable size (e.g., "1.2 MB")
     */
    public static String formatKilobitesSize(double sizeInKb) {
        if (sizeInKb <= 0) {
            return "0 B";
        }

        int sizeInBytes = (int) sizeInKb * 1024;

        final String[] units = { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
        int digitGroups = (int) (Math.log10(sizeInBytes) / Math.log10(1024));

        return String.format("%.1f %s", sizeInBytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Create an array of length {@code n} filled with 1.0.
     *
     * @param n length of the array
     * @return array filled with ones
     */
    public static double[] ones(int n) {
        double[] d = new double[n];
        for (int k = 0; k < n; k++) {
            d[k] = 1;
        }
        return d;
    }

    /**
     * Build a range of integers from {@code start} (inclusive) to {@code end}
     * (exclusive).
     *
     * @param start start value
     * @param end   end value (exclusive)
     * @return array containing the range
     */
    public static Integer[] range(int start, int end) {
        // Return a range of integers from start (inclusive) to end (exclusive).
        // If end <= start, return an empty array.
        if (end <= start) {
            return new Integer[0];
        }
        int n = end - start;
        Integer[] result = new Integer[n];
        for (int i = 0; i < n; i++) {
            result[i] = start + i;
        }
        return result;
    }

    /**
     * Reorder an array according to a given index mapping.
     * Returns null if the arrays have different lengths.
     *
     * @param indices index order to apply
     * @param array   source array
     * @return reordered array or null on mismatch
     */
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

    /**
     * Check whether a string matches a template where "%s" is treated as a
     * wildcard.
     * The template is converted into a regular expression by replacing "%s" with
     * a capturing group that matches one or more characters.
     *
     * @param template template containing "%s" as a placeholder
     * @param str      string to test against the template
     * @return true if the string matches the template
     */
    public static boolean matchesTemplate(String template, String str) {
        String regex = "^%s$".formatted(template.replace("%s", "(.+)"));
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }
}
