package SeCause.SeCause_be.domain.user.code;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "요청한 유저 정보를 찾을 수 없습니다."),
    GITHUB_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "USER_GITHUB4011", "GitHub 인증 정보가 없습니다. 다시 로그인해주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
