package SeCause.SeCause_be.domain.projectRepository.exception.code;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProjectRepositoryErrorCode implements BaseErrorCode {

    PROJECT_REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_REPOSITORY404", "요청한 레포지토리를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
