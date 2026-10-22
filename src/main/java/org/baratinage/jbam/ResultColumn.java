package org.baratinage.jbam;

import java.util.List;

import org.baratinage.utils.Calc;
import org.baratinage.utils.ParameterSymbol;

public class ResultColumn {
  public final KIND kind;
  public final String header;
  public final double[] samples;
  public final int maxpostIndex;
  public final double[] statistics; // usefull? Dedicated class
  public final ParameterSymbol symbol;

  private List<double[]> density = null;
  private double[] u95 = null;
  private Float validity = null;

  public final Parameter parameter; // null: derived parameter or LogPost
  // public final VariableParameter parent; // null: not a variable parameter TODO

  final private static String[] SUMMARY_STATISTICS_NAMES = new String[] {
      "n", "min", "max", "range", "mean", "mediann", "q10", "q25",
      "q75", "q90", "std", "var", "cv", "skewness", "kurtosis", "maxpost"
  };

  public static enum KIND {
    LOGPOST,
    ESTIMATED,
    GAMMA,
    DERIVED,
    SYNTHETIC
  };

  public ResultColumn(
      KIND kind,
      String header,
      double[] samples,
      double[] statistics,
      int maxpostIndex,
      Parameter parameter) {
    this(kind,
        header,
        samples,
        statistics,
        maxpostIndex,
        parameter,
        ParameterSymbol.guess(header));
  }

  public ResultColumn(
      KIND kind,
      String header,
      double[] samples,
      double[] statistics,
      int maxpostIndex,
      Parameter parameter,
      ParameterSymbol symbol) {
    this.kind = kind;
    this.header = header;
    this.samples = samples;
    this.statistics = statistics;
    this.maxpostIndex = maxpostIndex;
    this.parameter = parameter;
    this.symbol = symbol;
  }

  public boolean shouldDisplayPrior() {
    if (parameter == null) {
      return false;
    }
    return kind == KIND.ESTIMATED
        && parameter.distribution.type != DistributionType.FIXED
        && parameter.distribution.type != DistributionType.FLAT
        && parameter.distribution.type != DistributionType.FLAT_POSITIVE;
  }

  public Float getValidityCheckEstimate() {
    // 0.5 = valid
    // 0 = underestimated,
    // 1 = overestimated
    if (parameter == null) {
      return null;
    }
    if (validity != null) {
      return validity;
    }
    if (parameter.distribution.type.equals(DistributionType.FIXED)) {
      return 0.5f;
    }

    int n = samples.length;
    double[] priorSamples = parameter.getRandomValues(n);
    if (priorSamples == null) {
      return 0.5f;
    }
    int belowCount = 0;
    for (int k = 0; k < n; k++) {
      if (samples[k] < priorSamples[k]) {
        belowCount++;
      }
    }
    float belowFreq = ((float) belowCount / (float) n);
    validity = 1f - belowFreq;
    return validity;
  }

  public List<double[]> getPriorDensity() {
    if (parameter != null) {
      return parameter.distribution.getDensity();
    }
    return null;
  }

  public List<double[]> getPostDensity() {
    if (density != null) {
      return density;
    }
    double[] sorted = Calc.sort(samples);
    density = Calc.density(sorted, 20);
    return density;
  }

  public double[] get95interval() {
    if (u95 != null) {
      return u95;
    }
    u95 = Calc.percentiles(samples, false, 0.025, 0.975);
    return u95;
  }

  public double getMaxpost() {
    return samples[maxpostIndex];
  }

  public ResultColumn withSymbol(ParameterSymbol newSymbol) {
    return new ResultColumn(kind, header, samples, statistics, maxpostIndex, parameter, newSymbol);
  }

  @Override
  public String toString() {
    String str = String.format("Parameter results '%s' with %d samples",
        this.header, this.samples.length);
    if (this.statistics != null) {
      String[] summaryStr = new String[this.statistics.length];
      for (int k = 0; k < this.statistics.length; k++) {
        summaryStr[k] = String.format("%s: %.2e", SUMMARY_STATISTICS_NAMES[k], this.statistics[k]);
      }
      str += " (" + String.join(", ", summaryStr) + ")";
    }
    return str;
  }
}
