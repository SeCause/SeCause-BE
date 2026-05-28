package SeCause.SeCause_be.domain.projectRepository.dto;

import SeCause.SeCause_be.domain.security.entity.ReferenceType;
import io.swagger.v3.oas.annotations.media.Schema;

public record SecurityReferenceResponse(
        @Schema(description = "보안 레퍼런스 ID", example = "1")
        Long securityReferenceId,

        @Schema(description = "레퍼런스 유형", example = "OWASP")
        ReferenceType referenceType,

        @Schema(description = "레퍼런스 제목", example = "OWASP Top 10 A03:2021 Injection")
        String title,

        @Schema(description = "레퍼런스 URL", example = "https://owasp.org/Top10/A03_2021-Injection/")
        String referenceUrl
) {
}
