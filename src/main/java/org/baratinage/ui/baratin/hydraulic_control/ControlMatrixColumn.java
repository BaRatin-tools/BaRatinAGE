package org.baratinage.ui.baratin.hydraulic_control;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

public class ControlMatrixColumn implements ItemListener {

    public List<ControlCheckBox> ctrlCheckBoxes;

    @FunctionalInterface
    public interface IControleMatrixColumnHasChanged {
        public void controlMatrixColumnHasChanged();
    }

    private IControleMatrixColumnHasChanged controleMatrixColumnHasChanged;

    public ControlMatrixColumn(IControleMatrixColumnHasChanged controleMatrixColumnHasChanged, int nSegments) {
        this.controleMatrixColumnHasChanged = controleMatrixColumnHasChanged;
        ctrlCheckBoxes = new ArrayList<>();
        for (int k = 0; k < nSegments; k++) {
            ctrlCheckBoxes.add(new ControlCheckBox("", this));
        }
        ctrlCheckBoxes.get(0).setSelected(true);
    }

    public void update(boolean[] selected) {
        int n = selected.length;
        int m = ctrlCheckBoxes.size();
        int d = n - m;
        if (d > 0) {
            // need to add new segments
            for (int k = 0; k < d; k++) {
                ctrlCheckBoxes.add(new ControlCheckBox("", this));
            }
        } else if (d < 0) {
            // need to remove segments
            for (int k = 0; k > d; k--) {
                int index = m + k - 1;
                ControlCheckBox ccb = ctrlCheckBoxes.get(index);
                ccb.destroy();
                ctrlCheckBoxes.remove(index);
            }
        }
        for (int k = 0; k < n; k++) {
            ctrlCheckBoxes.get(k).setSelected(selected[k]);
            // FIXME: check consistency
        }
        updateEditability();
    }

    private void updateEditability() {
        for (int k = 1; k < ctrlCheckBoxes.size(); k++) {
            ControlCheckBox current = ctrlCheckBoxes.get(k);
            ControlCheckBox previous = ctrlCheckBoxes.get(k - 1);
            if (previous.isSelected()) {
                current.setEnabled(true);
                if (current.isSelected()) {
                    previous.setEnabled(false);
                } else {
                    previous.setEnabled(true);
                }
            } else {
                current.setEnabled(false);
            }
        }
        ctrlCheckBoxes.get(0).setEnabled(false);
    }

    public void destroyControl() {
        for (ControlCheckBox ctrlCheckBox : ctrlCheckBoxes) {
            ctrlCheckBox.destroy();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        updateEditability();
        controleMatrixColumnHasChanged.controlMatrixColumnHasChanged();
    }

}
