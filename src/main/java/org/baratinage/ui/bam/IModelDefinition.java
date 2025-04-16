package org.baratinage.ui.bam;

public interface IModelDefinition {
    public String getModelId();

    public int getNumberOfParameters();

    public String[] getInputNames();

    public String[] getOutputNames();

    // FIXME: weird to specify workspace here... Risks of inconsistencies when
    // FIXME: running BaM afterwards (e.g. in a different workspace)
    public String getXtra(String workspace);
}