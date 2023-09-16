package org.baratinage.jbam;

import java.util.ArrayList;
import java.util.List;

public class CalibrationDataResiduals {

    // - Xi (obs, true, )
    // FIXME: better names needed
    public record InputDataResiduals(String name, double[] obsValues, double[] trueValues) {

        @Override
        public String toString() {
            return String.format("Input data residuals '%s' containing %d rows", name, obsValues.length);
        }
    }

    // - Yi (obs, unbiased, sim, res, stdres)
    // FIXME: better names needed
    public record OutputDataResiduals(
            String name,
            double[] obsValues,
            double[] unbiasedValues,
            double[] simValues,
            double[] resValues,
            double[] stdresValues) {

        @Override
        public String toString() {
            return String.format("Output data residuals '%s' containing %d rows", this.name, this.obsValues.length);
        }
    }

    public final List<InputDataResiduals> inputResiduals;
    public final List<OutputDataResiduals> outputResiduals;

    public CalibrationDataResiduals(List<double[]> calDataResidualMatrix, CalibrationData calibrationData) {

        inputResiduals = new ArrayList<>();
        outputResiduals = new ArrayList<>();

        // Example columns of a residual matrix with 3 inputs and 2 outputs:
        // X1_obs, X2_obs, X3_obs, X1_true, X2_true, X3_true,
        // Y1_obs, Y2_obs, Y1_unbiased, Y2_unbiased, Y1_sim, Y2_sim
        // Y1_res, Y2_res, Y1_stdres, Y2_stdres

        UncertainData[] inputs = calibrationData.inputs;
        int nInputs = inputs.length;
        int k = 0;
        for (UncertainData i : inputs) {
            inputResiduals.add(
                    new InputDataResiduals(
                            i.name,
                            calDataResidualMatrix.get(k),
                            calDataResidualMatrix.get(k + nInputs)));
            k++;
        }

        UncertainData[] outputs = calibrationData.outputs;
        int nOuputs = outputs.length;
        k = 0;
        for (UncertainData o : outputs) {
            outputResiduals.add(
                    new OutputDataResiduals(
                            o.name,
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
        for (InputDataResiduals i : inputResiduals) {
            str += "   > " + i.toString() + "\n";
        }
        str += " - Outputs: \n";
        for (OutputDataResiduals o : outputResiduals) {
            str += "   > " + o.toString() + "\n";
        }
        return str;
    }
}
