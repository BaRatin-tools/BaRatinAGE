package bam;

// import bam.exe.ConfigFile;

public class ModelOutput {
    private String name;
    private StructuralErrorModel structuralErrorModel;

    public ModelOutput(String name, StructuralErrorModel structuralErrorModel) {
        this.name = name;
        this.structuralErrorModel = structuralErrorModel;
    }

    public String getStructErrConfName() {
        return this.structuralErrorModel.getConfigFileName();
    }

    public void writeConfig(String workspace) {
        this.structuralErrorModel.writeConfig(workspace);
    }

    public void log() {
        System.out.println(
                String.format("Model output %s associated with the following structural error model.", this.name));
        this.structuralErrorModel.log();
    }
}
