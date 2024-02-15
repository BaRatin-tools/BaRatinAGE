package org.baratinage.ui.commons;

import org.baratinage.jbam.EstimatedParameter;

public class BamEstimatedParameter extends EstimatedParameter {

    // depending on context short/full name are usefull
    // both default to parameter.name
    public final String shortName;
    public final String fullName;
    // some parameter are part of the estimation
    // others are derived and finally some are
    // related to the structural error models of the
    // different outputs
    public final boolean isEstimatedParameter;
    public final boolean isDerivedParameter;
    public final boolean isGammaParameter;
    // for some parameter, an index and a type are usefull
    public final String type;
    public final int index;

    public BamEstimatedParameter(
            EstimatedParameter parameter,
            String shortName,
            String fullName,
            boolean isEstimatedParameter,
            boolean isDerivedParameter,
            boolean isGammaParameter,
            String type,
            int index) {
        super(
                parameter.name,
                parameter.mcmc,
                parameter.summary,
                parameter.maxpostIndex,
                parameter.parameterConfig);
        this.shortName = shortName;
        this.fullName = fullName;
        this.isEstimatedParameter = isEstimatedParameter;
        this.isDerivedParameter = isDerivedParameter;
        this.isGammaParameter = isGammaParameter;
        this.type = type;
        this.index = index;
    }

}
