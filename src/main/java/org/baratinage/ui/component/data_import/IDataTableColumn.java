package org.baratinage.ui.component.data_import;

public interface IDataTableColumn {

  public boolean isMissing(int colIndex, int rowIndex);

  public boolean isInvalid(int colIndex, int rowIndex);

  public String getLabelText(int colIndex, int rowIndex);

  public String getSecondaryLabelText(int colIndex, int rowIndex);

}
