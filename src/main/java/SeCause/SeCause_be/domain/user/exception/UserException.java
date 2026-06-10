package SeCause.SeCause_be.domain.user.exception;

import SeCause.SeCause_be.domain.user.code.UserErrorCode;
import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;

public class UserException extends GeneralException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}
