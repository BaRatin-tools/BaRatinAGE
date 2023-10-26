package org.baratinage.ui.baratin;

import org.baratinage.jbam.EstimatedParameter;

public record EstimatedControlParameters(
        EstimatedParameter k,
        EstimatedParameter a,
        EstimatedParameter c,
        EstimatedParameter b) {

}
