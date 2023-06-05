package org.baratinage.ui.baratin;

import javax.swing.JLabel;

import org.baratinage.jbam.StructuralErrorModel;

import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.IStructuralError;
import org.baratinage.ui.commons.AbstractStructuralErrorModel;
import org.baratinage.ui.commons.ConstantStructuralErrorModel;
import org.baratinage.ui.commons.LinearStructuralErrorModel;
import org.baratinage.ui.component.RadioButtons;
import org.baratinage.ui.component.RadioButtons.RadioButton;
import org.baratinage.ui.container.RowColPanel;
import org.baratinage.ui.lg.Lg;
import org.baratinage.ui.bam.BamItemList;

import org.json.JSONObject;

public class StructuralError extends BaRatinItem implements IStructuralError, BamItemList.BamItemListChangeListener {

    static private final String defaultNameTemplate = "Modèle d'erreur structurelle #%s";
    static private int nInstance = 0;

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    private AbstractStructuralErrorModel currentStructuralErrorModel;
    private RowColPanel modelParametersPanel;

    public StructuralError() {
        super(TYPE);
        nInstance++;
        setName(String.format(defaultNameTemplate, nInstance));

        RowColPanel content = new RowColPanel(AXIS.COL, ALIGN.START);
        content.setPadding(5);

        JLabel headerLabel = new JLabel();
        Lg.registerLabel(headerLabel, "ui", "choose_structural_error_model_type");

        RadioButtons modelTypeRadioButtons = new RadioButtons();
        // if using <sub> html tag, labels are not longer centered correctly.
        // using <sup>&nbsp;</sup> resolves the centering issue.
        String linearLabel = String.format(
                "<html>%s - &Nu;(0, &gamma;<sub>1</sub>+&gamma;<sub>2</sub>Q)<sup>&nbsp;</sup></html>",
                "Linéaire");

        String constantLabel = String.format("<html>%s - &Nu;(0, &gamma;<sub>1</sub><sup>&nbsp;</sup>)</html>",
                "Constant");
        RadioButton[] options = new RadioButton[] {
                new RadioButton(linearLabel, "linear"),
                new RadioButton(constantLabel, "constant"),
        };
        modelTypeRadioButtons.setOptions(options);
        modelTypeRadioButtons.addOnChangeAction((newValue) -> {
            if (newValue == "linear") {
                currentStructuralErrorModel = new LinearStructuralErrorModel();
                currentStructuralErrorModel.applyDefaultConfig();
                modelParametersPanel.clear();
                modelParametersPanel.appendChild(currentStructuralErrorModel);
            } else if (newValue == "constant") {
                currentStructuralErrorModel = new ConstantStructuralErrorModel();
                currentStructuralErrorModel.applyDefaultConfig();
                modelParametersPanel.clear();
                modelParametersPanel.appendChild(currentStructuralErrorModel);
            }
        });

        RowColPanel headerPanel = new RowColPanel();
        headerPanel.setMainAxisAlign(ALIGN.START);
        headerPanel.setGap(5);
        headerPanel.appendChild(headerLabel, 0);
        headerPanel.appendChild(modelTypeRadioButtons, 0);

        modelParametersPanel = new RowColPanel();

        content.appendChild(headerPanel);
        content.appendChild(modelParametersPanel);

        setContent(content);
    }

    @Override
    public StructuralErrorModel getStructuralErrorModel() {
        if (currentStructuralErrorModel == null)
            return null;
        return currentStructuralErrorModel.getStructuralErrorModel();
    }

    @Override
    public void parentHasChanged(BamItem parent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parentHasChanged'");
    }

    @Override
    public JSONObject toJSON() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toJSON'");
    }

    @Override
    public void fromJSON(JSONObject json) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    }

    @Override
    public void onBamItemListChange(BamItemList bamItemList) {
        // System.out.println();
    }

}
