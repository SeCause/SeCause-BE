package SeCause.SeCause_be.domain.analysis.code;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorCode implements BaseErrorCode {

    ANALYSIS_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_RESULT404", "요청한 분석 결과를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
