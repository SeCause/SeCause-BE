package SeCause.SeCause_be.domain.auth.service;

import SeCause.SeCause_be.domain.auth.code.AuthErrorCode;
import SeCause.SeCause_be.domain.auth.dto.TokenReissueResult;
import SeCause.SeCause_be.domain.auth.exception.AuthException;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.auth.entity.LoginSession;
import SeCause.SeCause_be.domain.auth.repository.LoginSessionRepository;

import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;
import SeCause.SeCause_be.global.security.jwt.JwtTokenProvider;
import SeCause.SeCause_be.global.security.jwt.RefreshTokenHasher;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final LoginSessionRepository loginSessionRepository;

    @Transactional
    public TokenReissueResult issue(User user) {
        UUID sessionId = UUID.randomUUID();
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user, sessionId);
        loginSessionRepository.save(LoginSession.create(sessionId, user,
                refreshTokenHasher.hash(refreshToken), jwtTokenProvider.getRefreshTokenExpiration(refreshToken)));
        return new TokenReissueResult(accessToken, refreshToken);
    }

    @Transactional
    public TokenReissueResult reissue(String refreshToken) {
        LoginSession session = getSessionByRefreshToken(refreshToken);
        User user = session.getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user, session.getSessionId());
        session.rotate(refreshTokenHasher.hash(newRefreshToken),
                jwtTokenProvider.getRefreshTokenExpiration(newRefreshToken));
        return new TokenReissueResult(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            LoginSession session = getSessionByRefreshToken(refreshToken);
            loginSessionRepository.delete(session);
        } catch (GeneralException ignored) {
            // Logout still clears client cookies even when the refresh token is missing or already invalid.
        }
    }

    private LoginSession getSessionByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw invalidRefreshToken();
        }

        try {
            if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
                throw invalidRefreshToken();
            }

            Long userId = jwtTokenProvider.getRefreshTokenUserId(refreshToken);
            UUID sessionId = jwtTokenProvider.getRefreshTokenSessionId(refreshToken);
            LoginSession session = loginSessionRepository.findForUpdateBySessionId(sessionId)
                    .orElseThrow(this::invalidRefreshToken);

            if (!userId.equals(session.getUser().getUserId())
                    || !session.getExpiresAt().isAfter(Instant.now())
                    || !refreshTokenHasher.matches(refreshToken, session.getRefreshTokenHash())) {
                throw invalidRefreshToken();
            }

            return session;
        } catch (JwtException | IllegalArgumentException exception) {
            throw invalidRefreshToken();
        }
    }

    private AuthException invalidRefreshToken() {
        return new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
