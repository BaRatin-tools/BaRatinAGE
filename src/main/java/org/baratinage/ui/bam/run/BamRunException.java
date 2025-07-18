package org.baratinage.ui.bam.run;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.baratinage.translation.T;
import org.baratinage.ui.component.CommonDialog;

public class BamRunException extends Exception {

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

    public final Exception originalException;
    public final String originalErrorMessage;

    private KnownBamRunError knownBamRunError;

    public BamRunException(Exception exception) {
        originalException = exception;
        originalErrorMessage = exception.getMessage();
    }

    public BamRunException(String consoleMessage) {
        originalException = this;
        originalErrorMessage = consoleMessage;
        knownBamRunError = null;
        for (KnownBamRunError kbre : knownBamRunErrors) {
            if (kbre.isKnownError.test(consoleMessage)) {
                knownBamRunError = kbre;
                break;
            }
        }
    }

    public void errorMessageDialog() {
        String msg = "";
        if (knownBamRunError == null) {
            if (originalErrorMessage == null) {
                msg = T.html("bam_run_error_unknown_error", "-");
            } else {
                msg = T.html("bam_run_error_unknown_error", originalErrorMessage.replace("\n", "<br>"));
            }
        } else {
            msg = T.html(knownBamRunError.errorMsgKey);
        }
        CommonDialog.errorDialog(msg, T.text("bam_error"));
    }
}
