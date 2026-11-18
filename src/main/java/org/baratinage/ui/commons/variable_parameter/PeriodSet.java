package org.baratinage.ui.commons.variable_parameter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baratinage.utils.Misc;
import org.json.JSONObject;

public class PeriodSet {

  // FIXME: this class should support period date/time transitions instead

  public final String id;
  private String name;
  private int nPeriods;

  public PeriodSet(String name, int nPeriods) {
    this(Misc.getId(), name, nPeriods);
  }

  private PeriodSet(String id, String name, int nPeriods) {
    this.id = id;
    this.name = name;
    this.nPeriods = nPeriods;
  }

  public void setName(String name) {
    this.name = name;
    fireChangeListeners();
  }

  public String getName() {
    return this.name;
  }

  public void setNumberOfPeriod(int n) {
    this.nPeriods = n;
    fireChangeListeners();
  }

  public int getNumberOfPeriods() {
    return this.nPeriods;
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

  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.put("id", id);
    json.put("name", name);
    json.put("nPeriods", nPeriods);
    return json;
  }

  public static PeriodSet fromJSON(JSONObject json) {
    return new PeriodSet(
        json.getString("id"),
        json.getString("name"),
        json.getInt("nPeriods"));
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PeriodSet ps && id.equals(ps.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

}
