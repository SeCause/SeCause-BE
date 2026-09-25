package SeCause.SeCause_be.domain.projectRepository.dto;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;

import java.time.LocalDateTime;

public record RepositoryAnalysisResponse(
        AnalysisStatus status,
        int progressPercent,
        LocalDateTime requestedAt,
        LocalDateTime completedAt,
        String failureReason
) {
}
