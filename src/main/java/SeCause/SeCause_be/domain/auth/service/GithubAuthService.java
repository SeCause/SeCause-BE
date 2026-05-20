package SeCause.SeCause_be.domain.auth.service;

import SeCause.SeCause_be.domain.auth.dto.GithubAccessTokenResponse;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginRequest;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginResponse;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginResult;
import SeCause.SeCause_be.domain.auth.dto.GithubUserResponse;
import SeCause.SeCause_be.domain.auth.properties.GithubOAuthProperties;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.service.UserService;
import SeCause.SeCause_be.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GithubAuthService {

    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_URL = "https://api.github.com/user";

    private final WebClient webClient;
    private final GithubOAuthProperties githubOAuthProperties;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public GithubLoginResult login(GithubLoginRequest request) {
        GithubAccessTokenResponse tokenResponse = requestAccessToken(request.code());
        GithubUserResponse userResponse = requestUserInfo(tokenResponse.accessToken());
        User user = userService.saveOrUpdateGithubUser(
                userResponse.email(),
                userResponse.name(),
                tokenResponse.accessToken()
        );
        String accessToken = jwtTokenProvider.createAccessToken(user);

        GithubLoginResponse response = new GithubLoginResponse(
                user.getUserId(),
                userResponse.id(),
                userResponse.login(),
                user.getName(),
                user.getEmail(),
                userResponse.avatarUrl()
        );

        return new GithubLoginResult(response, accessToken);
    }

    private GithubAccessTokenResponse requestAccessToken(String code) {
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

    private GithubUserResponse requestUserInfo(String accessToken) {
        return webClient.get()
                .uri(GITHUB_USER_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GithubUserResponse.class)
                .block();
    }
}
