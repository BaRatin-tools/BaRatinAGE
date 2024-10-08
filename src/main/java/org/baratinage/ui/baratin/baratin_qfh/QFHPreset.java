package org.baratinage.ui.baratin.baratin_qfh;

import java.util.List;

public record QFHPreset(String id,
        String formula,
        String stageSymbole,
        List<RatingCurveEquationParameter> parameters) {
    public record RatingCurveEquationParameter(String symbole, String type) {
    }
}
