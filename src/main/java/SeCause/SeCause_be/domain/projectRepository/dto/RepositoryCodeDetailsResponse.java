package SeCause.SeCause_be.domain.projectRepository.dto;

import java.util.List;

public record RepositoryCodeDetailsResponse(
        String branch,
        int fileCount,
        long lineCount,
        List<String> languages
) {
}
