package SeCause.SeCause_be.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class JwtCookieProvider {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    private final JwtProperties jwtProperties;

    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofMillis(jwtProperties.accessTokenExpiration()))
                .build();
    }
}
