package org.baratinage.ui.baratin.gaugings;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.text.DefaultFormatterFactory;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.MsgPanel;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleCheckbox;
import org.baratinage.ui.component.SimpleDateTimeField;
import org.baratinage.ui.component.SimpleNumberField;
import org.baratinage.ui.container.SimpleFlowPanel;

public class GaugingEditor extends SimpleFlowPanel {

  private GaugingsDataset dataset = null;
  private final List<Integer> selectedIndices = new ArrayList<>();

  private final JButton newGaugingButton;
  private final JButton deleteGaugingsButton;

  private final SimpleDateTimeField dateTimeField;
  private final SimpleNumberField stageField;
  private final SimpleNumberField stageUncertaintyField;
  private final SimpleNumberField dischargeField;
  private final SimpleNumberField dischargeUncertaintyField;
  private final SimpleCheckbox stateField;

  private final JLabel dateTimeLabel = new JLabel();
  private final JLabel stageLabel = new JLabel();
  private final JLabel stageUncertaintyLabel = new JLabel();
  private final JLabel dischargeLabel = new JLabel();
  private final JLabel dischargeUncertaintyLabel = new JLabel();

  private final MsgPanel multiSelectionWarningMsg = new MsgPanel(MsgPanel.TYPE.WARNING, true);

  // controls reactivity to avoid infinite callbacks loop
  // and other exeptions
  boolean blockUpdatingFields = false;
  boolean blockFieldListeners = false;

  public GaugingEditor() {
    super(true);
    setGap(5);
    setPadding(5);

    newGaugingButton = new JButton();
    newGaugingButton.addActionListener(l -> {
      addGauging();
    });
    deleteGaugingsButton = new JButton();
    deleteGaugingsButton.addActionListener(l -> {
      deleteSelectedGaugings();
    });
    deleteGaugingsButton.setForeground(AppSetup.COLORS.DANGER);
    deleteGaugingsButton.setEnabled(false);

    dateTimeField = new SimpleDateTimeField();
    stageField = new SimpleNumberField() {
      @Override
      public boolean isValueValid() {
        return true;
      }
    };
    stageUncertaintyField = new SimpleNumberField() {
      @Override
      public boolean isValueValid() {
        return true;
      }
    };
    dischargeField = new SimpleNumberField() {
      @Override
      public boolean isValueValid() {
        return true;
      }
    };
    dischargeUncertaintyField = new SimpleNumberField() {
      @Override
      public boolean isValueValid() {
        return true;
      }
    };
    stateField = new SimpleCheckbox();

    DefaultFormatterFactory emptyValueFormatterFactory = new DefaultFormatterFactory(new AbstractFormatter() {
      @Override
      public Object stringToValue(String text) throws ParseException {
        if ("-".equals(text) || text == null || text.trim().isEmpty()) {
          return null;
        }
        return text;
      }

      @Override
      public String valueToString(Object value) throws ParseException {
        return (value == null) ? "-" : value.toString();
      }
    });

    stageField.setFormatterFactory(emptyValueFormatterFactory);
    stageUncertaintyField.setFormatterFactory(emptyValueFormatterFactory);
    dischargeField.setFormatterFactory(emptyValueFormatterFactory);
    dischargeUncertaintyField.setFormatterFactory(emptyValueFormatterFactory);

    dateTimeField.addChangeListener((e) -> handleFieldChange());
    stageField.addChangeListener((e) -> handleFieldChange());
    stageUncertaintyField.addChangeListener((e) -> handleFieldChange());
    dischargeField.addChangeListener((e) -> handleFieldChange());
    dischargeUncertaintyField.addChangeListener((e) -> handleFieldChange());
    stateField.addItemListener((e) -> handleFieldChange());

    T.t(this, newGaugingButton, false, "new_gauging");
    T.t(this, deleteGaugingsButton, false, "delete_gaugings");
    T.t(this, dateTimeLabel, false, "date_time");
    T.t(this, stageLabel, false, "stage");
    T.t(this, stageUncertaintyLabel, false, "stage_uncertainty");
    T.t(this, dischargeLabel, false, "discharge");
    T.t(this, stateField, false, "active_gauging");

    T.t(this, dischargeUncertaintyLabel, false, "discharge_uncertainty_percent");
    T.t(this, multiSelectionWarningMsg.message, false, "changes_will_affect_all_selected_gaugings_warning");
  }

  private void handleFieldChange() {
    if (blockFieldListeners) {
      return;
    }
    blockUpdatingFields = true;
    updateSelectedGaugings();
    blockUpdatingFields = false;
  }

  public void setSelectedIndices(int[] indices) {
    selectedIndices.clear();
    for (int i : indices) {
      selectedIndices.add(i);
    }
    blockFieldListeners = true;
    updateFieldFromGaugings();
    blockFieldListeners = false;
    deleteGaugingsButton.setEnabled(selectedIndices.size() > 0);
  }

  private void addGauging() {
    if (dataset == null) {
      return;
    }
    GaugingData newGauging = new GaugingData();
    newGauging.dataTime = LocalDateTime.now();
    newGauging.stage = 0.0;
    newGauging.discharge = 0.0;
    newGauging.stageUncertainty = 0.0;
    newGauging.dischargeUncertainty = 0.0;
    newGauging.isActive = false;

    dataset.addGauging(newGauging);
  }

