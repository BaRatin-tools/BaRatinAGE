package org.baratinage.ui.baratin.hydraulic_control.control_panel;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.baratinage.AppSetup;
import org.baratinage.translation.T;
import org.baratinage.ui.commons.AbstractParameterPriorDist;
import org.baratinage.ui.component.EquationLabel;
import org.baratinage.ui.container.SimpleFlowPanel;
import org.baratinage.ui.container.SimpleGridPanel;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class ChannelPriorControlPanel extends PriorControlPanel {
  private boolean useManningCoefficient = false;

  private final JLabel equationLabelStrickler;
  private final JLabel equationLabelManning;
  private final JButton menuBtn;
  private final JPopupMenu contextMenu;
  private final JCheckBoxMenuItem manningStricklerToggleBtn;

  private boolean doNotUpdateBtn = false;

  public ChannelPriorControlPanel(
      int nColumns,
      int nRows,
      String equationStrickler,
      String equationManning) {
    super(nColumns, nRows, equationStrickler);

    contextMenu = new JPopupMenu();
    manningStricklerToggleBtn = new JCheckBoxMenuItem();
    manningStricklerToggleBtn.addItemListener(l -> {
      if (doNotUpdateBtn) {
        return;
      }
      useManningCoefficient = manningStricklerToggleBtn.isSelected();
      fireChangeListeners();
      display();
    });
    T.t(this, manningStricklerToggleBtn, false, "pref_use_manning_coef");
    contextMenu.add(manningStricklerToggleBtn);

    menuBtn = new JButton();
    menuBtn.setIconTextGap(0);
    menuBtn.setMargin(new Insets(2, 2, 2, 2));
    menuBtn.setIcon(AppSetup.ICONS.CONFIG);
    menuBtn.addActionListener(l -> contextMenu.show(menuBtn, 0, menuBtn.getHeight()));
    equationLabelStrickler = equationLabel;
    equationLabelManning = new EquationLabel(equationManning);

    removeComponent(equationLabel);
    SimpleFlowPanel equationPanel = new SimpleFlowPanel();
    equationPanel.setGap(5);
    equationPanel.addChild(menuBtn, false);
    equationPanel.addChild(equationLabelStrickler, false);
    equationPanel.addChild(equationLabelManning, false);
    add(equationPanel, SimpleGridPanel
        .cell(0, 0)
        .span(nColumns + 4, 1)
        .align(SimpleGridPanel.Align.CENTER, SimpleGridPanel.Align.CENTER));

    setUseManning(AppSetup.CONFIG.USE_MANNING_COEF.get());
  }

  public boolean useManning() {
    return useManningCoefficient;
  }

  @Override
  public void display() {
    // we'll assume rugosity is always in second position
    // with the the stricker parameter at the second position
    // and the manning parameter at the end of the list

    List<AbstractParameterPriorDist> pars = new ArrayList<>();
    pars.addAll(parameters.subList(0, 1));
    if (useManningCoefficient) {
      pars.add(parameters.get(parameters.size() - 1));
    } else {
      pars.add(parameters.get(1));
    }
    pars.addAll(parameters.subList(2, parameters.size() - 1));
    super.display(pars);

    equationLabelManning.setVisible(useManningCoefficient);
    equationLabelStrickler.setVisible(!useManningCoefficient);

  }

  private void setUseManning(boolean useManning) {
    doNotUpdateBtn = true;
    manningStricklerToggleBtn.setSelected(useManning);
    useManningCoefficient = useManning;
    doNotUpdateBtn = false;
  }

  @Override
  public JSONObject toJSON() {
    JSONObject obj = super.toJSON();
    obj.put("use_manning", useManningCoefficient);
    return obj;
  }

  @Override
  public void fromJSON(JSONObject json) {
    if (json.has("use_manning")) {
      setUseManning(json.getBoolean("use_manning"));
    } else {
      setUseManning(false);
    }

    JSONArray pars = json.getJSONArray("parameters");
    super.fromJSON(pars);
    display();
  }

  @Override
  public void fromJSON(JSONArray json) {
    setUseManning(false);
    super.fromJSON(json);
    display();
  }

}