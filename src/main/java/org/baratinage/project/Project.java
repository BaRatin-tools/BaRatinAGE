package org.baratinage.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.baratinage.jbam.BaM;
import org.baratinage.jbam.CalDataResidualConfig;
import org.baratinage.jbam.CalibrationConfig;
import org.baratinage.jbam.CalibrationData;
import org.baratinage.jbam.Distribution;
import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;
import org.baratinage.jbam.McmcSummaryConfig;
import org.baratinage.jbam.Model;
import org.baratinage.jbam.ModelOutput;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.PredictionConfig;
import org.baratinage.jbam.PredictionInput;
import org.baratinage.jbam.PredictionOutput;
import org.baratinage.jbam.RunOptions;
import org.baratinage.jbam.StructuralErrorModel;
import org.baratinage.jbam.UncertainData;
import org.baratinage.jbam.utils.Read;

public class Project {
        public Project() {

        }

        public BaM createTest() {

                // ----------------------------------------------------------
                // MODEL DEFINITION
                System.out.println("Creating model definition...");
                Parameter[] parameters = new Parameter[] {
                                new Parameter("K1", 10000,
                                                Distribution.LogNormal(9, 1)),
                                new Parameter("P0", 100,
                                                Distribution.Gaussian(100, 10)),
                                new Parameter("r", 0.001,
                                                Distribution.LogNormal(-7, 1)),
                                new Parameter("K2", 10000,
                                                Distribution.LogNormal(9, 1)),
                };

                // FIXME: have a proper object to store and handle xTra
                String xTra = "3 \nt, T1,T2 \n4 \nK1,P0,r,K2\n2\nK1/(1+((K1-P0)/P0)*exp(-r*T1*t))\nK2/(1+((K2-P0)/P0)*exp(-r*T2*t))";

                Model model = new Model("TextFile", 3, 2, parameters, xTra);

                // ----------------------------------------------------------
                // MODEL OUTPUTS
                System.out.println("Creating model outputs...");
                StructuralErrorModel linearErrModel = new StructuralErrorModel("P1andP2",
                                "Linear",
                                new Parameter[] {
                                                new Parameter("gamma1", 1, Distribution.Uniform(0, 1000)),
                                                new Parameter("gamma2", 0.1, Distribution.Uniform(0, 1000)),
                                });
                ModelOutput[] modelOutputs = new ModelOutput[] {
                                new ModelOutput("P1", linearErrModel),
                                new ModelOutput("P2", linearErrModel),
                };

                // ----------------------------------------------------------
                // TEST DATA
                System.out.println("Importing test data...");
                List<double[]> data;
                try {
                        data = Read.readMatrix("./test/twoPop.txt", ";", 1, 0,
                                        "NA", true);
                } catch (IOException e) {
                        System.err.println(e);
                        return null;
                }
                Read.prettyPrintMatrix(data);

                System.out.println("Creating fake big test data...");
                int nRepeat = 10;
                // Creating a big version
                List<double[]> repeatedData = data.stream().map((column -> {
                        int n = column.length;
                        double[] repeatedColumn = new double[n * nRepeat];
                        for (int i = 0; i < nRepeat; i++) {
                                for (int j = 0; j < n; j++) {
                                        repeatedColumn[i * n + j] = column[j];
                                }
                        }
                        return repeatedColumn;
                })).collect(Collectors.toList());
                Read.prettyPrintMatrix(repeatedData);

                // ----------------------------------------------------------
                // CALIBRATION DATA
                System.out.println("Creating calibration data inputs and outputs ...");
                UncertainData[] inputs = {
                                new UncertainData("t", data.get(0)),
                                new UncertainData("T1", data.get(1)),
                                new UncertainData("T2", data.get(2))
                };
                UncertainData[] outputs = {
                                new UncertainData("P1", data.get(3)),
                                new UncertainData("P2", data.get(4)),
                };

                CalibrationData calibrationData = new CalibrationData("twoPop",
                                inputs, outputs);

                // ----------------------------------------------------------
                // ADDITIONAL CONFIGURATION
                System.out.println("Creating additional configuration objects ...");
                CalDataResidualConfig calDataResidualConfig = new CalDataResidualConfig();
                McmcConfig mcmcConfig = new McmcConfig();
                McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
                McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig();

                // ----------------------------------------------------------
                // CALIBRATION CONFIGURATION
                System.out.println("Creating model calibration with normal data...");
                CalibrationConfig calibrationConfig = new CalibrationConfig(
                                model,
                                modelOutputs,
                                calibrationData,
                                mcmcConfig,
                                mcmcCookingConfig,
                                mcmcSummaryConfig,
                                calDataResidualConfig);

                // ----------------------------------------------------------
                // PREDICTION CONFIGURATION

                List<double[]> t = new ArrayList<>();
                List<double[]> T1 = new ArrayList<>();
                List<double[]> T2 = new ArrayList<>();
                int nSpag = 10;
                for (int k = 0; k < nSpag; k++) {
                        t.add(repeatedData.get(0));
                        T1.add(repeatedData.get(1));
                        T2.add(repeatedData.get(2));
                }

                System.out.println("Creating prediction inputs ...");
                PredictionInput[] predInputs = new PredictionInput[] {
                                new PredictionInput("t", t),
                                new PredictionInput("T1", T1),
                                new PredictionInput("T2", T2),
                };

                System.out.println("Creating prediction outputs ...");
                PredictionOutput[] predOutputs = new PredictionOutput[] {
                                new PredictionOutput(
                                                "P0",
                                                true,
                                                true,
                                                true),
                                new PredictionOutput(
                                                "P1",
                                                true,
                                                true,
                                                true)
                };

                System.out.println("Creating prediction configuration ...");
                PredictionConfig[] predConfigs = new PredictionConfig[] {

                                new PredictionConfig(
                                                "TestWithCalibData",
                                                new PredictionInput[] {
                                                                new PredictionInput("t_small", data.subList(0, 1)),
                                                                new PredictionInput("T1_small", data.subList(1, 2)),
                                                                new PredictionInput("T2_small", data.subList(2, 3)),
                                                },
                                                predOutputs,
                                                new PredictionOutput[] {},
                                                true,
                                                true,
                                                -1),
                                new PredictionConfig(
                                                "TestWithCalibDataBig",
                                                predInputs,
                                                predOutputs,
                                                new PredictionOutput[] {},
                                                true,
                                                true,
                                                -1)
                };

                // ----------------------------------------------------------
                // BaM

                System.out.println("Creating final BaM configuration...");
                RunOptions runOptions = new RunOptions(
                                true,
                                true,
                                true,
                                true);

                boolean useRepeatedDataInCalibration = false;
                BaM bam;
                if (!useRepeatedDataInCalibration) {
                        bam = new BaM(calibrationConfig, predConfigs, runOptions, null, null);
                } else {
                        // Calibration using repeated data instead of regular data for longer MCMC
                        // sampling
                        CalibrationConfig calibrationConfigBig = new CalibrationConfig(
                                        model,
                                        modelOutputs,
                                        new CalibrationData("twoPop",
                                                        new UncertainData[] {
                                                                        new UncertainData("t", repeatedData.get(0)),
                                                                        new UncertainData("T1", repeatedData.get(1)),
                                                                        new UncertainData("T2", repeatedData.get(2))
                                                        },
                                                        new UncertainData[] {
                                                                        new UncertainData("P1", repeatedData.get(3)),
                                                                        new UncertainData("P2", repeatedData.get(4)),
                                                        }),
                                        mcmcConfig,
                                        mcmcCookingConfig,
                                        mcmcSummaryConfig,
                                        calDataResidualConfig);
                        bam = new BaM(calibrationConfigBig, predConfigs, runOptions, null, null);
                }
                System.out.println(bam);
                return bam;
        }
}
