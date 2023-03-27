package org.baratinage.ui.bam;

public interface IModelDefinition {
    public String getModelId();

    public String[] getParameterNames();

    public String[] getInputNames();

    public String[] getOutputNames();

    public String getXtra(String workspace);
}