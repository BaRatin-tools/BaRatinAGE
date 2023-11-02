package org.baratinage;

import java.io.File;
import java.nio.file.Paths;

import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;

import java.io.IOException;

import java.lang.ProcessBuilder.Redirect;

public class Build {

    public static void main(String[] args) {

        ConsoleLogger.init();

        String name = "BaRatinAGE";
        String iconPath = "resources\\icons\\icon.ico";
        String version = "3.0.0-alpha1";
        boolean winConsole = true;
        String[] dirToCopy = new String[] {
                "exe", "resources", "log"
        };

        String nameVersion = name + "-" + version;
        String jpacakgeVersion = version.split("-")[0];

        File rootFolder = Paths.get("").toAbsolutePath().toFile();
        File targetDir = new File(".\\target");
        File targetPackagedDir = new File(".\\target-packaged\\" + nameVersion);

        ConsoleLogger.log("Emptying target directory...");
        if (!Misc.deleteDirContent(targetDir)) {
            ConsoleLogger.log("Failed to delete target dir content");
            return;
        }
        if (targetPackagedDir.exists()) {
            ConsoleLogger.log("Deleting directory '" + targetPackagedDir.toString() + "'...");
            if (!Misc.deleteDir(targetPackagedDir)) {
                ConsoleLogger.log("Failed to delete target packaged dir ");
                return;
            }
        }

        ConsoleLogger.log("Setting MVN build version in pom.xml file...");
        runCommand(rootFolder, "cmd", " /c", "mvn", "versions:set", "-DnewVersion=\"" + version + "\"");

        ConsoleLogger.log("Running MVN clean + build ...");
        runCommand(rootFolder, "cmd", " /c", "mvn", "clean", "package");

        ConsoleLogger.log("Packaging ...");
        runCommand(rootFolder, "cmd", " /c", "jpackage",
                "--type", "app-image",
                "--app-version", jpacakgeVersion,
                "--name", nameVersion,
                "--dest", "target-packaged",
                "--input", "target",
                "--main-jar", nameVersion + ".jar",
                "--icon", iconPath,
                winConsole ? "--win-console" : "");

        ConsoleLogger.log("Copying resource directories ...");
        for (String d : dirToCopy) {
            runCommand(rootFolder, "cmd", " /c",
                    "xcopy",
                    ".\\" + d,
                    ".\\target-packaged\\" + nameVersion + "\\" + d,
                    "/S", "/I");
        }
    }

    private static void runCommand(File folder, String... cmd) {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(folder);
        try {
            pb.redirectOutput(Redirect.INHERIT);
            pb.redirectError(Redirect.INHERIT);
            Process p = pb.start();

            p.waitFor();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
