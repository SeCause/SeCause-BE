package SeCause.SeCause_be.domain.auth.dto;

public record GithubLoginResult(
        GithubLoginResponse response,
        String accessToken
) {
}
