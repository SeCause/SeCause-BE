package SeCause.SeCause_be.domain.auth.entity;

import SeCause.SeCause_be.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "login_sessions", indexes = {
        @Index(name = "idx_login_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_login_sessions_expires_at", columnList = "expires_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginSession {
    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash", nullable = false, length = 128)
    private String refreshTokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public static LoginSession create(UUID sessionId, User user, String refreshTokenHash, Instant expiresAt) {
        LoginSession session = new LoginSession();
        session.sessionId = sessionId;
        session.user = user;
        session.rotate(refreshTokenHash, expiresAt);
        return session;
    }

    public void rotate(String refreshTokenHash, Instant expiresAt) {
        this.refreshTokenHash = refreshTokenHash;
        this.expiresAt = expiresAt;
    }
}
