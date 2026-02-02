package org.baratinage.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.baratinage.App;
import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemList;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.baratin.Gaugings;
import org.baratinage.ui.baratin.HydraulicConfiguration;
import org.baratinage.ui.baratin.HydraulicConfigurationBAC;
import org.baratinage.ui.baratin.HydraulicConfigurationQFH;
import org.baratinage.ui.baratin.Hydrograph;
import org.baratinage.ui.baratin.Limnigraph;
import org.baratinage.ui.baratin.RatingCurve;
import org.baratinage.ui.baratin.RatingCurveCompare;
import org.baratinage.ui.baratin.RatingShiftHappens;
import org.baratinage.ui.baratin.hydraulic_configuration.PriorRatingCurve;
import org.baratinage.ui.plot.EditablePlot;
import org.baratinage.ui.plot.EditablePlotItem;
import org.baratinage.ui.plot.PlotEditor;
import org.baratinage.utils.ConsoleLogger;
import org.baratinage.utils.Misc;
import org.baratinage.utils.fs.WriteFile;

public class DebugMenu extends JMenu {

    public DebugMenu() {
        super("Debug");

        JMenuItem resetPlotTextsBtn = new JMenuItem("Resets legends and axis labels");
        add(resetPlotTextsBtn);
        resetPlotTextsBtn.addActionListener((e) -> {

        });

        JMenuItem restartAppBtn = new JMenuItem("Restart App");
        add(restartAppBtn);
        restartAppBtn.addActionListener((e) -> {
            App.restart();
        });

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
            ConsoleLogger
                    .error("Cannot updating Windows registry because BaRatinAGE is not packaged in an executable file");
            return;
        }

        String newBaRatinAGEPath = Path.of(AppSetup.PATH_APP_ROOT_DIR, String.format("%s.exe", AppSetup.APP_NAME))
                .toString();

        ConsoleLogger.log("Starting Windows registry update...");
        ConsoleLogger.log("New BaRatinAGE path is: ");

        String regPath = "HKEY_CLASSES_ROOT\\Applications\\BaRatinAGE.exe\\shell\\open\\command";
        String cmdCommand = String.format("reg add %s /ve /d \\\"\"%s\" \"%%1\"\\\" /f",
                regPath,
                newBaRatinAGEPath);

