package SeCause.SeCause_be.domain.auth.client;

import SeCause.SeCause_be.domain.auth.dto.GithubAccessTokenResponse;
import SeCause.SeCause_be.domain.auth.dto.GithubUserResponse;
import SeCause.SeCause_be.domain.auth.properties.GithubOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GithubOAuthClient {

    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";

    private final WebClient webClient;
    private final GithubOAuthProperties githubOAuthProperties;

    public GithubAccessTokenResponse requestAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", githubOAuthProperties.clientId());
        formData.add("client_secret", githubOAuthProperties.clientSecret());
        formData.add("code", code);
        formData.add("redirect_uri", githubOAuthProperties.redirectUri());

        return webClient.post()
                .uri(GITHUB_TOKEN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GithubAccessTokenResponse.class)
                .block();
    }

    public GithubUserResponse requestUserInfo(String accessToken) {
        return webClient.get()
                .uri(GITHUB_USER_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GithubUserResponse.class)
                .block();
    }
}
