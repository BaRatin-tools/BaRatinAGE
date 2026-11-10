package org.baratinage.ui.baratin.baratin_spd;

import org.baratinage.utils.Misc;
import org.json.JSONObject;

public class PeriodSet {

  // FIXME: this class should support period date/time transitions instead

  public final String id;
  public String name;
  public int nPeriods;

  public PeriodSet(String name, int nPeriods) {
    this(Misc.getId(), name, nPeriods);
  }

  private PeriodSet(String id, String name, int nPeriods) {
    this.id = id;
    this.name = name;
    this.nPeriods = nPeriods;
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

}
