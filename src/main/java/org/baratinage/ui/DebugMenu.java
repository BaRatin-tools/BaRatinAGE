package org.baratinage.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.fs.WriteFile;

public class DebugMenu extends JMenu {

    public DebugMenu() {
        super("Debug");

        JMenuItem clearConsoleBtn = new JMenuItem("Clear console");
        add(clearConsoleBtn);
        clearConsoleBtn.addActionListener((e) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        });

        JMenuItem gcBtn = new JMenuItem("Garbage collection");
        add(gcBtn);
        gcBtn.addActionListener((e) -> {
            System.gc();
        });

        JMenuItem tPrintStateBtn = new JMenuItem("Print Translatables stats");
        add(tPrintStateBtn);
        tPrintStateBtn.addActionListener((e) -> {
            T.printStats(false);
        });

        JMenuItem tPrintStateAllBtn = new JMenuItem("Print Translatables stats details");
        add(tPrintStateAllBtn);
        tPrintStateAllBtn.addActionListener((e) -> {
            T.printStats(true);
        });

        JMenuItem lgResetBtn = new JMenuItem("Reload T resources");
        add(lgResetBtn);
        lgResetBtn.addActionListener((e) -> {
            T.reloadResources();
        });

        JMenuItem modifyAllIconsBtn = new JMenuItem("Update all icons");
        add(modifyAllIconsBtn);
        modifyAllIconsBtn.addActionListener((e) -> {
            AppSetup.ICONS.updateAllIcons();
        });

        JMenuItem updateCompTreeBtn = new JMenuItem("Update component tree UI");
        add(updateCompTreeBtn);
        updateCompTreeBtn.addActionListener((e) -> {
            SwingUtilities.updateComponentTreeUI(AppSetup.MAIN_FRAME);
        });

        JMenuItem printWindowsRegistry = new JMenuItem("Print BaRatinAGE location in windows registry");
        add(printWindowsRegistry);
        printWindowsRegistry.addActionListener((e) -> {
            String regPath = getWindowsRegistryBaRatinAGEPath();
            ConsoleLogger.log(String.format("Path to BaRatinAGE in Windows registry is '%s'", regPath));
            ConsoleLogger.log(String.format("Is it equal to current executable path?  %b",
                    AppSetup.PATH_APP_ROOT_DIR.equals(regPath)));

        });

        JMenuItem updateWindowsRegistry = new JMenuItem("Update BaRatinAGE location in windows registry");
        add(updateWindowsRegistry);
        updateWindowsRegistry.addActionListener((e) -> {
            updateWindowsRegistryForBaRatinAGE();
        });

    }

    private static String getWindowsRegistryBaRatinAGEPath() {
        if (!AppSetup.IS_WINDOWS) {
            ConsoleLogger.error("Cannot query Windows registry because it is not a Windows platform");
            return null;
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "reg",
                    "query",
                    String.format("HKEY_CLASSES_ROOT\\Applications\\%s.exe\\shell\\open\\command", AppSetup.APP_NAME),
                    "/s");
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                ConsoleLogger.log(line);
                if (line.contains("REG_SZ")) {
                    boolean nextItemIsPath = false;
                    String[] split = line.split("\\s+");
                    for (String l : split) {
                        if (nextItemIsPath) {
                            l = l.replace("\"", "");
                            String currentRegistryPath = Path.of(l).getParent().toString();
                            return currentRegistryPath;
                        }
                        if (l.contains("REG_SZ")) {
                            nextItemIsPath = true;
                        }
                    }
                    break;
                }
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateWindowsRegistryForBaRatinAGE() {
        if (!AppSetup.IS_WINDOWS) {
            ConsoleLogger.error("Cannot update Windows registry because it is not a Windows platform");
            return;
        }
        if (!AppSetup.IS_PACKAGED) {
            ConsoleLogger.error(
                    "Cannot updating Windows registry because BaRatinAGE is not packaged in an executable file");
            return;
        }

        String scriptPath = Path.of(AppSetup.PATH_APP_TEMP_DIR, "update_reg.ps1").toString();
        String newBaRatinAGEPath = Path.of(AppSetup.PATH_APP_ROOT_DIR, String.format("%s.exe", AppSetup.APP_NAME))
                .toString();

        ConsoleLogger.log("Starting Windows registry update...");

        ConsoleLogger.log("New BaRatinAGE path is: ");
        ConsoleLogger.log(newBaRatinAGEPath);

        try {

            String regPath = "HKEY_CLASSES_ROOT\\Applications\\BaRatinAGE.exe\\shell\\open\\command";
            String cmdCommand = String.format("\"cmd /c reg add \\\"%s\\\" /ve /d \\\"\\\"%s\\\" \\\"%%1\\\"\\\" /f\"",
                    regPath,
                    newBaRatinAGEPath);
            // note we could remove the NoExit argument...
            String pwsCommand = String.format(
                    "Start-Process powershell -Verb RunAs -ArgumentList '-NoExit', '-Command', '%s'", cmdCommand);

            ConsoleLogger.log("Writing powershell script ...");
            WriteFile.writeLines(scriptPath, new String[] { pwsCommand });
            ConsoleLogger.log("Executing powershell script ...");
            String command = "powershell.exe -ExecutionPolicy Bypass -File " + scriptPath;
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            int exitCode = powerShellProcess.waitFor();
            ConsoleLogger.log("PowerShell script exited with code: " + exitCode);

        } catch (Exception e) {
            ConsoleLogger.error(e);
        }

        try {

            File f = new File(scriptPath);
            if (f.delete()) {
                ConsoleLogger.log("Successfuly deleted script file");
            } else {
                ConsoleLogger.error("Failed to deleted script file");
            }
        } catch (Exception e) {
            ConsoleLogger.error(e);
        }

        ConsoleLogger.log("Registry should be updated.");
    }
}
