package SeCause.SeCause_be.domain.user.validator;

import SeCause.SeCause_be.domain.user.code.UserErrorCode;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.exception.UserException;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    public User validateUserForUpdate(Long userId) {
        return userRepository.findForUpdateByUserId(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
