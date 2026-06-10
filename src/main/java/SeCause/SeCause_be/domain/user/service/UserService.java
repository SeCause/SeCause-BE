package SeCause.SeCause_be.domain.user.service;

import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User saveOrUpdateGithubUser(Long githubId, String githubLoginId, String email, String name, String githubToken, String avatarUrl) {
        return userRepository.findByGithubId(githubId)
                .or(() -> findExistingUserByEmail(email))
                .map(user -> {
                    user.updateGithubProfile(githubId, githubLoginId, email, name, githubToken, avatarUrl);
                    return user;
                })
                .orElseGet(() -> userRepository.save(User.createGithubUser(githubId, githubLoginId, email, name, githubToken, avatarUrl)));
    }

    @Transactional
    public void updateRefreshTokenHash(User user, String refreshTokenHash) {
        user.updateRefreshTokenHash(refreshTokenHash);
    }

    private java.util.Optional<User> findExistingUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return java.util.Optional.empty();
        }

        return userRepository.findByEmail(email);
    }
}
