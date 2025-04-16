package org.baratinage.ui.bam;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;

public class BamRunError {

    private record KnownBamRunError(Predicate<String> isKnownError, String errorMsgKey) {

    }

    private static final List<KnownBamRunError> knownBamRunErrors = new ArrayList<>();

    static {

        knownBamRunErrors.add(new KnownBamRunError((String msg) -> {
            return msg.contains("Adaptive_Metro_OAAT:Fatal:Unfeasible starting point");
        }, "bam_run_error_unfeasible_starting_point"));

        knownBamRunErrors.add(new KnownBamRunError((String msg) -> {
            return msg.contains(
                    "Config_Read_Xtra:XtraRead:BaRatinBAC_XtraRead:FATAL: the number of active controls is not allowed to decrease");
        }, "bam_run_error_bac_invalid_matrix_no_ctrl_decrease"));

        knownBamRunErrors.add(new KnownBamRunError((String msg) -> {
            return msg.contains(
                    "Config_Read_Xtra:XtraRead:BaRatinBAC_XtraRead:FATAL: unsupported control matrix.");
        }, "bam_run_error_bac_unsupported_ctrl_matrix"));

    }

    public final String originalErrorMessage;
    private KnownBamRunError knownBamRunError;

    public BamRunError(Exception exception) {
        originalErrorMessage = exception.getMessage();
        knownBamRunError = null;
        for (KnownBamRunError kbre : knownBamRunErrors) {
            if (kbre.isKnownError.test(exception.getMessage())) {
                knownBamRunError = kbre;
                break;
            }
        }
    }

    public void errorMessageDialog() {
        String msg = "";
        if (knownBamRunError == null) {
            msg = T.html("bam_run_error_unknown_error", originalErrorMessage.replace("\n", "<br>"));
        } else {
            msg = T.html(knownBamRunError.errorMsgKey);
        }
        CommonDialog.errorDialog(msg, T.text("bam_error"));
    }
}
