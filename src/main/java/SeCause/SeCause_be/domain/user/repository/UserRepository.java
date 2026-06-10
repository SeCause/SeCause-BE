package SeCause.SeCause_be.domain.user.repository;

import SeCause.SeCause_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGithubId(Long githubId);

    Optional<User> findByEmail(String email);
}
