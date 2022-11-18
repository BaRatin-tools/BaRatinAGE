package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import org.mozilla.universalchardet.UniversalDetector;
import java.util.Scanner;

public class FileReadWrite {

    /**
     * Create a Scanner object from a text file path.
     * 
     * Note that this function does not throw any errors. If any errors occurs while
     * trying to read the files a scanner containing an empty string is returned.
     * However the stack trace will still be printed to the console. This behavior
     * might not be desirable.
     * 
     * @param textFilePath full file path
     * @return Scanner object, may contain an empty string
     *         in case the scanner enconters an error while reading the file
     */
    static private Scanner createScanner(String textFilePath, boolean detectCharset) {

        File file = new File(textFilePath);

        String encoding = StandardCharsets.UTF_8.toString();
        if (detectCharset) {
            try {
                encoding = UniversalDetector.detectCharset(file);
                System.out.println(String.format("Encoding is '%s'", encoding));
            } catch (IOException e) {
                System.err.println("Exception caught: ");
                e.printStackTrace();
                encoding = StandardCharsets.UTF_8.toString();
            }
        }

        Scanner scanner;
        try {
            scanner = new Scanner(file, encoding);
        } catch (IOException e) {
            System.err.println("Exception caught: ");
            e.printStackTrace();
            return new Scanner("");
        }

        return scanner;
    }

    /**
     * Create a string array containing the lines of a text file
     * 
     * @param textFilePath full file path
     * @return an array of string corresponding to the lines of a text file
     */
    static public String[] readLines(String textFilePath, boolean detectCharset) {
        Scanner scanner = FileReadWrite.createScanner(textFilePath, detectCharset);
        ArrayList<String> lines = new ArrayList<String>();
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        scanner.close();
        String[] stringLines = lines.toArray(new String[0]);
        return stringLines;
    }

    /**
     * Parse a string into an array of string given a separator and whether
     * the input string should be trimmed
     * 
     * @param str  string to parse
     * @param sep  separator (can be a regex e.g. "\s+" for unknown number of
     *             spaces)
     * @param trim should be trimmed (i.e. remove heading/trailing spaces)?
     * @return an array string of items found within the string
     */
    static private String[] parseString(String str, String sep, boolean trim) {
        if (trim) {
            return str.trim().split(sep);
        } else {
            return str.split(sep);
        }
    }

    /**
     * Read a file containing float numbers (Double) in a matrix format
     * 
     * @param textFilePath  file path
     * @param sep           separator (can be a regex e.g. "\s+" for unknown number
     *                      of
     * @param nHeah         number of header row to skip
     * @param skipCol       number of columns to skip
     * @param trim          whether rows should be trimmed to remove
     *                      heading/trailing spaces
     * @param detectCharset whether the file encoding should be detected (default is
     *                      using UTF_8)
     * @return A Double matrix of size [Rows][Columns]
     * @throws Exception Custom exceptions occuring when trying to parse the rows
     *                   (e.g. if the number of columns found is not consistent)
     */
    static public Double[][] readMatrix(String textFilePath, String sep, int nHeah, int skipCol, boolean trim,
            boolean detectCharset)
            throws Exception {
        String[] rows = FileReadWrite.readLines(textFilePath, detectCharset);
        int nRow = rows.length;
        if (nRow <= nHeah) {
            return new Double[0][0];
        }
        int nCol = FileReadWrite.parseString(rows[0], sep, trim).length;
        Double[][] itemsPerRow = new Double[nRow - nHeah][nCol];
        for (int rowIndex = nHeah; rowIndex < nRow; rowIndex++) {
            String[] currentRow = FileReadWrite.parseString(rows[rowIndex], sep, trim);
            if (currentRow.length != nCol) {
                throw new Exception(String.format(
                        "Error while parsing file %s.\n"
                                + "The number of column in line %o doesn't match "
                                + "the expect number of column:\n"
                                + "%o columns were expected, %o columns were found",
                        textFilePath, rowIndex, nCol, currentRow.length));
            }
            try {
                for (int colIndex = 0; colIndex < nCol; colIndex++) {
                    itemsPerRow[rowIndex - nHeah][colIndex] = Double.parseDouble(currentRow[colIndex]);
                }
            } catch (Exception e) {
                throw new Exception(String.format(
                        "Error while parsing file %s.\n"
                                + "Cannot convert to Double type on line %o.",
                        textFilePath, rowIndex));
            }
        }
        return itemsPerRow;
    }

    /**
     * Read a file containing float numbers (Double) in a matrix format
     * 
     * @param textFilePath file path
     * @param sep          separator (can be a regex e.g. "\s+" for unknown number
     *                     of
     * @param nHeah        number of header row to skip
     * @return A Double matrix of size [Rows][Columns]
     * @throws Exception Custom exceptions occuring when trying to parse the rows
     *                   (e.g. if the number of columns found is not consistent)
     */
    static public Double[][] readMatrix(String textFilePath, String sep, int nHeah) throws Exception {
        return FileReadWrite.readMatrix(textFilePath, sep, nHeah, nHeah, true, false);
    }

    /**
     * Given lines of texts in an array and file path, write the lines of text to
     * the file.
     * If the file doesn't exist, it is created.
     * 
     * @param textFilePath text file path as a Path object
     * @param lines        array of strings containing the lines of text to write
     * @throws IOException Errors may occur when creating the file or when trying to
     *                     write
     *                     to the files
     */
    static public void writeLines(Path textFilePath, String[] lines) throws IOException {
        File file = textFilePath.toFile();
        if (!file.exists()) {
            System.out.println("File does not exists");
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (int k = 0; k < lines.length; k++) {
            bufferedWriter.write(lines[k]);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    /**
     * Given lines of texts in an array and file path, write the lines of text to
     * the file.
     * If the file doesn't exist, it is created.
     * 
     * @param textFilePath text file path
     * @param lines        array of strings containing the lines of text to write
     * @throws IOException Errors may occur when creating the file or when trying to
     *                     write
     *                     to the files
     */
    static public void writeLines(String textFilePath, String[] lines) throws IOException {

        File file = new File(textFilePath);
        if (!file.exists()) {
            System.out.println("Exists");
            // file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(textFilePath);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (int k = 0; k < lines.length; k++) {
            bufferedWriter.write(lines[k]);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    // for debugging purposes
    static public void printStringArray(String[] arr, String sep) {
        for (int k = 0; k < arr.length; k++) {
            System.out.print(String.format("%s%s", arr[k], sep));
        }
    } // for debugging purposes

    static public void printStringArray(String[] arr) {
        printStringArray(arr, ", ");
    }

    // for debugging purposes
    static public void printMatrix(Double[][] arr) {
        for (int k = 0; k < arr.length; k++) {
            for (int i = 0; i < arr[k].length; i++) {
                System.out.print(String.format("%f\t", arr[k][i]));
            }
            System.out.print("\n");
            if (k > 10) {
                return;
            }
        }
    }

}
