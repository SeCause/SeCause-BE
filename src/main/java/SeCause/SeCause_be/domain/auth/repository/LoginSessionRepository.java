package SeCause.SeCause_be.domain.auth.repository;

import SeCause.SeCause_be.domain.auth.entity.LoginSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LoginSessionRepository extends JpaRepository<LoginSession, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from LoginSession s where s.sessionId = :sessionId")
    Optional<LoginSession> findForUpdateBySessionId(@Param("sessionId") UUID sessionId);
}
