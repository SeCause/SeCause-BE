package SeCause.SeCause_be.domain.auth.service;

import SeCause.SeCause_be.domain.auth.client.GithubOAuthClient;
import SeCause.SeCause_be.domain.auth.dto.GithubAccessTokenResponse;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginRequest;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginResponse;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginResult;
import SeCause.SeCause_be.domain.auth.dto.GithubUserResponse;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.service.UserService;
import SeCause.SeCause_be.global.security.jwt.JwtTokenProvider;
import SeCause.SeCause_be.global.security.jwt.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GithubAuthService {

    private final GithubOAuthClient githubOAuthClient;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;

    public GithubLoginResult login(GithubLoginRequest request) {
        GithubAccessTokenResponse tokenResponse = githubOAuthClient.requestAccessToken(request.code());
        GithubUserResponse userResponse = githubOAuthClient.requestUserInfo(tokenResponse.accessToken());
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
                user.getAvatarUrl()
        );

        return new GithubLoginResult(response, accessToken, refreshToken);
    }
}