        if (runAdminCmdCommand(cmdCommand)) {

            ConsoleLogger.log("Registry should be updated.");
        } else {
            ConsoleLogger.log("Registry update failed.");

        }
    }

    private static boolean runAdminCmdCommand(String cmd) {

        String randomId = Misc.getTimeStampedId();
        String scriptPath = Path.of(AppSetup.PATH_APP_TEMP_DIR, String.format("script_%s.ps1", randomId)).toString();
        int exitCode = -1;
        try {

            // String cmdCommand = String.format("\"cmd /c %s\"", cmd);
            String cmdCommand = String.format("cmd /c %s", cmd);

            String pwsCommand = String.format(
                    "Start-Process powershell -Verb RunAs -ArgumentList '-NoExit', '-Command', '%s'", cmdCommand);

            ConsoleLogger.log("Writing powershell script ...");
            WriteFile.writeLines(scriptPath, new String[] { pwsCommand });
            ConsoleLogger.log("Executing powershell script ...");
            String[] command = new String[] { "powershell.exe", "-ExecutionPolicy", "Bypass", "-File", scriptPath };
            Process powerShellProcess = Runtime.getRuntime().exec(command);
            exitCode = powerShellProcess.waitFor();
            ConsoleLogger.log("PowerShell script exited with code: " + exitCode);

        } catch (Exception e) {
            ConsoleLogger.error(e);
            return false;
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

        return exitCode == 0;
    }

    public static void resetPlotLegendsAndAxis() {
        if (AppSetup.MAIN_FRAME.currentProject == null) {
            return;
        }
        BamItemList selectedBamItems;
        PlotEditor pe;
        EditablePlotItem epi;
        EditablePlot ep;
        // gaugings
        selectedBamItems = AppSetup.MAIN_FRAME.currentProject.BAM_ITEMS
                .filterByType(BamItemType.GAUGINGS);
        for (BamItem bi : selectedBamItems) {
            if (bi instanceof Gaugings g) {
                epi = g.plotPanel.plotEditor.getEditablePlotItem("active_gaugings");
                if (epi != null) {
                    epi.setLabel(T.text("lgd_active_gaugings"));
                }
                epi = g.plotPanel.plotEditor.getEditablePlotItem("inactive_gaugings");
                if (epi != null) {
                    epi.setLabel(T.text("lgd_inactive_gaugings"));
                }
                ep = g.plotPanel.plotEditor.getEditablePlot();
                if (ep != null) {
                    ep.setXAxisLabel(T.text("stage") + " [m]");
                    ep.setYAxisLabel(T.text("discharge") + " [m]");
                }
            }
        }
        // prior rating curve
        selectedBamItems = AppSetup.MAIN_FRAME.currentProject.BAM_ITEMS
                .filterByType(BamItemType.HYDRAULIC_CONFIG,
                        BamItemType.HYDRAULIC_CONFIG_BAC,
                        BamItemType.HYDRAULIC_CONFIG_QFH);
        for (BamItem bi : selectedBamItems) {
            PriorRatingCurve<?> prc = null;
            if (bi instanceof HydraulicConfiguration hc) {
                prc = hc.priorRatingCurve;
            } else if (bi instanceof HydraulicConfigurationBAC hc) {
                prc = hc.priorRatingCurve;
            } else if (bi instanceof HydraulicConfigurationQFH hc) {
                prc = hc.priorRatingCurve;
            }
            if (prc != null) {
                pe = prc.resultsPanel.ratingCurvePlot.plotEditor;
                resetRCPlotLegendsAndAxis(pe, true);
            }
        }
        // posterior rating curve
        selectedBamItems = AppSetup.MAIN_FRAME.currentProject.BAM_ITEMS
                .filterByType(BamItemType.RATING_CURVE);
        for (BamItem bi : selectedBamItems) {
            if (bi instanceof RatingCurve rc) {
                pe = rc.resultsPanel.ratingCurvePlot.plotEditor;
                resetRCPlotLegendsAndAxis(pe, false);
                pe = rc.resultsPanel.rcResiduals.plotEditor;
                ep = pe.getEditablePlot("index");
                if (ep != null) {
                    ep.setXAxisLabel(T.text("index"));
                    ep.setYAxisLabel(T.text("residuals"));
                }
                ep = pe.getEditablePlot("time");
                if (ep != null) {
                    ep.setXAxisLabel(T.text("date_time"));
                    ep.setYAxisLabel(T.text("residuals"));
                }
                ep = pe.getEditablePlot("simQ");
                if (ep != null) {
                    ep.setXAxisLabel(T.text("sim_discharge") + " [m3/s]");
                    ep.setYAxisLabel(T.text("residuals"));
                }
                ep = pe.getEditablePlot("obsQ");
                if (ep != null) {
                    ep.setXAxisLabel(T.text("obs_discharge") + " [m3/s]");
                    ep.setYAxisLabel(T.text("residuals"));
                }
                ep = pe.getEditablePlot("stage");
                if (ep != null) {
                    ep.setXAxisLabel(T.text("stage") + " [m]");
                    ep.setYAxisLabel(T.text("residuals"));
                }
            }
        }
        // limnigraph
        selectedBamItems = AppSetup.MAIN_FRAME.currentProject.BAM_ITEMS
                .filterByType(BamItemType.LIMNIGRAPH);
        for (BamItem bi : selectedBamItems) {
            if (bi instanceof Limnigraph li) {
                pe = li.limniPlot.plotEditor;
                ep = pe.getEditablePlot();
                if (ep != null) {
                    ep.setXAxisLabel(T.text("time"));
                    ep.setYAxisLabel(T.text("stage") + " [m]");
                }
                epi = pe.getEditablePlotItem("u");
                if (epi != null) {
                    epi.setLabel(T.text("stage_uncertainty"));
                }
                epi = pe.getEditablePlotItem("stage");
                if (epi != null) {
                    epi.setLabel(T.text("limnigraph"));
                }
            }
        }
        // hydrograph
        selectedBamItems = AppSetup.MAIN_FRAME.currentProject.BAM_ITEMS
                .filterByType(BamItemType.HYDROGRAPH);
        for (BamItem bi : selectedBamItems) {
            if (bi instanceof Hydrograph hy) {
                pe = hy.plotPanel.plotEditor;
                ep = pe.getEditablePlot();
                if (ep != null) {
                    ep.setXAxisLabel(T.text("time"));
                    ep.setYAxisLabel(T.text("discharge") + " [m3/s]");
                }
                epi = pe.getEditablePlotItem("maxpost");
                if (epi != null) {
                    epi.setLabel(T.text("lgd_discharge_maxpost"));
                }
                epi = pe.getEditablePlotItem("limniu");
                if (epi != null) {
                    epi.setLabel(T.text("lgd_discharge_limni_u"));
                }
                epi = pe.getEditablePlotItem("paramu");
                if (epi != null) {
                    epi.setLabel(T.text("lgd_discharge_limni_param_u"));
                }
                epi = pe.getEditablePlotItem("totalu");
                if (epi != null) {
                    epi.setLabel(T.text("lgd_discharge_total_u"));
                }
            }
        }
        // rc comparator
        selectedBamItems = AppSetup.MAIN_FRAME.currentProject.BAM_ITEMS
                .filterByType(BamItemType.COMPARING_RATING_CURVES);
        for (BamItem bi : selectedBamItems) {
            if (bi instanceof RatingCurveCompare rcc) {
                rcc.stageAxixLabelField.setText(
                        T.text("stage") + " [m]");
                rcc.dischargeAxisLabelField.setText(
                        T.text("discharge") + " [m3/s]");

                for (Entry<BamItem, HashMap<String, EditablePlotItem>> e : rcc.knownEditablePlotItems.entrySet()) {
                    BamItem item = e.getKey();
                    boolean isPrior = !(item instanceof RatingCurve);
                    HashMap<String, EditablePlotItem> hm = e.getValue();
                    epi = hm.get("stage_transition_value");
                    String stageLegendText = isPrior ? "lgd_prior_activation_stage"
                            : "lgd_posterior_activation_stage";
                    if (epi != null) {
                        epi.setLabel(T.text(stageLegendText));
                    }
                    epi = hm.get("stage_transition_u");
                    if (epi != null) {
                        epi.setLabel(T.text(stageLegendText));
                    }
                    epi = hm.get("param_u");
                    if (epi != null) {
                        String paramLegendKey = isPrior ? "lgd_prior_parametric_uncertainty"
                                : "lgd_posterior_parametric_uncertainty";
                        epi.setLabel(T.text(paramLegendKey));
                    }
                    epi = hm.get("total_u");
                    if (epi != null) {
                        epi.setLabel(T.text("lgd_posterior_parametric_structural_uncertainty"));
                    }
                    epi = hm.get("maxpost");
                    if (epi != null) {
                        String mpLegendKey = isPrior ? "lgd_prior_rating_curve" : "lgd_posterior_rating_curve";
                        epi.setLabel(T.text(mpLegendKey));
                    }
                }
                rcc.setPlotItemOrder(rcc.episList.getAllObjects());
                rcc.resetPlot();
            }
        }
        // rating shift detector
        selectedBamItems = AppSetup.MAIN_FRAME.currentProject.BAM_ITEMS
                .filterByType(BamItemType.RATING_SHIFT_HAPPENS);
        for (BamItem bi : selectedBamItems) {
            if (bi instanceof RatingShiftHappens rsh) {
                pe = rsh.ratingShiftResults.results.mainPlot.plotEditor;
                boolean isDischargePlot = rsh.ratingShiftResults.results.mainPlot.radioDischargeOrStage
                        .getSelectedId()
                        .equals("q");
                if (pe != null) {
                    ep = pe.getEditablePlot("mainPlot");
                    if (ep != null) {
                        ep.setYAxisLabel(
                                isDischargePlot
                                        ? "%s [m3/s]".formatted(T.text("discharge"))
                                        : "%s [m]".formatted(T.text("stage")));
                    }
                }
                pe = rsh.ratingShiftResults.results.gaugings.plotEditor;
                if (pe != null) {
                    ep = pe.getEditablePlot();
                    if (ep != null) {
                        ep.setXAxisLabel(T.text("stage") + " [m]");
                        ep.setYAxisLabel(T.text("discharge") + " [m]");
                    }
                }
            }
        }
    }

    private static void resetRCPlotLegendsAndAxis(PlotEditor pe, boolean isPrior) {
        EditablePlotItem epi;
        EditablePlot ep;

        ep = pe.getEditablePlot();
        if (ep != null) {
            ep.setXAxisLabel(T.text("stage") + " [m]");
            ep.setYAxisLabel(T.text("discharge") + " [m]");
        }
        epi = pe.getEditablePlotItem("gaugingsPoints");
        if (epi != null) {
            epi.setLabel(T.text("lgd_active_gaugings"));
        }
        epi = pe.getEditablePlotItem("maxpostLine");
        if (epi != null) {
            String mpLegendKey = isPrior ? "lgd_prior_rating_curve" : "lgd_posterior_rating_curve";
            epi.setLabel(T.text(mpLegendKey));
        }
        epi = pe.getEditablePlotItem("paramUncertaintyBand");
        if (epi != null) {
            String paramLegendKey = isPrior ? "lgd_prior_parametric_uncertainty"
                    : "lgd_posterior_parametric_uncertainty";
            epi.setLabel(T.text(paramLegendKey));
        }
        epi = pe.getEditablePlotItem("totalUncertaintyBand");
        if (epi != null) {
            epi.setLabel(T.text("lgd_posterior_parametric_structural_uncertainty"));
        }
        String stageLegendText = isPrior ? "lgd_prior_activation_stage"
                : "lgd_posterior_activation_stage";
        epi = pe.getEditablePlotItem("stageLine");
        if (epi != null) {
            epi.setLabel(T.text(stageLegendText));
        }
        epi = pe.getEditablePlotItem("stageBand");
        if (epi != null) {

            epi.setLabel(T.text(stageLegendText));
        }
    }
}
