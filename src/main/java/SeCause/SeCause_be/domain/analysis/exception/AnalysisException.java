package SeCause.SeCause_be.domain.analysis.exception;

import SeCause.SeCause_be.domain.analysis.code.AnalysisErrorCode;
import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;

public class AnalysisException extends GeneralException {

    public AnalysisException(AnalysisErrorCode errorCode) {
        super(errorCode);
    }
}
