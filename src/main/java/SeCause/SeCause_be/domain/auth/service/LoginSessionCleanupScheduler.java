package SeCause.SeCause_be.domain.auth.service;

import SeCause.SeCause_be.domain.auth.repository.LoginSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSessionCleanupScheduler {
    private final LoginSessionRepository loginSessionRepository;

    @Scheduled(cron = "${auth.session-cleanup.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpiredSessions() {
        int deletedCount = loginSessionRepository.deleteExpiredSessions(Instant.now());
        log.info("Deleted {} expired login sessions", deletedCount);
    }
}
