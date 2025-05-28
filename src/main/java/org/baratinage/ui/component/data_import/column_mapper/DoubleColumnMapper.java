package org.baratinage.ui.component.data_import.column_mapper;

import java.util.HashSet;
import java.util.Set;

public class DoubleColumnMapper extends SingleColumnMapper<Double> {

  private static Double toDouble(String v) {
    try {
      Double d = Double.parseDouble(v);
      return d == null ? Double.NaN : d;
    } catch (Exception e) {
      return Double.NaN;
    }
  }

  @Override
  protected void updateState() {
    String[] unparsed = getUnparsedColumn();
    if (unparsed == null) {
      parsed = null;
      return;
    }
    parsed = new double[unparsed.length];
    missingValuesIndices.clear();
    invalidValuesIndices.clear();
    for (int k = 0; k < unparsed.length; k++) {
      if (unparsed[k].equals(mv)) {
        missingValuesIndices.add(k);
        parsed[k] = Double.NaN;
      } else {
        Double d = toDouble(unparsed[k]);
        if (d.isNaN()) {
          invalidValuesIndices.add(k);
          parsed[k] = Double.NaN;
        } else {
          parsed[k] = d;
        }
      }
    }
  }

  private double[] parsed = null;
  HashSet<Integer> missingValuesIndices = new HashSet<>();
  HashSet<Integer> invalidValuesIndices = new HashSet<>();

  public double[] getParsedColumn() {
    return parsed;
  }

  @Override
  protected Set<Integer> getMissingIndices() {
    return missingValuesIndices;
  }

  @Override
  public Set<Integer> getInvalidIndices() {
    return invalidValuesIndices;
  }

}
