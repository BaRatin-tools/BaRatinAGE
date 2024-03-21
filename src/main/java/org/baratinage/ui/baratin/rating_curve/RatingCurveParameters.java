package org.baratinage.ui.baratin.rating_curve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.ui.commons.BamEstimatedParameter;
import org.baratinage.utils.ConsoleLogger;

public class RatingCurveParameters {

    public final List<BamEstimatedParameter> kacbParameters;
    public final List<BamEstimatedParameter> gammas;
    public final BamEstimatedParameter logPostParameter;

    public RatingCurveParameters(List<EstimatedParameter> parameters) {

        List<BamEstimatedParameter> processedParameters = new ArrayList<>();
        for (EstimatedParameter p : parameters) {
            BamEstimatedParameter bp = processParameter(p);
            processedParameters.add(bp);
        }

        logPostParameter = processedParameters
                .stream().filter(bep -> bep.type.equals("LogPost"))
                .findFirst()
                .orElse(null);

        gammas = processedParameters
                .stream()
                .filter(bep -> bep.isGammaParameter)
                .sorted(
                        Comparator
                                .<BamEstimatedParameter>comparingInt(bep -> bep.index)
                                .thenComparing(bep -> bep.type))
                .collect(Collectors.toList());

        Map<String, Integer> controlParameterOrder = Map.of("k", 0, "a", 1, "c", 2, "b", 3);
        kacbParameters = processedParameters
                .stream()
                .filter(bep -> (bep.isEstimatedParameter || bep.isDerivedParameter) && !bep.isGammaParameter)
                .filter(bep -> controlParameterOrder.keySet().contains(bep.type))
                .sorted(
                        Comparator
                                .<BamEstimatedParameter>comparingInt(bep -> bep.index)
                                .thenComparing(bep -> controlParameterOrder.get(bep.type)))

                .collect(Collectors.toList());

    }

    public List<double[]> getActivationStagesMPAndEnv() {
        return kacbParameters
                .stream()
                .filter(bep -> bep.shortName.startsWith("k"))
                .map(bep -> {
                    double[] u95 = bep.get95interval();
                    double mp = bep.getMaxpost();
                    return new double[] { mp, u95[0], u95[1] };
                }).collect(Collectors.toList());
    }

    private static record NamesAndIndex(String name, int index) {
    };

    private static Pattern gammaNamePattern = Pattern.compile("Y\\d+_gamma_(\\d+)");
    private static Pattern compParNamePattern = Pattern.compile("([a-zA-Z]+)(\\d+)");
    private static Pattern confParNamePattern = Pattern.compile("([a-zA-Z]+)_(\\d+)");

    private static NamesAndIndex getNamesIndex(Pattern pattern, String rawName, String defaultName,
            boolean incrementIndex) {
        Matcher matcher = pattern.matcher(rawName);
        if (matcher.matches()) {
            String name = defaultName;
            int index = -1;
            try {
                if (matcher.groupCount() == 2) {
                    name = matcher.group(1);
                    index = Integer.parseInt(matcher.group(2));
                } else if (matcher.groupCount() == 1) {
                    index = Integer.parseInt(matcher.group(1));
                }
            } catch (Exception e) {
                ConsoleLogger.error(e);
            }
            if (incrementIndex && index >= 0) {
                index++;
            }
            return new NamesAndIndex(name, index);
        } else {
            return new NamesAndIndex("", -1);
        }
    }

    private static BamEstimatedParameter processParameter(EstimatedParameter parameter) {
        if (parameter.name.equals("LogPost")) {
            return new BamEstimatedParameter(parameter, "LogPost", "LogPost",
                    false, false, false,
                    "LogPost", -1);
        }
        if (parameter.name.startsWith("Y") && parameter.name.contains("gamma")) {
            NamesAndIndex namesAndIndex = getNamesIndex(gammaNamePattern, parameter.name, "&gamma;", true);
            String niceName = String.format(
                    "<html>%s<sub>%d</sub></html>",
                    namesAndIndex.name,
                    namesAndIndex.index);
            String shortName = namesAndIndex.name + "_" + namesAndIndex.index;
            return new BamEstimatedParameter(parameter, shortName, niceName,
                    true, false, true,
                    "&gamma;", namesAndIndex.index);
        }
        if (parameter.name.contains("_")) {
            NamesAndIndex namesAndIndex = getNamesIndex(confParNamePattern, parameter.name, "", true);
            String niceName = String.format(
                    "<html>%s<sub>%d</sub></html>",
                    namesAndIndex.name,
                    namesAndIndex.index);
            String shortName = namesAndIndex.name + "_" + namesAndIndex.index;
            return new BamEstimatedParameter(parameter, shortName, niceName,
                    true, false, false,
                    namesAndIndex.name, namesAndIndex.index);
        } else {
            NamesAndIndex namesAndIndex = getNamesIndex(compParNamePattern, parameter.name, "", false);
            String niceName = String.format("<html>%s<sub>%d</sub></html>",
                    namesAndIndex.name,
                    namesAndIndex.index);
            String shortName = namesAndIndex.name + "_" + namesAndIndex.index;
            return new BamEstimatedParameter(parameter, shortName, niceName,
                    false, true, false,
                    namesAndIndex.name, namesAndIndex.index);
        }
    }
}
