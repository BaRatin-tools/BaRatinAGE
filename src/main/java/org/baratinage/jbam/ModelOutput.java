package org.baratinage.jbam;

public class ModelOutput {
    public final int outputIndex;
    public final String name;
    public final StructuralErrorModel structuralErrorModel;

    public ModelOutput(int outputIndex, StructuralErrorModel structuralErrorModel) {
        this.outputIndex = outputIndex;
        this.name = "Y" + (outputIndex + 1);
        this.structuralErrorModel = structuralErrorModel;
    }

    public void toFiles(String workspace) {
        structuralErrorModel.toFiles(workspace);
    }

    @Override
    public String toString() {
        String str = String.format("Model output %s associated with the following structural error model.",
                outputIndex);
        str = str + "\n" + structuralErrorModel.toString();
        return str;
    }
}
