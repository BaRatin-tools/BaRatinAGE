package org.baratinage.jbam;

import java.nio.file.Path;

import org.baratinage.jbam.Distribution.DISTRIB;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.jbam.utils.ConfigFile;

public class StructuralErrorModel {
    private String name;
    private String modelId;
    private Parameter[] parameters;

    public StructuralErrorModel(String name, String modelId, Parameter[] parameters) {
        this.name = name;
        this.modelId = modelId;
        this.parameters = parameters;
    }

    public String getConfigFileName() {
        String configFileName = String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, this.name);
        return configFileName;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void toFiles(String workspace) {
        ConfigFile configFile = new ConfigFile();
        configFile.addItem(this.modelId, "Function f used in sdev=f(Qrc) ", true);
        configFile.addItem(this.parameters.length, "Number of parameters gamma for f");

        for (Parameter p : this.parameters) {
            configFile.addItem(p.getName(), "Parameter name -----", true);
            configFile.addItem(p.getInitialGuess(), "Initial guess");
            Distribution d = p.getDistribution();
            configFile.addItem(d.getName(), "Prior distribution", true);
            configFile.addItem(d.getParameterValues(), "Prior parameters");
        }

        String configFileName = this.getConfigFileName();
        configFile.writeToFile(Path.of(workspace, configFileName).toString());
    }

    @Override
    public String toString() {
        String str = String.format("Structural Error Model %s of type '%s':\n", this.name, this.modelId);
        for (Parameter p : this.parameters) {
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
            Distribution d = new Distribution(DISTRIB.getDistribFromName(distName), distParams);
            modelParameters[k] = new Parameter(parameterName, initialGuess, d);
        }

        return new StructuralErrorModel(structErrorModelConfigFileName, modelId, modelParameters);
    }
}
