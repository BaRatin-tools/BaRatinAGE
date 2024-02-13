package org.baratinage.ui.baratin.baratin_bac;

import javax.swing.JLabel;

import org.baratinage.jbam.Parameter;
import org.baratinage.ui.bam.BamConfig;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.IModelDefinition;
import org.baratinage.ui.bam.IPredictionMaster;
import org.baratinage.ui.bam.IPriors;
import org.baratinage.ui.bam.PredExpSet;
import org.baratinage.ui.baratin.BaratinProject;

public class HydraulicConfigurationBAC extends BamItem
        implements IModelDefinition, IPriors, IPredictionMaster {

    public HydraulicConfigurationBAC(String uuid, BaratinProject project) {
        super(BamItemType.HYDRAULIC_CONFIG_BAC, uuid, project);
        setContent(new JLabel("HydraulicConfigurationBAC"));
    }

    @Override
    public PredExpSet getPredExps() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPredExps'");
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
    public String[] getParameterNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParameterNames'");
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
