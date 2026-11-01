package SeCause.SeCause_be.domain.auth.service;

import SeCause.SeCause_be.domain.auth.client.GithubOAuthClient;
import SeCause.SeCause_be.domain.auth.dto.GithubAccessTokenResponse;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginRequest;
import SeCause.SeCause_be.domain.auth.dto.GithubUserResponse;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.service.UserService;
import SeCause.SeCause_be.domain.auth.dto.TokenReissueResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GithubAuthServiceTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "Display Name"})
    void preservesProfileFallbackAndTokenIssuance(String githubName) {
        GithubOAuthClient client = mock(GithubOAuthClient.class);
        UserService users = mock(UserService.class);
        AuthTokenService tokens = mock(AuthTokenService.class);
        GithubAuthService service = new GithubAuthService(client, users, tokens);
        String expectedName = githubName == null || githubName.isBlank() ? "owner" : githubName;
        User user = User.createGithubUser(1L, "owner", "email", expectedName, "github-token", "avatar");
        when(client.requestAccessToken("code"))
                .thenReturn(new GithubAccessTokenResponse("github-token", "bearer", "repo"));
        when(client.requestUserInfo("github-token"))
                .thenReturn(new GithubUserResponse(1L, "owner", githubName, "email", "avatar"));
        when(users.saveOrUpdateGithubUser(1L, "owner", "email", expectedName, "github-token", "avatar"))
                .thenReturn(user);
        when(tokens.issue(user)).thenReturn(new TokenReissueResult("access", "refresh"));

        var result = service.login(new GithubLoginRequest("code"));

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        var order = inOrder(client, users, tokens);
        order.verify(client).requestAccessToken("code");
        order.verify(client).requestUserInfo("github-token");
        order.verify(users).saveOrUpdateGithubUser(1L, "owner", "email", expectedName, "github-token", "avatar");
        order.verify(tokens).issue(user);
    }
}
