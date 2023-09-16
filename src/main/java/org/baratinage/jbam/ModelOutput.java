package org.baratinage.jbam;

public class ModelOutput {
    public final String name;
    public final StructuralErrorModel structuralErrorModel;

    public ModelOutput(String name, StructuralErrorModel structuralErrorModel) {
        this.name = name;
        this.structuralErrorModel = structuralErrorModel;
    }

    public void toFiles(String workspace) {
        this.structuralErrorModel.toFiles(workspace);
    }

    @Override
    public String toString() {

        String str = String.format("Model output %s associated with the following structural error model.", this.name);
        str = str + "\n" + this.structuralErrorModel.toString();
        return str;
    }
}
