package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.AppSetup;
import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.AbstractParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDist;
import org.baratinage.ui.commons.ParameterPriorDistSimplified;
import org.baratinage.ui.component.EquationLabel;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.SimpleGridPanel;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class PriorControlPanel extends SimpleGridPanel implements ChangeListener {

  protected record KBACGaussianConfig(
      Double kbMean, Double kbStd,
      Double aMean, Double aStd,
      Double cMean, Double cStd) {
  };

  private final JLabel equationLabel;
  protected final JLabel lockLabel;

  private final List<JLabel> columnHeaders;
  public final List<AbstractParameterPriorDist> parameters = new ArrayList<>();

  public PriorControlPanel(int nColumns, int nRows, String equation) {
    setColumns(nColumns + 4);
    setColumn(3, SimpleGridPanel.grow());
    setColumn(4, SimpleGridPanel.grow());

    setPadding(0);
    setGaps(5, 5);

    equationLabel = new EquationLabel(equation);
    add(equationLabel, SimpleGridPanel.cell(0, 0).span(3, 1));
    add(new SimpleSep(), SimpleGridPanel.cell(0, 1).span(3, 1));

    lockLabel = new JLabel();
    lockLabel.setIcon(AppSetup.ICONS.LOCK);
    add(lockLabel, SimpleGridPanel.cell(3 + nColumns, 0));
    add(new SimpleSep(), SimpleGridPanel.cell(3 + nColumns, 1));

    columnHeaders = new ArrayList<>();
    for (int k = 0; k < nColumns; k++) {
      JLabel label = new JLabel("column #" + k + 1);
      columnHeaders.add(label);
      add(label, SimpleGridPanel.cell(3 + k, 0));
      add(new SimpleSep(), SimpleGridPanel.cell(3 + k, 1));
    }

  }

  protected void setHeaders(String... headers) {
    int n = headers.length;
    if (n != columnHeaders.size()) {
      throw new IllegalArgumentException(
          "Number of provided headers doesn't match the number of needed headers!");
    }
    for (int k = 0; k < n; k++) {
      columnHeaders.get(k).setText(headers[k]);
    }
    revalidate();
  }

  protected void addParameter(ParameterPriorDistSimplified parameter) {
    parameter.meanValueField.addChangeListener(this);
    parameter.uncertaintyValueField.addChangeListener(this);
    parameters.add(parameter);
    T.updateHierarchy(this, parameter);
  }

  protected void addParameter(ParameterPriorDist parameter) {
    parameter.initialGuessField.addChangeListener(this);
    parameter.distributionField.distributionCombobox.addChangeListener(this);
    parameters.add(parameter);
    T.updateHierarchy(this, parameter);
  }

  public void display() {
    display(parameters);
  }

  public void display(AbstractParameterPriorDist... parameters) {
    List<AbstractParameterPriorDist> pars = List.of(parameters);
    display(pars);
  }

  public void display(List<AbstractParameterPriorDist> parameters) {
    removeRows(2, getRowCount());
    for (AbstractParameterPriorDist par : parameters) {
      if (par instanceof ParameterPriorDistSimplified p) {
        addParameterRow(p);
      } else if (par instanceof ParameterPriorDist p) {
        addParameterRow(p);
      }
    }

    revalidate();
    repaint();
  }

  private void addParameterRow(ParameterPriorDistSimplified parameter) {
    add(parameter.iconLabel);
    add(parameter.nameLabel);
    add(parameter.symbolUnitLabel);
    add(parameter.meanValueField);
    add(parameter.uncertaintyValueField);
    add(parameter.lockCheckbox);
  }

  private void addParameterRow(ParameterPriorDist parameter) {
    add(parameter.iconLabel);
    add(parameter.nameLabel);
    add(parameter.symbolUnitLabel);
    add(parameter.distributionField.distributionCombobox);
    add(parameter.distributionField.parameterFieldsPanel);
    add(parameter.initialGuessField);
    add(parameter.menuButton);
  }

  public Parameter[] getParameters() {
    int n = parameters.size();
    Parameter[] pars = new Parameter[n];
    for (int k = 0; k < n; k++) {
      Parameter par = parameters.get(k).getParameter();
      if (par == null) {
        return null;
      }
      pars[k] = par;
    }
    return pars;
  }

  public void setGlobalLock(boolean lock) {
    for (AbstractParameterPriorDist p : parameters) {
      p.setEnabled(!lock);
    }
  }

  public JSONArray toJSON() {
    JSONArray json = new JSONArray();
    int n = parameters.size();
    for (int k = 0; k < n; k++) {
      json.put(k, parameters.get(k).toJSON());
    }
    return json;
  }

  public void fromJSON(JSONArray json) {
    int n = parameters.size();
    for (int k = 0; k < n; k++) {
      JSONObject obj = json.optJSONObject(k);
      if (obj != null) {
        parameters.get(k).fromJSON(obj);
      }
    }
  }

  private final List<ChangeListener> changeListeners = new ArrayList<>();

  public void addChangeListener(ChangeListener l) {
    changeListeners.add(l);
  }

  public void removeChangeListener(ChangeListener l) {
    changeListeners.remove(l);
  }

  protected void fireChangeListeners() {
    for (ChangeListener l : changeListeners) {
      l.stateChanged(new ChangeEvent(this));
    }
  }

  @Override
  public void stateChanged(ChangeEvent chEvt) {
    fireChangeListeners();
  }

  public abstract KBACGaussianConfig toKACGaussianConfig();

}