package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueDetailResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueSeverity;
import SeCause.SeCause_be.domain.projectRepository.dto.VulnerableFileListResponse;
import SeCause.SeCause_be.domain.projectRepository.validator.ProjectRepositoryValidator;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepositoryIssueService {

    private final AnalysisResultRepository analysisResultRepository;
    private final ProjectRepositoryValidator projectRepositoryValidator;

    public RepositoryIssueListResponse getRepositoryIssues(
            Long repositoryId,
            Long userId,
            RepositoryIssueSeverity severity,
            int page,
            int size
    ) {
        projectRepositoryValidator.validateRepositoryOwner(repositoryId, userId);

        Severity severityFilter = severity.toSeverityOrNull();
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        return analysisResultRepository.findRepositoryIssues(
                repositoryId,
                userId,
                severityFilter,
                pageRequest
        );
    }

    public VulnerableFileListResponse getVulnerableFiles(Long repositoryId, Long userId) {
        projectRepositoryValidator.validateRepositoryOwner(repositoryId, userId);
        return analysisResultRepository.findVulnerableFiles(repositoryId, userId);
    }

    public RepositoryIssueDetailResponse getRepositoryIssueDetail(
            Long repositoryId,
            Long userId,
            Long analysisResultId
    ) {
        projectRepositoryValidator.validateRepositoryOwner(repositoryId, userId);
        RepositoryIssueDetailResponse response = analysisResultRepository.findRepositoryIssueDetail(
                repositoryId,
                userId,
                analysisResultId
        );

        if (response == null) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND);
        }

        return response;
    }
}
