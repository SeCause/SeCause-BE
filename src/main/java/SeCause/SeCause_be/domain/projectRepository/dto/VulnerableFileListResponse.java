package SeCause.SeCause_be.domain.projectRepository.dto;

import java.util.List;

public record VulnerableFileListResponse(
        List<VulnerableFileSummaryResponse> files
) {

    public static VulnerableFileListResponse from(List<VulnerableFileSummaryResponse> files) {
        return new VulnerableFileListResponse(files);
    }
}
