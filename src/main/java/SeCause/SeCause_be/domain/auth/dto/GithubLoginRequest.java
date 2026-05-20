package SeCause.SeCause_be.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GithubLoginRequest(
        @NotBlank String code
) {
}
