package SeCause.SeCause_be.domain.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.oauth")
public record GithubOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {
}
