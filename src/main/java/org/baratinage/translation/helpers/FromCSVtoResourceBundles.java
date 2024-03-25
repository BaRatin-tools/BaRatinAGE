package org.baratinage.translation.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FromCSVtoResourceBundles {

    public static void main(String[] args) {
        String mainDir = Path.of("resources", "i18n").toString();
        String translationsSource = Path.of(mainDir, "translations.csv").toString();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(translationsSource), "UTF-8"))) {
            // Read the header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("Empty file!");
                return;
            }

            // Split the header line into column names
            String[] headers = headerLine.split("\t");

            // Create a map to store FileWriter objects for each column
            Map<String, BufferedWriter> columnWriters = new HashMap<>();

            // Create a FileWriter for each column
            for (int i = 2; i < headers.length; i++) {
                String header = headers[i];
                String targetFilePath = Path.of(mainDir, "translations_" + header + ".properties").toString();
                FileOutputStream fos = new FileOutputStream(targetFilePath);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                BufferedWriter writer = new BufferedWriter(osw);
                columnWriters.put(header, writer);

            }

            // Read and process the remaining lines
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\t", -1);
                if (columns.length != headers.length) {
                    System.out.println(line);
                    continue;
                }
                String key = columns[0];
                for (int i = 2; i < headers.length; i++) {
                    BufferedWriter writer = columnWriters.get(headers[i]);
                    if (writer != null && i < columns.length) {
                        writer.write(key + " = " + columns[i] + "\n");
                    }
                }
            }

            // Close all writers
            for (BufferedWriter writer : columnWriters.values()) {
                writer.close();
            }

            System.out.println("File splitting completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
