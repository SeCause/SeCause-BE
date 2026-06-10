package SeCause.SeCause_be.global.security.jwt;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class RefreshTokenHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final JwtProperties jwtProperties;

    public RefreshTokenHasher(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String hash(String refreshToken) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    jwtProperties.refreshTokenHashSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(keySpec);
            byte[] digest = mac.doFinal(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash refresh token", exception);
        }
    }

    public boolean matches(String refreshToken, String refreshTokenHash) {
        if (refreshToken == null || refreshTokenHash == null) {
            return false;
        }

        byte[] expected = refreshTokenHash.getBytes(StandardCharsets.UTF_8);
        byte[] actual = hash(refreshToken).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expected);
    }
}
