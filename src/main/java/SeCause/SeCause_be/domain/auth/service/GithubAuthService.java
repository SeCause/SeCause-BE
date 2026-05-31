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
import SeCause.SeCause_be.global.security.jwt.RefreshTokenHasher;
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
    private final RefreshTokenHasher refreshTokenHasher;

    public GithubLoginResult login(GithubLoginRequest request) {
        GithubAccessTokenResponse tokenResponse = requestAccessToken(request.code());
        GithubUserResponse userResponse = requestUserInfo(tokenResponse.accessToken());
        String name = userResponse.name() != null && !userResponse.name().isBlank()
                ? userResponse.name()
                : userResponse.login();
        User user = userService.saveOrUpdateGithubUser(
                userResponse.id(),
                userResponse.login(),
                userResponse.email(),
                name,
                tokenResponse.accessToken(),
                userResponse.avatarUrl()
        );
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        userService.updateRefreshTokenHash(user, refreshTokenHasher.hash(refreshToken));

        GithubLoginResponse response = new GithubLoginResponse(
                user.getUserId(),
                userResponse.id(),
                userResponse.login(),
                user.getName(),
                user.getEmail(),
                userResponse.avatarUrl()
        );

        return new GithubLoginResult(response, accessToken, refreshToken);
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
