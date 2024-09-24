package org.baratinage.ui.baratin.baratin_qfh;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPriors;

public class HydraulicConfigurationQFH extends BamItem
        implements IModelDefinition, IPriors {

    public HydraulicConfigurationQFH(String uuid, BamProject project) {
        super(BamItemType.HYDRAULIC_CONFIG_QFH, uuid, project);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Parameter[] getParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParameters'");
    }

    @Override
    public String getModelId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getModelId'");
    }

    @Override
    public int getNumberOfParameters() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberOfParameters'");
    }

    @Override
    public String[] getInputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getInputNames'");
    }

    @Override
    public String[] getOutputNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputNames'");
    }

    @Override
    public String getXtra(String workspace) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getXtra'");
    }

    @Override
    public BamConfig save(boolean writeFiles) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void load(BamConfig config) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'load'");
    }

}
