package org.baratinage.ui.commons.variable_parameter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;
import org.baratinage.ui.component.SimpleIntegerField;
import org.baratinage.ui.component.SimpleList;
import org.baratinage.ui.component.SimpleTextField;
import org.baratinage.ui.container.SimpleFlowPanel;

public class PeriodSetsPanel extends SimpleFlowPanel implements PeriodSetsSource {

  private static final Icon periodSetIcon = AppSetup.ICONS
      .getCustomAppImageIcon("period_set.svg");

  private final SimpleList<PeriodSet> periodSetList;
  private final SimpleFlowPanel periodSetConfigPanel;

  public PeriodSetsPanel() {

    setGap(5);

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
      periodSetList.addItem(ps.getName(), periodSetIcon, ps);
      periodSetList.setSelected(ps);
      updatePeriodSetLabel(ps);
      fireChangeListeners();
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
    T.updateHierarchy(this, periodSetConfigPanel); // FIXME: should not be needed, see T.clear()
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
        fireChangeListeners();
      }
    });

    nameField.setText(ps.getName());
    nPeriodsField.setValue(ps.getNumberOfPeriods());

    nameField.addChangeListener(e -> {
      ps.setName(nameField.getText());
      updatePeriodSetLabel(ps);
      fireChangeListeners();
    });
    nPeriodsField.addChangeListener(e -> {
      ps.setNumberOfPeriod(nPeriodsField.getIntValue());
      updatePeriodSetLabel(ps);
      fireChangeListeners();
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
    periodSetList.setItemLabel(psIndex, "%s (%d)".formatted(ps.getName(), ps.getNumberOfPeriods()));
  }

  private final List<ChangeListener> changeListeners = new ArrayList<>();

  @Override
  public void addChangeListener(ChangeListener l) {
    changeListeners.add(l);
  }

  @Override
  public void removeChangeListener(ChangeListener l) {
    changeListeners.remove(l);
  }

  protected void fireChangeListeners() {
    for (ChangeListener l : changeListeners) {
      l.stateChanged(new ChangeEvent(this));
    }
  }

  @Override
  public List<PeriodSet> getPeriodSets() {
    return periodSetList.getAllObjects();
  }

}
