package SeCause.SeCause_be.domain.auth.service;

import SeCause.SeCause_be.domain.auth.entity.LoginSession;
import SeCause.SeCause_be.domain.auth.exception.AuthException;
import SeCause.SeCause_be.domain.auth.repository.LoginSessionRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.global.security.jwt.JwtProperties;
import SeCause.SeCause_be.global.security.jwt.JwtTokenProvider;
import SeCause.SeCause_be.global.security.jwt.RefreshTokenHasher;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthTokenServiceTest {
    private static final String SECRET = "test-signing-secret-with-at-least-32-bytes";
    private final JwtProperties properties = new JwtProperties(SECRET, SECRET, SECRET, 3600000, 1209600000);
    private final JwtTokenProvider tokens = new JwtTokenProvider(properties);
    private final RefreshTokenHasher hasher = new RefreshTokenHasher(properties);
    private final LoginSessionRepository repository = mock(LoginSessionRepository.class);
    private final AuthTokenService service = new AuthTokenService(tokens, hasher, repository);
    private final Map<UUID, LoginSession> sessions = new HashMap<>();
    private User user;

    @BeforeEach
    void setUp() {
        user = User.createGithubUser(1L, "owner", "email", "name", "github", null);
        ReflectionTestUtils.setField(user, "userId", 1L);
        when(repository.save(any(LoginSession.class))).thenAnswer(invocation -> {
            LoginSession session = invocation.getArgument(0);
            sessions.put(session.getSessionId(), session);
            return session;
        });
        when(repository.findForUpdateBySessionId(any(UUID.class)))
                .thenAnswer(invocation -> Optional.ofNullable(sessions.get(invocation.getArgument(0))));
        doAnswer(invocation -> {
            LoginSession session = invocation.getArgument(0);
            sessions.remove(session.getSessionId());
            return null;
        }).when(repository).delete(any(LoginSession.class));
    }

    @Test
    void separateLoginsCanBothRefreshAndRotationOnlyChangesTheirOwnSession() {
        var a = service.issue(user);
        var b = service.issue(user);
        UUID aId = tokens.getRefreshTokenSessionId(a.refreshToken());
        UUID bId = tokens.getRefreshTokenSessionId(b.refreshToken());
        assertThat(aId).isNotEqualTo(bId);
        assertThat(sessions).hasSize(2);
        String bHash = sessions.get(bId).getRefreshTokenHash();

        var nextA = service.reissue(a.refreshToken());

        assertThat(tokens.isValidAccessToken(nextA.accessToken())).isTrue();
        assertThat(tokens.getRefreshTokenSessionId(nextA.refreshToken())).isEqualTo(aId);
        assertThat(nextA.refreshToken()).isNotEqualTo(a.refreshToken());
        assertThat(sessions.get(aId).getRefreshTokenHash()).isEqualTo(hasher.hash(nextA.refreshToken()));
        assertThat(sessions.get(bId).getRefreshTokenHash()).isEqualTo(bHash);
        assertThatThrownBy(() -> service.reissue(a.refreshToken())).isInstanceOf(AuthException.class);
        assertThatCode(() -> service.reissue(b.refreshToken())).doesNotThrowAnyException();
    }

    @Test
    void logoutOnlyRevokesCurrentSessionAndIsIdempotent() {
        var a = service.issue(user);
        var b = service.issue(user);
        service.logout(a.refreshToken());
        service.logout(a.refreshToken());
        assertThat(sessions).hasSize(1);
        assertThatThrownBy(() -> service.reissue(a.refreshToken())).isInstanceOf(AuthException.class);
        assertThatCode(() -> service.reissue(b.refreshToken())).doesNotThrowAnyException();
    }

    @Test
    void staleLogoutDoesNotDeleteRotatedSession() {
        var initial = service.issue(user);
        var rotated = service.reissue(initial.refreshToken());
        service.logout(initial.refreshToken());
        assertThatCode(() -> service.reissue(rotated.refreshToken())).doesNotThrowAnyException();
    }

    @Test
    void rejectsExpiredSessionAndWrongUser() {
        var issued = service.issue(user);
        LoginSession session = sessions.get(tokens.getRefreshTokenSessionId(issued.refreshToken()));
        session.rotate(session.getRefreshTokenHash(), Instant.now().minusSeconds(1));
        assertThatThrownBy(() -> service.reissue(issued.refreshToken())).isInstanceOf(AuthException.class);
        session.rotate(session.getRefreshTokenHash(), Instant.now().plusSeconds(60));
        ReflectionTestUtils.setField(user, "userId", 2L);
        assertThatThrownBy(() -> service.reissue(issued.refreshToken())).isInstanceOf(AuthException.class);
    }

    @Test
    void rejectsMissingMalformedAndLegacyTokens() {
        String legacy = Jwts.builder().subject("1").claim("type", "REFRESH")
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        for (String invalid : new String[]{null, "", "invalid", legacy}) {
            assertThatThrownBy(() -> service.reissue(invalid)).isInstanceOf(AuthException.class);
            assertThatCode(() -> service.logout(invalid)).doesNotThrowAnyException();
        }
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsExpiredJwtEvenWhenSessionHasNotExpired() {
        UUID sessionId = UUID.randomUUID();
        String expired = Jwts.builder().subject("1").claim("type", "REFRESH")
                .claim("sid", sessionId.toString()).expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        sessions.put(sessionId, LoginSession.create(sessionId, user, hasher.hash(expired), Instant.now().plusSeconds(60)));
        assertThatThrownBy(() -> service.reissue(expired)).isInstanceOf(AuthException.class);
        verifyNoInteractions(repository);
    }
}
