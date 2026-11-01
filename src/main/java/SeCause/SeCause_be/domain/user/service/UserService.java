package SeCause.SeCause_be.domain.user.service;

import SeCause.SeCause_be.domain.user.dto.UserMeResponse;
import SeCause.SeCause_be.domain.user.dto.UserUpdateRequest;
import SeCause.SeCause_be.domain.user.dto.UserUpdateResponse;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import SeCause.SeCause_be.domain.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Transactional
    public User saveOrUpdateGithubUser(Long githubId, String githubLoginId, String email, String name, String githubToken, String avatarUrl) {
        return userRepository.findByGithubId(githubId)
                .or(() -> findExistingUserByEmail(email))
                .map(user -> {
                    user.updateGithubAuthentication(githubId, githubLoginId, email, githubToken);
                    return user;
                })
                .orElseGet(() -> userRepository.save(User.createGithubUser(githubId, githubLoginId, email, name, githubToken, avatarUrl)));
    }

    @Transactional(readOnly = true)
    public UserMeResponse getMe(Long userId) {
        User user = userValidator.validateUser(userId);
        return UserMeResponse.from(user);
    }

    @Transactional
    public UserUpdateResponse updateMe(Long userId, UserUpdateRequest request) {
        User user = userValidator.validateUser(userId);

        String name = resolveProfileValue(user.getName(), request.name());
        String avatarUrl = resolveAvatarUrl(user.getAvatarUrl(), request);

        user.updateProfile(name, avatarUrl);
        return UserUpdateResponse.from(user);
    }

    private java.util.Optional<User> findExistingUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return java.util.Optional.empty();
        }

        return userRepository.findByEmail(email);
    }

    private String resolveAvatarUrl(String currentValue, UserUpdateRequest request) {
        if (!request.hasAvatarUrl()) {
            return currentValue;
        }

        if (request.avatarUrl() == null) {
            return null;
        }

        return resolveProfileValue(currentValue, request.avatarUrl());
    }

    private String resolveProfileValue(String currentValue, String requestedValue) {
        if (!StringUtils.hasText(requestedValue)) {
            return currentValue;
        }

        return requestedValue.trim();
    }
}
