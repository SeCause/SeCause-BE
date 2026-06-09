package SeCause.SeCause_be.domain.analysis.validator;

import SeCause.SeCause_be.domain.user.code.UserErrorCode;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.exception.UserException;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AnalysisRequestValidator {

    private final UserRepository userRepository;

    public User validateLoginUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    public String validateGithubToken(String githubToken) {
        if (!StringUtils.hasText(githubToken)) {
            throw new UserException(UserErrorCode.GITHUB_TOKEN_NOT_FOUND);
        }

        return githubToken;
    }
}
