package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.jbam.Parameter;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.AbstractParameterPriorDist;
import org.baratinage.ui.component.EquationLabel;
import org.baratinage.ui.component.SimpleSep;
import org.baratinage.ui.container.SimpleGridPanel;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class PriorControlPanel extends SimpleGridPanel implements ChangeListener {

  protected final JLabel equationLabel;

  private final List<JLabel> columnHeaders;
  public final List<AbstractParameterPriorDist> parameters = new ArrayList<>();

  // FIXME: the number of column should be fixed (7 or 5, to match addToGrid)
  // FIXME: the number of rows should not be needed
  public PriorControlPanel(int nColumns, int nRows, String equation) {
    setColumns(nColumns + 4);
    setColumn(3, SimpleGridPanel.grow());
    setColumn(4, SimpleGridPanel.grow());

    setPadding(0);
    setGaps(5, 5);

    equationLabel = new EquationLabel(equation);
    add(equationLabel, SimpleGridPanel
        .cell(0, 0)
        .span(nColumns + 4, 1)
        .align(SimpleGridPanel.Align.CENTER, SimpleGridPanel.Align.CENTER));

    // to fill the empty space;

    add(new SimpleSep(), SimpleGridPanel.cell(3 + nColumns, 2));

    columnHeaders = new ArrayList<>();
    for (int k = 0; k < nColumns; k++) {
      JLabel label = new JLabel("column #" + k + 1);
      columnHeaders.add(label);
      add(label, SimpleGridPanel.cell(3 + k, 1));
      add(new SimpleSep(), SimpleGridPanel.cell(3 + k, 2));
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

  protected void addParameter(AbstractParameterPriorDist parameter) {
    parameter.addChangeListener(this);
    parameter.addLayoutChangeListener(l -> display());
    parameters.add(parameter);
    T.updateHierarchy(this, parameter); // CHECK: memory leak?
  }

  public void display() {
    display(parameters);
  }

  public void display(AbstractParameterPriorDist... parameters) {
    List<AbstractParameterPriorDist> pars = List.of(parameters);
    display(pars);
  }

  public void display(List<AbstractParameterPriorDist> parameters) {
    removeRows(3, getRowCount());
    int firstRow = 3;
    for (AbstractParameterPriorDist par : parameters) {
      firstRow = par.addToGrid(this, firstRow);
    }
    revalidate();
    repaint();
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

  public JSONObject toJSON() {
    JSONArray pars = new JSONArray();
    int n = parameters.size();
    for (int k = 0; k < n; k++) {
      pars.put(k, parameters.get(k).toJSON());
    }
    JSONObject json = new JSONObject();
    json.put("parameters", pars);
    return json;
  }

  public void fromJSON(JSONObject json) {
    JSONArray pars = json.getJSONArray("parameters");
    fromJSON(pars);
  }

  public void fromJSON(JSONArray pars) {
    int n = parameters.size();
    for (int k = 0; k < n; k++) {
      JSONObject obj = pars.optJSONObject(k);
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

}