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
    public User saveOrUpdateGithubUser(String email, String name, String githubToken) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.updateGithubProfile(name, githubToken);
                    return user;
                })
                .orElseGet(() -> userRepository.save(User.createGithubUser(email, name, githubToken)));
    }
}
