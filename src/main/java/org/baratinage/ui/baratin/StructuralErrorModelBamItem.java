package org.baratinage.ui.baratin;

import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.jbam.Distribution.DISTRIBUTION;
import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.IStructuralErrorModels;
import org.baratinage.ui.commons.StructuralErrorModelPanel;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONArray;
import org.json.JSONObject;

public class StructuralErrorModelBamItem extends BamItem implements IStructuralErrorModels {

    private final int nOutputs;
    private final String[][] outputSymbolAndUnits;
    private final StructuralErrorModelPanel[] strucErrModelPanels;

    private static final Font MONOSPACE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private static final Font BOLD_SERIF_FONT = new Font(Font.SERIF, Font.BOLD, 14);

    public StructuralErrorModelBamItem(String uuid, BamProject project, String[]... outputSymbolAndUnits) {
        super(BamItemType.STRUCTURAL_ERROR, uuid, project);

        nOutputs = outputSymbolAndUnits.length;

        this.outputSymbolAndUnits = outputSymbolAndUnits;

        strucErrModelPanels = new StructuralErrorModelPanel[nOutputs];

        RowColPanel panel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        panel.setGap(5);
        panel.setPadding(5);

        for (int k = 0; k < nOutputs; k++) {
            if (outputSymbolAndUnits[k].length != 2) {
                throw new IllegalArgumentException(
                        "Each element of 'outputSymbolAndUnits' must be of length 2 (symbol, unit)!");
            }

            String outputSymbol = outputSymbolAndUnits[k][0];
            String outputUnit = outputSymbolAndUnits[k][1];

            JLabel parameterNameLabel = new JLabel();
            parameterNameLabel.setFont(BOLD_SERIF_FONT);
            parameterNameLabel.setText(outputSymbol);

            JLabel infoLabel = new JLabel();
            infoLabel.setFont(MONOSPACE_FONT);
            infoLabel.setText(
                    String.format(
                            "<html>N&sim;(&gamma;<sub>1</sub> + &gamma;<sub>2</sub>%s)</html>",
                            outputSymbol));

            // support only linear model, but with fixced gamma2 set to 0, it is equivalent
            // to the constant model
            StructuralErrorModelPanel strucErrModelPanel = new StructuralErrorModelPanel();
            strucErrModelPanel.addParameter("&gamma;<sub>1</sub>", outputUnit,
                    DISTRIBUTION.UNIFORM, 1, 0, 10000);
            strucErrModelPanel.addParameter("&gamma;<sub>2</sub>", "-",
                    DISTRIBUTION.UNIFORM, 0.1, 0, 10000);
            strucErrModelPanels[k] = strucErrModelPanel;

            panel.appendChild(parameterNameLabel, 0);
            panel.appendChild(infoLabel, 0);
            panel.appendChild(strucErrModelPanel, 0);
            if (k + 1 < nOutputs) {
                JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
                panel.appendChild(sep, 0);
            }

            setContent(panel);
        }

        // strucErrModelPanel = new StructuralErrorModelPanel();

        // strucErrModelPanel.addParameter("&gamma;<sub>1</sub>", outputUnit,
        // DISTRIBUTION.UNIFORM, 0, 0, 10000);
        // strucErrModelPanel.addParameter("&gamma;<sub>2</sub>", "-",
        // DISTRIBUTION.UNIFORM, 0.1, 0, 10000);

        // setContent(strucErrModelPanel);
    }

    @Override
    public StructuralErrorModel[] getStructuralErrorModels() {
        StructuralErrorModel[] structErrorModels = new StructuralErrorModel[nOutputs];
        for (int k = 0; k < nOutputs; k++) {
            String name = "linear_model_" + k + "_" + outputSymbolAndUnits[k][0];
            String fileName = String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name);
            Parameter[] parameters = strucErrModelPanels[k].getParameters();
            structErrorModels[k] = new StructuralErrorModel(name, fileName, "Linear", parameters);
        }
        return structErrorModels;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray strucErrModelPanelsJSON = new JSONArray();
        for (int k = 0; k < nOutputs; k++) {
            strucErrModelPanelsJSON.put(k, strucErrModelPanels[k].toJSON());
        }
        json.put("strucErrModelPanels", strucErrModelPanelsJSON);
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        JSONArray strucErrModelPanelsJSON = json.getJSONArray("strucErrModelPanels");
        for (int k = 0; k < nOutputs; k++) {
            strucErrModelPanels[k].fromJSON(strucErrModelPanelsJSON.getJSONArray(k));
        }
    }

    @Override
    public BamItem clone(String uuid) {
        return new StructuralErrorModelBamItem(uuid, PROJECT, outputSymbolAndUnits);
    }

}
