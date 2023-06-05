package org.baratinage.ui.component;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.baratinage.ui.container.RowColPanel;

public class RadioButtons extends RowColPanel {

    private RadioButton[] radioButtons;

    public void setOptions(RadioButton[] radioButtons) {
        this.radioButtons = radioButtons;
        ButtonGroup grp = new ButtonGroup();
        clear();
        for (RadioButton btn : radioButtons) {
            btn.addActionListener((e) -> {
                fireOnChangeAction();
            });
            grp.add(btn);
            appendChild(btn);
        }
    }

    public void setSelectedValue(String value) {
        for (RadioButton btn : radioButtons) {
            if (btn.getValue().equals(value)) {
                btn.setSelected(true);
                return;
            }
        }
    }

    public String getSelectedValue() {
        for (RadioButton btn : radioButtons) {
            if (btn.isSelected()) {
                return btn.getValue();
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface IChangeAction {
        public void onChange(String newValue);
    }

    private List<IChangeAction> onChangeActions = new ArrayList<>();

    public void addOnChangeAction(IChangeAction l) {
        onChangeActions.add(l);
    }

    public void removeOnChangeAction(IChangeAction l) {
        onChangeActions.remove(l);
    }

    public void fireOnChangeAction() {
        String currentValue = getSelectedValue();
        if (currentValue == null) {
            return;
        }
        for (IChangeAction l : onChangeActions) {
            l.onChange(currentValue);
        }
    }

    static public class RadioButton extends JRadioButton {
        private String value;

        public RadioButton(String label, String value) {
            super(label);
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
