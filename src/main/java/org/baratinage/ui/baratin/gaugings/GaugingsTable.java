package org.baratinage.ui.baratin.gaugings;

import java.time.LocalDateTime;

import javax.swing.JToggleButton;
import javax.swing.event.TableModelEvent;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.DataTable;

public class GaugingsTable extends DataTable {

  private GaugingsDataset dataset = null;

  private boolean hasDateTime = false;
  private boolean hasStageUncertainty = false;

  public final JToggleButton editableGaugingsToggle = new JToggleButton();

  private boolean doNotFireChangeListeners = false;

  public GaugingsTable() {
    editableGaugingsToggle.setIcon(AppSetup.ICONS.EDIT);

    addChangeListener(e -> {
      if (doNotFireChangeListeners) {
        return;
      }
      if (dataset == null) {
        return;
      }
      int columnIndex = e.getColumn();
      int rowIndex = e.getFirstRow();
      Object[] activeGaugingColumn = getColumn(columnIndex);
      if (activeGaugingColumn == null) {
        return;
      }
      Object active = activeGaugingColumn[rowIndex];
      if (active instanceof Boolean) {
        GaugingData g = dataset.getGauging(rowIndex);
        g.isActive = (Boolean) active;
        dataset.updateGauging(rowIndex, g);
      }
    });

    T.t(this, () -> {
      editableGaugingsToggle.setToolTipText(T.text("make_gaugings_editable"));
    });

    toolsPanel.add(editableGaugingsToggle);
  }

  public void updateGauging(int index, GaugingData gauging) {
    doNotFireChangeListeners = true;
    int rowIndex = index;
    int colIndex = 0;
    if (hasDateTime) {
      setValueAt(gauging.dataTime, rowIndex, colIndex);
      colIndex++;
    }
    setValueAt(gauging.stage, rowIndex, colIndex);
    colIndex++;
    if (hasStageUncertainty) {
      setValueAt(gauging.stageUncertainty, rowIndex, colIndex);
      colIndex++;
    }
    setValueAt(gauging.discharge, rowIndex, colIndex);
    colIndex++;
    setValueAt(gauging.dischargeUncertainty, rowIndex, colIndex);
    colIndex++;
    setValueAt(gauging.isActive, rowIndex, colIndex);
    doNotFireChangeListeners = false;
    fireChangeListeners(new TableModelEvent(model, rowIndex));
  }

  private void rebuildTable() {

    if (dataset == null) {
      return;
    }

    double[] stageAbsoluteUncertainty = dataset.getStageAbsoluteUncertainty();
    hasStageUncertainty = stageAbsoluteUncertainty != null;

    clearColumns();
    LocalDateTime[] dateTime = dataset.getDateTime();
    hasDateTime = dateTime != null;
    if (hasDateTime) {
      addColumn(dateTime);
    }
    addColumn(dataset.getStageValues());
    if (hasStageUncertainty) {
      addColumn(stageAbsoluteUncertainty);
    }
    addColumn(dataset.getDischargeValues());
    addColumn(dataset.getDischargePercentUncertainty());
    addColumn(dataset.getStateAsBoolean(), true);

    updateData();

    int i = hasDateTime ? 1 : 0;
    setColumnNumberPrecision(i, 3, true); // stage
    if (hasStageUncertainty) {
      setColumnNumberPrecision(i + 1, 1, false); // stage uncertainty
    }
    i = (hasDateTime ? 1 : 0) + (hasStageUncertainty ? 1 : 0);
    setColumnNumberPrecision(i + 1, 3, false);
    setColumnNumberPrecision(i + 2, 1, false);

    T.clear(table);
    T.t(table, () -> {
      int offset = 0;
      if (dateTime != null) {
        addColumn(dateTime);
        setHeader(0, T.text("date_time"));
        offset++;
      }
      setHeader(0 + offset, T.text("stage"));
      if (hasStageUncertainty) {
        offset++;
        setHeader(0 + offset, T.text("stage_uncertainty_absolute"));
      }
      setHeader(1 + offset, T.text("discharge"));
      setHeader(2 + offset, T.text("discharge_uncertainty_percent"));
      setHeader(3 + offset, T.text("active_gauging"));
      updateHeader();
    });
  }

  public void updateTable(GaugingsDataset gaugingDataset) {
    dataset = gaugingDataset;
    rebuildTable();

    // for (int k = 0; k < gaugingDataset.getNumberOfRows(); k++) {
    // updateGauging(k, gaugingDataset.getGauging(k));
    // }
  }

  public void updateTable() {
    rebuildTable();
  }
}
