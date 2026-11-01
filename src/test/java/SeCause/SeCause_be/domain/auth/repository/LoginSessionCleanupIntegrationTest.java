package SeCause.SeCause_be.domain.auth.repository;

import SeCause.SeCause_be.domain.auth.entity.LoginSession;
import SeCause.SeCause_be.domain.auth.service.LoginSessionCleanupScheduler;
import SeCause.SeCause_be.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "auth.session-cleanup.cron=-")
@Transactional
class LoginSessionCleanupIntegrationTest {
    @Autowired EntityManager entityManager;
    @Autowired LoginSessionRepository repository;
    @Autowired LoginSessionCleanupScheduler scheduler;

    @Test
    void deletesExpiredSessionsIncludingBoundaryAndPreservesActiveSessions() {
        Instant cutoff = Instant.parse("2026-01-01T00:00:00Z");
        User user = createUser();
        UUID expired = createSession(user, cutoff.minusSeconds(1));
        UUID boundary = createSession(user, cutoff);
        UUID active = createSession(user, cutoff.plusSeconds(1));
        entityManager.flush();

        repository.deleteExpiredSessions(cutoff);
        entityManager.clear();

        assertThat(repository.findById(expired)).isEmpty();
        assertThat(repository.findById(boundary)).isEmpty();
        assertThat(repository.findById(active)).isPresent();
        assertThat(repository.deleteExpiredSessions(cutoff)).isZero();
    }

    @Test
    void schedulerCleansExpiredSessionsUsingCurrentTime() {
        User user = createUser();
        UUID expired = createSession(user, Instant.now().minusSeconds(3600));
        UUID active = createSession(user, Instant.now().plusSeconds(3600));
        entityManager.flush();

        scheduler.deleteExpiredSessions();
        entityManager.clear();

        assertThat(repository.findById(expired)).isEmpty();
        assertThat(repository.findById(active)).isPresent();
    }

    private User createUser() {
        User user = User.createGithubUser(987654322L, "session-cleanup-test",
                "session-cleanup@example.com", "Cleanup", "token", null);
        entityManager.persist(user);
        return user;
    }

    private UUID createSession(User user, Instant expiresAt) {
        UUID id = UUID.randomUUID();
        entityManager.persist(LoginSession.create(id, user, "test-hash", expiresAt));
        return id;
    }
}
