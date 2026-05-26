package SeCause.SeCause_be.domain.analysis.repository;

import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.repository.dto.VulnerableFileListResponse;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import org.springframework.data.domain.Pageable;

public interface AnalysisResultRepositoryCustom {

    RepositoryIssueListResponse findRepositoryIssues(
            Long repositoryId,
            Long userId,
            Severity severity,
            Pageable pageable
    );

    VulnerableFileListResponse findVulnerableFiles(Long repositoryId, Long userId);
}
