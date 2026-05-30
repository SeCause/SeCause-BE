package SeCause.SeCause_be.domain.auth.exception;

import SeCause.SeCause_be.domain.auth.code.AuthErrorCode;
import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;

public class AuthException extends GeneralException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
