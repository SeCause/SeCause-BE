package SeCause.SeCause_be.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalSuccessCode implements BaseSuccessCode{

    OK(HttpStatus.OK, "COMMON200", "성공적으로 처리됐습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
