package SeCause.SeCause_be.global.apiPayload.exception;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import java.util.Objects;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public GeneralException(BaseErrorCode errorCode) {
        super(Objects.requireNonNull(errorCode, "errorCode must not be null").getMessage());
        this.errorCode = errorCode;
    }
}
