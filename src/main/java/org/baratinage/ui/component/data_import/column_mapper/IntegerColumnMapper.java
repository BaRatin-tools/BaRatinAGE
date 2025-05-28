package org.baratinage.ui.component.data_import.column_mapper;

import java.util.HashSet;
import java.util.Set;

import org.baratinage.AppSetup;

public class IntegerColumnMapper extends SingleColumnMapper<Integer> {

  private static Integer toInteger(String v) {
    try {
      Integer i = Integer.parseInt(v);
      return i == null ? AppSetup.CONFIG.INT_MISSING_VALUE : i;
    } catch (Exception e) {
      return AppSetup.CONFIG.INT_MISSING_VALUE;
    }
  }

  @Override
  protected void updateState() {
    String[] unparsed = getUnparsedColumn();
    if (unparsed == null) {
      parsed = null;
      return;
    }
    parsed = new int[unparsed.length];
    missingValuesIndices.clear();
    invalidValuesIndices.clear();
    for (int k = 0; k < unparsed.length; k++) {
      if (unparsed[k].equals(mv)) {
        missingValuesIndices.add(k);
        parsed[k] = AppSetup.CONFIG.INT_MISSING_VALUE;
      } else {
        Integer i = toInteger(unparsed[k]);
        if (i.equals(AppSetup.CONFIG.INT_MISSING_VALUE)) {
          invalidValuesIndices.add(k);
          parsed[k] = AppSetup.CONFIG.INT_MISSING_VALUE;
        } else {
          parsed[k] = i;
        }
      }
    }
  }

  private int[] parsed = null;
  HashSet<Integer> missingValuesIndices = new HashSet<>();
  HashSet<Integer> invalidValuesIndices = new HashSet<>();

  public int[] getParsedColumn() {
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
