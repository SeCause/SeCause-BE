package SeCause.SeCause_be.domain.analysis.exception.code;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AnalysisErrorCode implements BaseErrorCode {

    ANALYSIS_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_RESULT4041", "요청한 분석 결과를 찾을 수 없습니다."),
    ANALYSIS_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "ANALYSIS4091", "이미 진행 중인 분석 요청이 있습니다."),
    GITHUB_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "ANALYSIS_GITHUB4012", "GitHub 인증 정보가 유효하지 않습니다. 다시 로그인해주세요."),
    GITHUB_API_FORBIDDEN(HttpStatus.FORBIDDEN, "ANALYSIS_GITHUB4031", "GitHub 레포지토리 목록을 조회할 권한이 없습니다."),
    GITHUB_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_GITHUB4041", "요청한 GitHub 계정을 찾을 수 없습니다."),
    GITHUB_REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_GITHUB4042", "요청한 GitHub 레포지토리를 찾을 수 없습니다."),
    GITHUB_BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_GITHUB4043", "요청한 GitHub 브랜치를 찾을 수 없습니다."),
    GITHUB_API_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "ANALYSIS_GITHUB5021", "GitHub API 호출에 실패했습니다."),
    FASTAPI_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "ANALYSIS_FASTAPI5021", "분석 서버 요청에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
