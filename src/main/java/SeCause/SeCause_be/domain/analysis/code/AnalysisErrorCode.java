package SeCause.SeCause_be.domain.analysis.code;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorCode implements BaseErrorCode {

    ANALYSIS_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_RESULT404", "요청한 분석 결과를 찾을 수 없습니다."),
    GITHUB_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "ANALYSIS_GITHUB4012", "GitHub 인증 정보가 유효하지 않습니다. 다시 로그인해주세요."),
    GITHUB_API_FORBIDDEN(HttpStatus.FORBIDDEN, "ANALYSIS_GITHUB403", "GitHub 레포지토리 목록을 조회할 권한이 없습니다."),
    GITHUB_API_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "ANALYSIS_GITHUB502", "GitHub API 호출에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
