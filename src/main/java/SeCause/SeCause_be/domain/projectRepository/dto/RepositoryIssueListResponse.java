package SeCause.SeCause_be.domain.projectRepository.dto;

import java.util.List;

public record RepositoryIssueListResponse(
        List<RepositoryIssueSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static RepositoryIssueListResponse from(
            List<RepositoryIssueSummaryResponse> content,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new RepositoryIssueListResponse(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page < totalPages
        );
    }
}
