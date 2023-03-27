package org.baratinage.ui.bam;

import org.baratinage.jbam.McmcConfig;
import org.baratinage.jbam.McmcCookingConfig;

public interface IMcmc {
    public McmcConfig getMcmcConfig();

    public McmcCookingConfig getMcmcCookingConfig();

}