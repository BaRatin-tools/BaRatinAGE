package org.baratinage.jbam;

import java.nio.file.Path;

import org.baratinage.jbam.Distribution.DISTRIBUTION;
import org.baratinage.jbam.utils.ConfigFile;

public class StructuralErrorModel {
    public final String name;
    public final String fileName;
    public final String modelId;
    public final Parameter[] parameters;

    public StructuralErrorModel(String name, String fileName, String modelId, Parameter[] parameters) {
        this.name = name;
        this.fileName = fileName;
        this.modelId = modelId;
        this.parameters = parameters;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(modelId, "Function f used in sdev=f(Qrc) ", true);
        configFile.addItem(parameters.length, "Number of parameters gamma for f");

        for (Parameter p : parameters) {
            configFile.addItem(p.name, "Parameter name -----", true);
            configFile.addItem(p.initalGuess, "Initial guess");
            Distribution d = p.distribution;
            configFile.addItem(d.distribution.bamName, "Prior distribution", true);
            configFile.addItem(d.parameterValues, "Prior parameters");
        }

        configFile.writeToFile(Path.of(workspace, fileName).toString());
    }

    @Override
    public String toString() {
        String str = String.format("Structural Error Model %s of type '%s':\n", name, modelId);
        for (Parameter p : parameters) {
            str += p.toString() + "\n";
        }
        return str;
    }

    static public StructuralErrorModel readStructuralErrorModel(String workspace,
            String structErrorModelConfigFileName) {
        ConfigFile configFile = ConfigFile.readConfigFile(workspace, structErrorModelConfigFileName);

        String modelId = configFile.getString(0);
        int nPars = configFile.getInt(1);

        Parameter[] modelParameters = new Parameter[nPars];
        for (int k = 0; k < nPars; k++) {
            String parameterName = configFile.getString(1 + k * 4 + 1);
            double initialGuess = configFile.getDouble(1 + k * 4 + 2);
            String distName = configFile.getString(1 + k * 4 + 3);
            double[] distParams = configFile.getDoubleArray(1 + k * 4 + 4);
            Distribution d = new Distribution(DISTRIBUTION.getDistribFromBamName(distName), distParams);
            modelParameters[k] = new Parameter(parameterName, initialGuess, d);
        }

        return new StructuralErrorModel(
                structErrorModelConfigFileName,
                structErrorModelConfigFileName,
                modelId,
                modelParameters);
    }
}
