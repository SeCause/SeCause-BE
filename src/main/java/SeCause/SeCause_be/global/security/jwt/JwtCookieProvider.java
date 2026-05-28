package SeCause.SeCause_be.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class JwtCookieProvider {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    private final JwtProperties jwtProperties;

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessToken,
                Duration.ofMillis(jwtProperties.accessTokenExpiration())
        );
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return createCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                Duration.ofMillis(jwtProperties.refreshTokenExpiration())
        );
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return deleteCookie(ACCESS_TOKEN_COOKIE_NAME);
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return deleteCookie(REFRESH_TOKEN_COOKIE_NAME);
    }

    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ZERO)
                .build();
    }
}
