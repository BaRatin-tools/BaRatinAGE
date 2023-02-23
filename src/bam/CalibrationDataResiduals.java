package bam;

import java.util.HashMap;
import java.util.List;

public class CalibrationDataResiduals {

    // - Xi (obs, true, )
    public class InputDataResiduals {
        public String name;
        // FIXME: better names needed
        public double[] obsValues;
        public double[] trueValues;

        InputDataResiduals(String name, double[] obsValues, double[] trueValues) {
            this.name = name;
            this.obsValues = obsValues;
            this.trueValues = trueValues;
        }

        @Override
        public String toString() {
            return String.format("Input data residuals '%s' containing %d rows", this.name, this.obsValues.length);
        }
    }

    // - Yi (obs, unbiased, sim, res, stdres)
    public class OutputDataResiduals {

        public String name;
        // FIXME: better names needed
        public double[] obsValues;
        public double[] unbiasedValues;
        public double[] simValues;
        public double[] resValues;
        public double[] stdresValues;

        OutputDataResiduals(
                String name,
                double[] obsValues,
                double[] unbiasedValues,
                double[] simValues,
                double[] resValues,
                double[] stdresValues) {
            this.name = name;
            this.obsValues = obsValues;
            this.unbiasedValues = unbiasedValues;
            this.simValues = simValues;
            this.resValues = resValues;
            this.stdresValues = stdresValues;
        }

        @Override
        public String toString() {
            return String.format("Output data residuals '%s' containing %d rows", this.name, this.obsValues.length);
        }
    }

    private HashMap<String, InputDataResiduals> inputResiduals;
    private HashMap<String, OutputDataResiduals> outputResiduals;

    public CalibrationDataResiduals(List<double[]> calDataResidualMatrix, CalibrationData calibrationData) {

        this.inputResiduals = new HashMap<>();
        this.outputResiduals = new HashMap<>();

        // Example columns of a residual matrix with 3 inputs and 2 outputs:
        // X1_obs, X2_obs, X3_obs, X1_true, X2_true, X3_true,
        // Y1_obs, Y2_obs, Y1_unbiased, Y2_unbiased, Y1_sim, Y2_sim
        // Y1_res, Y2_res, Y1_stdres, Y2_stdres

        UncertainData[] inputs = calibrationData.getInputs();
        int nInputs = inputs.length;
        int k = 0;
        for (UncertainData i : inputs) {
            this.inputResiduals.put(i.getName(),
                    new InputDataResiduals(
                            i.getName(),
                            calDataResidualMatrix.get(k),
                            calDataResidualMatrix.get(k + nInputs)));
            k++;
        }

        UncertainData[] outputs = calibrationData.getOutputs();
        int nOuputs = outputs.length;
        k = 0;
        for (UncertainData o : outputs) {
            this.outputResiduals.put(o.getName(),
                    new OutputDataResiduals(
                            o.getName(),
                            calDataResidualMatrix.get(nInputs * 2 + k),
                            calDataResidualMatrix.get(nInputs * 2 + k + nOuputs * 1),
                            calDataResidualMatrix.get(nInputs * 2 + k + nOuputs * 2),
                            calDataResidualMatrix.get(nInputs * 2 + k + nOuputs * 3),
                            calDataResidualMatrix.get(nInputs * 2 + k + nOuputs * 4)

                    ));
            k++;
        }

    }

    @Override
    public String toString() {
        String str = "Calibration data residulas: \n";
        str += " - Inputs: \n";
        for (InputDataResiduals i : this.inputResiduals.values()) {
            str += "   > " + i.toString() + "\n";
        }
        str += " - Outputs: \n";
        for (OutputDataResiduals o : this.outputResiduals.values()) {
            str += "   > " + o.toString() + "\n";
        }
        return str;
    }
}
