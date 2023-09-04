package org.baratinage.ui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.baratinage.ui.container.RowColPanel;
import org.baratinage.utils.Action;

public class RadioButtons extends RowColPanel implements ActionListener {

    // private List<RadioButton> radioButtons;
    private Map<String, JRadioButton> options = new HashMap<>();
    private ButtonGroup buttonGroup = new ButtonGroup();

    // public RadioButtons() {
    // radioButtons = new ArrayList<>();

    // }

    // public void setOptions(RadioButton[] radioButtons) {
    // // this.radioButtons = radioButtons;
    // ButtonGroup grp = new ButtonGroup();
    // clear();
    // for (RadioButton btn : radioButtons) {
    // btn.addActionListener((e) -> {
    // fireOnChangeAction();
    // });
    // grp.add(btn);
    // appendChild(btn);
    // }
    // }

    public void addOption(String value, JRadioButton button) {
        buttonGroup.add(button);
        options.put(value, button);
        button.addActionListener(this);
    }

    public void removeOption(String value) {
        JRadioButton btn = options.get(value);
        buttonGroup.remove(btn);
        options.remove(value);
        btn.removeActionListener(this);
    }

    public void setSelectedValue(String value) {
        JRadioButton btn = options.get(value);
        if (btn != null) {
            btn.setSelected(true);
        }

    }

    public String getSelectedValue() {
        for (String key : options.keySet()) {
            JRadioButton btn = options.get(key);
            if (btn.isSelected()) {
                return key;
            }
        }
        return null;
    }

    private List<Action> onChangeActions = new ArrayList<>();

    public void addOnChangeAction(Action action) {
        onChangeActions.add(action);
    }

    public void removeOnChangeAction(Action action) {
        onChangeActions.remove(action);
    }

    public void fireOnChangeAction() {
        String currentValue = getSelectedValue();
        if (currentValue == null) {
            return;
        }
        for (Action action : onChangeActions) {
            action.run();
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        fireOnChangeAction();
    }

}
