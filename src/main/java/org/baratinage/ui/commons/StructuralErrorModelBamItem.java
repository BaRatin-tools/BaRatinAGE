package org.baratinage.ui.commons;

import org.baratinage.jbam.DistributionType;
import org.baratinage.jbam.Parameter;
import org.baratinage.jbam.StructuralErrorModel;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.baratinage.jbam.utils.BamFilesHelpers;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.BamItem;
import org.baratinage.ui.bam.BamConfigRecord;
import org.baratinage.ui.bam.BamItemType;
import org.baratinage.ui.bam.BamProject;
import org.baratinage.ui.bam.IStructuralErrorModels;
import org.baratinage.ui.component.NameSymbolUnit;
import org.baratinage.ui.container.RowColPanel;
import org.json.JSONArray;
import org.json.JSONObject;

public class StructuralErrorModelBamItem extends BamItem implements IStructuralErrorModels {

    private final int nOutputs;
    private NameSymbolUnit[] nameSymbolUnits;
    private final StructuralErrorModelPanel[] strucErrModelPanels;

    private final JLabel parameterNameLabels[];

    public StructuralErrorModelBamItem(String uuid, BamProject project, NameSymbolUnit... nameSymbolUnits) {
        super(BamItemType.STRUCTURAL_ERROR, uuid, project);

        nOutputs = nameSymbolUnits.length;

        this.nameSymbolUnits = nameSymbolUnits;

        strucErrModelPanels = new StructuralErrorModelPanel[nOutputs];
        parameterNameLabels = new JLabel[nOutputs];

        RowColPanel panel = new RowColPanel(RowColPanel.AXIS.COL, RowColPanel.ALIGN.START);
        panel.setGap(5);
        panel.setPadding(5);

        for (int k = 0; k < nOutputs; k++) {

            parameterNameLabels[k] = new JLabel();
            parameterNameLabels[k].setText(buildNameSymbolString(nameSymbolUnits[k]));

            JLabel infoLabel = new JLabel();
            infoLabel.setText(
                    String.format(
                            "<html><code>N&sim;(&gamma;<sub>1</sub> + &gamma;<sub>2</sub>%s)</code></html>",
                            nameSymbolUnits[k].symbol()));

            // support only linear model, but with fixed gamma2 set to 0, it is equivalent
            // to the constant model
            StructuralErrorModelPanel strucErrModelPanel = new StructuralErrorModelPanel();
            strucErrModelPanel.addParameter("&gamma;<sub>1</sub>", nameSymbolUnits[k].unit(),
                    DistributionType.UNIFORM, 1, 0, 1000);
            strucErrModelPanel.addParameter("&gamma;<sub>2</sub>", "-",
                    DistributionType.UNIFORM, 0.1, 0, 1000);
            strucErrModelPanels[k] = strucErrModelPanel;

            strucErrModelPanel.addChangeListener((chEvt) -> {
                fireChangeListeners();
            });

            T.updateHierarchy(this, strucErrModelPanel);

            panel.appendChild(parameterNameLabels[k], 0);
            panel.appendChild(infoLabel, 0);
            panel.appendChild(strucErrModelPanel, 0);
            if (k + 1 < nOutputs) {
                JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
                panel.appendChild(sep, 0);
            }

            setContent(panel);
        }

    }

    private static String buildNameSymbolString(NameSymbolUnit nsu) {
        return "<html>" + nsu.name() + " (" + nsu.symbol() + ") </html>";
    }

    public void updateOutputNames(String... outputNames) {
        if (outputNames.length != nOutputs) {
            throw new IllegalArgumentException(nOutputs + " output names are expected!");
        }
        for (int k = 0; k < nOutputs; k++) {
            nameSymbolUnits[k] = new NameSymbolUnit(
                    outputNames[k],
                    nameSymbolUnits[k].symbol(),
                    nameSymbolUnits[k].unit());
            parameterNameLabels[k].setText(buildNameSymbolString(nameSymbolUnits[k]));

        }
    }

    @Override
    public StructuralErrorModel[] getStructuralErrorModels() {
        StructuralErrorModel[] structErrorModels = new StructuralErrorModel[nOutputs];
        for (int k = 0; k < nOutputs; k++) {
            String name = "linear_model_" + k + "_" + nameSymbolUnits[k].symbol();
            String fileName = String.format(BamFilesHelpers.CONFIG_STRUCTURAL_ERRORS, name);
            Parameter[] parameters = strucErrModelPanels[k].getParameters();
            structErrorModels[k] = new StructuralErrorModel(name, fileName, "Linear", parameters);
        }
        return structErrorModels;
    }

    @Override
    public BamConfigRecord save(boolean writeFiles) {
        JSONObject json = new JSONObject();
        JSONArray strucErrModelPanelsJSON = new JSONArray();
        for (int k = 0; k < nOutputs; k++) {
            strucErrModelPanelsJSON.put(k, strucErrModelPanels[k].toJSON());
        }
        json.put("strucErrModelPanels", strucErrModelPanelsJSON);
        return new BamConfigRecord(json);
    }

    @Override
    public void load(BamConfigRecord bamItemBackup) {
        JSONArray strucErrModelPanelsJSON = bamItemBackup.jsonObject().getJSONArray("strucErrModelPanels");
        for (int k = 0; k < nOutputs; k++) {
            strucErrModelPanels[k].fromJSON(strucErrModelPanelsJSON.getJSONArray(k));
        }
    }

}
