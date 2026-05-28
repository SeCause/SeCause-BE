package SeCause.SeCause_be.domain.projectRepository.code;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProjectRepositoryErrorCode implements BaseErrorCode {

    INVALID_PAGE_REQUEST(HttpStatus.BAD_REQUEST, "PROJECT_REPOSITORY400", "페이지 요청 값이 올바르지 않습니다."),
    PROJECT_REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_REPOSITORY404", "요청한 레포지토리를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
