package SeCause.SeCause_be.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String refreshSecret,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {

    private static final long DEFAULT_REFRESH_TOKEN_EXPIRATION = 1_209_600_000L;

    public JwtProperties {
        if (refreshSecret == null || refreshSecret.isBlank()) {
            refreshSecret = secret;
        }
        if (refreshTokenExpiration <= 0) {
            refreshTokenExpiration = DEFAULT_REFRESH_TOKEN_EXPIRATION;
        }
    }
}
