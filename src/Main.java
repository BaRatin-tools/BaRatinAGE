
// import java.awt.EventQueue;
// import javax.swing.UIManager;

// import ui.MainFrame;

import bam.BaM;
import bam.CalDataResidualConfig;
import bam.CalibrationConfig;
import bam.CalibrationData;
import bam.Distribution;
import bam.McmcConfig;
import bam.McmcCookingConfig;
import bam.McmcSummaryConfig;
import bam.Model;
import bam.ModelOutput;
import bam.Parameter;
import bam.PredictionConfig;
import bam.PredictionInput;
import bam.PredictionOutput;
import bam.RunOptions;
import bam.StructuralErrorModel;
import bam.UncertainData;

import utils.FileReadWrite;
import utils.Matrix;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hey");
        // ----------------------------------------------------------
        // MODEL DEFINITION
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

        String xTra = "3 \nt, T1,T2 \n4 \nK1,P0,r,K2\n2\nK1/(1+((K1-P0)/P0)*exp(-r*T1*t))\nK2/(1+((K2-P0)/P0)*exp(-r*T2*t))";

        Model model = new Model("TextFile", 3, 2, parameters, xTra);

        // model.log();

        // ----------------------------------------------------------
        // MODEL OUTPUTS
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

        // for (ModelOutput mo : modelOutputs) {
        // mo.log();
        // }

        // ----------------------------------------------------------
        // CALIBRATION DATA
        double[][] rawData = FileReadWrite.readMatrix("./test/twoPop.txt", ";", 1,
                "NA");
        double[][] data = Matrix.transpose(rawData);
        // FileReadWrite.printMatrix(data);
        // Matrix.prettyPrint(Matrix.transpose(data));
        // Matrix.prettyPrint(data);
        UncertainData[] inputs = {
                new UncertainData("t", data[0]),
                new UncertainData("T1", data[1]),
                new UncertainData("T2", data[2])
        };
        UncertainData[] outputs = {
                new UncertainData("P1", data[3]),
                new UncertainData("P2", data[4]),
        };
        CalibrationData calibrationData = new CalibrationData("twoPop", inputs,
                outputs);
        // calibrationData.log();

        // ----------------------------------------------------------
        // ADDITIONAL CONFIGURATION
        CalDataResidualConfig calDataResidualConfig = new CalDataResidualConfig();
        McmcConfig mcmcConfig = new McmcConfig();
        McmcCookingConfig mcmcCookingConfig = new McmcCookingConfig();
        McmcSummaryConfig mcmcSummaryConfig = new McmcSummaryConfig();
        // calDataResidualConfig.log();
        // mcmcConfig.log();
        // mcmcCookingConfig.log();
        // mcmcSummaryConfig.log();

        // ----------------------------------------------------------
        // CALIBRATION CONFIGURATION
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

        PredictionInput[] predInputs = new PredictionInput[] {
                new PredictionInput("t", Matrix.transpose(new double[][] { data[0] })),
                new PredictionInput("T1", Matrix.transpose(new double[][] { data[1] })),
                new PredictionInput("T2", Matrix.transpose(new double[][] { data[2] })),
        };
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
        PredictionConfig[] predConfigs = new PredictionConfig[] {
                new PredictionConfig(
                        "TestWithCalibData",
                        predInputs,
                        predOutputs,
                        new PredictionOutput[] {},
                        true,
                        true,
                        -1)
        };

        // predConfig.log();
        // ----------------------------------------------------------
        // BaM
        RunOptions runOptions = new RunOptions(true, true, true, false);
        BaM bam = new BaM(calibrationConfig, predConfigs, runOptions);
        // bam.log();

        bam.writeConfigFiles("test/testWorkspace");
        bam.run();

        // try {
        // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // EventQueue.invokeLater(new Runnable() {
        // public void run() {
        // try {
        // new MainFrame();

        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // });
    }
}
