package SeCause.SeCause_be.global.security.jwt;

import SeCause.SeCause_be.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;

    public String createAccessToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getAccessSigningKey())
                .compact();
    }

    public String createRefreshToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getRefreshSigningKey())
                .compact();
    }

    public Long getAccessTokenUserId(String token) {
        return Long.valueOf(getAccessTokenClaims(token).getSubject());
    }

    public Long getRefreshTokenUserId(String token) {
        return Long.valueOf(getRefreshTokenClaims(token).getSubject());
    }

    public boolean isValidAccessToken(String token) {
        getAccessTokenClaims(token);
        return true;
    }

    public boolean isValidRefreshToken(String token) {
        getRefreshTokenClaims(token);
        return true;
    }

    private Claims getAccessTokenClaims(String token) {
        Claims claims = getClaims(token, getAccessSigningKey());
        validateTokenType(claims, ACCESS_TOKEN_TYPE);
        return claims;
    }

    private Claims getRefreshTokenClaims(String token) {
        Claims claims = getClaims(token, getRefreshSigningKey());
        validateTokenType(claims, REFRESH_TOKEN_TYPE);
        return claims;
    }

    private Claims getClaims(String token, SecretKey signingKey) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateTokenType(Claims claims, String expectedTokenType) {
        if (!expectedTokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new IllegalArgumentException("Invalid token type");
        }
    }

    private SecretKey getAccessSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.refreshSecret().getBytes(StandardCharsets.UTF_8));
    }
}
