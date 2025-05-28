package org.baratinage.ui.component.data_import.column_mapper;

import java.util.HashSet;
import java.util.Set;

public class StringColumnMapper extends SingleColumnMapper<String> {

  @Override
  protected void updateState() {
    String[] unparsed = getUnparsedColumn();
    if (unparsed == null) {
      return;
    }
    missingValuesIndices.clear();
    invalidValuesIndices.clear();
    for (int k = 0; k < unparsed.length; k++) {
      if (unparsed[k].equals(mv)) {
        missingValuesIndices.add(k);
      }
    }
  }

  HashSet<Integer> missingValuesIndices = new HashSet<>();
  HashSet<Integer> invalidValuesIndices = new HashSet<>();

  @Override
  protected Set<Integer> getMissingIndices() {
    return missingValuesIndices;
  }

  @Override
  public Set<Integer> getInvalidIndices() {
    return invalidValuesIndices;
  }

}
