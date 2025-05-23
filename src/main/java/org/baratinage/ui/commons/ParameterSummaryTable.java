package org.baratinage.ui.commons;

import java.util.List;

import javax.swing.JButton;

import org.baratinage.jbam.EstimatedParameter;
import org.baratinage.translation.T;
import org.baratinage.ui.bam.EstimatedParameterWrapper;
import org.baratinage.ui.component.DataTable;

public class ParameterSummaryTable extends DataTable {

    public ParameterSummaryTable() {

        ColumnHeaderDescription colHeaderDescRCGridTable = new ColumnHeaderDescription();
        colHeaderDescRCGridTable.addColumnDesc("Name", () -> {
            return T.text("name");
        });
        colHeaderDescRCGridTable.addColumnDesc("Prior_low", () -> {
            return T.text("low_bound_parameter", T.text("prior_density"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Prior_high", () -> {
            return T.text("high_bound_parameter", T.text("prior_density"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Posterior_maxpost", () -> {
            return T.text("high_bound_parameter", T.text("maxpost_desc"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Posterior_low", () -> {
            return T.text("low_bound_parameter", T.text("posterior_density"));
        });
        colHeaderDescRCGridTable.addColumnDesc("Posterior_high", () -> {
            return T.text("high_bound_parameter", T.text("posterior_density"));
        });
        colHeaderDescRCGridTable.addColumnDesc("consistency", () -> {
            return T.text("consistency_description");
        });

        JButton showHeaderDescription = new JButton();
        showHeaderDescription.addActionListener(l -> {
            colHeaderDescRCGridTable.openDialog(T.text("rating_table"));
        });
        T.t(this, showHeaderDescription, false, "table_headers_desc");

        toolsPanel.appendChild(showHeaderDescription);
    }

    public void updateResults(List<EstimatedParameterWrapper> parameters) {

        int n = parameters.size();

        String[] parameterNames = new String[n];
        double[] priorLow = new double[n];
        double[] priorHigh = new double[n];
        double[] postMaxpost = new double[n];
        double[] postLow = new double[n];
        double[] postHigh = new double[n];
        String[] consistencyCheck = new String[n];
        double[] noPrior = new double[] { Double.NaN, Double.NaN };

        for (int k = 0; k < n; k++) {
            EstimatedParameterWrapper p = parameters.get(k);
            double[] prior95 = !p.shouldDisplayPrior() ? noPrior
                    : p.parameter.parameterConfig.distribution.getPercentiles(0.025, 0.975, 2);
            double[] post95 = p.parameter.get95interval();
            parameterNames[k] = p.symbol;
            priorLow[k] = prior95[0];
            priorHigh[k] = prior95[1];
            postMaxpost[k] = p.parameter.getMaxpost();
            postLow[k] = post95[0];
            postHigh[k] = post95[1];
            consistencyCheck[k] = getParameterConsistencyCheckString(p.parameter, 0.01f);
        }

        clearColumns();
        addColumn(parameterNames);
        addColumn(priorLow);
        addColumn(priorHigh);
        addColumn(postMaxpost);
        addColumn(postLow);
        addColumn(postHigh);
        addColumn(consistencyCheck);
        updateData();
        setHeader(0, "Name");
        setHeader(1, "Prior_low");
        setHeader(2, "Prior_high");
        setHeader(3, "Posterior_maxpost");
        setHeader(4, "Posterior_low");
        setHeader(5, "Posterior_high");
        setHeader(6, "consistency");
        setHeaderWidth(100);
        updateHeader();
    }

    private static String getParameterConsistencyCheckString(EstimatedParameter parameter, float thresholdOffset) {
        Float value = parameter.getValidityCheckEstimate();
        if (value == null) {
            return "";
        }
        if (value < (0f + thresholdOffset) || value > (1f - thresholdOffset)) {
            return "POSSIBLE_INCONSISTENCY";
        }
        return "OK";
    }
}
