package org.baratinage.ui.baratin.baratin_spd;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleIntegerField;
import org.baratinage.ui.component.SimpleList;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.SimpleFlowPanel;

public class PeriodSetsPanel extends SimpleFlowPanel {

  private static final Icon periodSetIcon = AppSetup.ICONS
      .getCustomAppImageIcon("period_set.svg");

  private final SimpleList<PeriodSet> periodSetList;
  private final SimpleFlowPanel periodSetConfigPanel;

  public PeriodSetsPanel() {

    setGap(5);
    // setPadding(5);

    periodSetList = new SimpleList<>();
    periodSetConfigPanel = new SimpleFlowPanel(true);
    periodSetConfigPanel.setGap(5);
    periodSetConfigPanel.setPadding(5);

    periodSetList.addSelectionChangeListeners(e -> updateConfigPanel());

    JButton addBtn = new JButton();
    addBtn.setIcon(AppSetup.ICONS.MORE);

    addBtn.addActionListener(e -> {
      int n = periodSetList.getItemCount();
      PeriodSet ps = new PeriodSet(T.text("period_set_name", n + 1), 2);
      periodSetList.addItem(ps.name, periodSetIcon, ps);
      periodSetList.setSelected(ps);
      updatePeriodSetLabel(ps);
    });

    SimpleFlowPanel left = new SimpleFlowPanel(true);
    left.setGap(5);
    left.addChild(addBtn, false);
    left.addChild(periodSetList, true);

    addChild(left, false);
    addChild(periodSetConfigPanel, true);

    T.updateHierarchy(this, periodSetConfigPanel);
    T.t(this, () -> {
      addBtn.setText(T.html("add"));
    });
  }

  private void updateConfigPanel() {
    PeriodSet ps = periodSetList.getSelectedObject();
    T.clear(periodSetConfigPanel);
    periodSetConfigPanel.removeAll();
    if (ps == null) {
      return;
    }

    SimpleTextField nameField = new SimpleTextField();
    SimpleIntegerField nPeriodsField = new SimpleIntegerField(2, 10000, 1);
    JButton delBtn = new JButton();
    delBtn.setIcon(AppSetup.ICONS.TRASH);

    delBtn.addActionListener(e -> {
      // FIXME: should check if the period set is currently used
      boolean yes = CommonDialog.confirmDialog(T.html("delete_period_set"));
      if (yes) {
        periodSetList.removeItem(ps);
      }

    });

    nameField.setText(ps.name);
    nPeriodsField.setValue(ps.nPeriods);

    nameField.addChangeListener(e -> {
      ps.name = nameField.getText();
      updatePeriodSetLabel(ps);

    });
    nPeriodsField.addChangeListener(e -> {
      ps.nPeriods = nPeriodsField.getIntValue();
      updatePeriodSetLabel(ps);
    });

    JLabel nameLabel = new JLabel();
    JLabel nPeriodsLabel = new JLabel();

    T.t(periodSetConfigPanel, () -> delBtn.setToolTipText(T.text("delete")));
    T.t(periodSetConfigPanel, nameLabel, false, "name");
    T.t(periodSetConfigPanel, nPeriodsLabel, false, "number_of_period");

    periodSetConfigPanel.addChild(nameLabel, false);
    periodSetConfigPanel.addChild(nameField, false);
    periodSetConfigPanel.addChild(nPeriodsLabel, false);
    periodSetConfigPanel.addChild(nPeriodsField, false);
    periodSetConfigPanel.addChild(delBtn, 0, SimpleFlowPanel.ALIGN.END);
  }

  private void updatePeriodSetLabel(PeriodSet ps) {
    int psIndex = periodSetList.getFirstIndex(ps);
    if (psIndex == -1) {
      return;
    }
    periodSetList.setItemLabel(psIndex, "%s (%d)".formatted(ps.name, ps.nPeriods));
  }
}
