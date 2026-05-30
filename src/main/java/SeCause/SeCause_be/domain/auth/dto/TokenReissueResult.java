package SeCause.SeCause_be.domain.auth.dto;

public record TokenReissueResult(
        String accessToken,
        String refreshToken
) {
}