  private void deleteSelectedGaugings() {
    if (dataset == null) {
      return;
    }

    if (selectedIndices.size() == dataset.getNumberOfRows()) {
      CommonDialog.errorDialog(T.html("cannot_remove_all_gaugings"));
      return;
    }

    boolean confirmed = CommonDialog.confirmDialog(T.html("confirm_delete_gauging"));

    if (confirmed) {
      dataset.deleteGaugings(selectedIndices);
      selectedIndices.clear();
    }
  }

  public void setDataset(GaugingsDataset dataset) {
    this.dataset = dataset;
    multiSelectionWarningMsg.setVisible(false);
    rebuidUI();
  }

  private void rebuidUI() {
    removeAll();
    if (this.dataset == null) {
      return;
    }

    addChild(multiSelectionWarningMsg, 0);

    SimpleFlowPanel btnsPanel = new SimpleFlowPanel(true);
    btnsPanel.setGap(5);
    btnsPanel.addChild(deleteGaugingsButton, 0);
    btnsPanel.addChild(newGaugingButton, 0);
    addChild(btnsPanel, 0);

    if (this.dataset.getDateTime() != null) {
      addChild(dateTimeLabel, 0);
      addChild(dateTimeField, 0);
    }
    addChild(stageLabel, 0);
    addChild(stageField, 0);
    if (this.dataset.getStageStdUncertainty() != null) {
      addChild(stageUncertaintyLabel, 0);
      addChild(stageUncertaintyField, 0);
    }
    addChild(dischargeLabel, 0);
    addChild(dischargeField, 0);
    addChild(dischargeUncertaintyLabel, 0);
    addChild(dischargeUncertaintyField, 0);
    addChild(stateField, 0);
  }

  private void updateSelectedGaugings() {
    LocalDateTime ldt = dateTimeField.getDateTime();
    Double h = stageField.getDoubleValue();
    Double uh = stageUncertaintyField.getDoubleValue();
    Double Q = dischargeField.getDoubleValue();
    Double uQ = dischargeUncertaintyField.getDoubleValue();
    Boolean active = stateField.isSelected();
    if (stateField.isIndeterminate()) {
      active = null;
    }
    for (int k = 0; k < selectedIndices.size(); k++) {
      int index = selectedIndices.get(k);
      GaugingData g = dataset.getGauging(index);
      g = updateGauging(g, ldt, h, uh, Q, uQ, active);
      dataset.updateGauging(index, g);
      // table.updateGauging(index, g);
    }
  }

  private static GaugingData updateGauging(GaugingData g,
      LocalDateTime ldt,
      Double h,
      Double uh,
      Double Q,
      Double uQ,
      Boolean active) {
    if (ldt != null) {
      g.dataTime = ldt;
    }
    if (h != null) {
      g.stage = h;
    }
    if (uh != null) {
      g.stageUncertainty = uh;
    }
    if (Q != null) {
      g.discharge = Q;
    }
    if (uQ != null) {
      g.dischargeUncertainty = uQ;
    }
    if (active != null) {
      g.isActive = active;
    }
    return g;
  }

  public void updateFieldFromGaugings() {

    if (blockUpdatingFields) {
      return;
    }

    if (dataset == null || selectedIndices.size() == 0) {
      return;
    }

    multiSelectionWarningMsg.setVisible(false);
    GaugingData mainGauging = dataset.getGauging(selectedIndices.get(0));

    if (mainGauging.dataTime != null) {
      dateTimeField.setDateTime(mainGauging.dataTime);
    }
    stageField.setValue(mainGauging.stage);
    dischargeField.setValue(mainGauging.discharge);
    dischargeUncertaintyField.setValue(mainGauging.dischargeUncertainty);
    if (mainGauging.stageUncertainty != null) {
      stageUncertaintyField.setValue(mainGauging.stageUncertainty);
    }
    stateField.setSelected(mainGauging.isActive);
    stateField.setIndeterminate(false);

    if (selectedIndices.size() > 1) {
      multiSelectionWarningMsg.setVisible(true);
      for (int k = 1; k < selectedIndices.size(); k++) {
        GaugingData g = dataset.getGauging(selectedIndices.get(k));
        if (g.dataTime != null
            && mainGauging.dataTime != null
            && !g.dataTime.equals(mainGauging.dataTime)) {
          dateTimeField.clearDateTime();
        }
        if (!g.stage.equals(mainGauging.stage)) {
          stageField.unsetValue();
        }
        if (g.stageUncertainty != null
            && mainGauging.stageUncertainty != null
            && !g.stageUncertainty.equals(mainGauging.stageUncertainty)) {
          stageUncertaintyField.unsetValue();
        }
        if (!g.discharge.equals(mainGauging.discharge)) {
          dischargeField.unsetValue();
        }
        if (!g.dischargeUncertainty.equals(mainGauging.dischargeUncertainty)) {
          dischargeUncertaintyField.unsetValue();
        }
        if (!g.isActive.equals(mainGauging.isActive)) {
          stateField.setIndeterminate(true);
        }
      }
    }
  }

}
