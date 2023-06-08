package org.baratinage.ui.baratin;

import javax.swing.JLabel;

import org.baratinage.jbam.Parameter;
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
import org.json.JSONArray;
import org.json.JSONObject;

public class StructuralError extends BaRatinItem implements IStructuralError, BamItemList.BamItemListChangeListener {

    static private final String defaultNameTemplate = "Modèle d'erreur structurelle #%s";
    static private int nInstance = 0;

    public static final int TYPE = (int) Math.floor(Math.random() * Integer.MAX_VALUE);

    private String currentModelType;
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
        modelTypeRadioButtons.addOnChangeAction((newModelType) -> {
            updateModelType(newModelType);
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

        modelTypeRadioButtons.setSelectedValue("linear");
        updateModelType("linear");
    }

    private void updateModelType(String newModelType) {
        if (newModelType.equals("linear")) {
            currentModelType = newModelType;
            currentStructuralErrorModel = new LinearStructuralErrorModel();
            currentStructuralErrorModel.applyDefaultConfig();
            modelParametersPanel.clear();
            modelParametersPanel.appendChild(currentStructuralErrorModel);
        } else if (newModelType.equals("constant")) {
            currentStructuralErrorModel = new ConstantStructuralErrorModel();
            currentStructuralErrorModel.applyDefaultConfig();
            modelParametersPanel.clear();
            modelParametersPanel.appendChild(currentStructuralErrorModel);
        }
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
        JSONObject json = new JSONObject();
        json.put("name", getName());
        json.put("description", getDescription());
        json.put("modelType", currentModelType);

        StructuralErrorModel sem = currentStructuralErrorModel.getStructuralErrorModel();
        JSONArray structErrModelParsJson = new JSONArray();
        for (Parameter p : sem.getParameters()) {
            JSONObject pJson = new JSONObject();
            pJson.put("distrib", p.getDistribution().getDistrib().name);
            pJson.put("initialGuess", p.getInitalGuess());
            JSONArray pPriorsJson = new JSONArray();
            for (double prior : p.getDistribution().getParameterValues()) {
                pPriorsJson.put(prior);
            }
            pJson.put("priors", pPriorsJson);
            structErrModelParsJson.put(pJson);
        }
        json.put("modelPriors", structErrModelParsJson);

        return json;
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
