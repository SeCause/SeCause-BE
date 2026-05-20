package SeCause.SeCause_be.domain.auth.dto;

public record GithubLoginResponse(
        Long userId,
        Long githubId,
        String githubLoginId,
        String name,
        String email,
        String avatarUrl
) {
}
