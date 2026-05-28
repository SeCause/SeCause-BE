package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueDetailResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueSeverity;
import SeCause.SeCause_be.domain.projectRepository.dto.VulnerableFileListResponse;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import SeCause.SeCause_be.global.apiPayload.code.GlobalErrorCode;
import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepositoryIssueService {

    private final ProjectRepositoryRepository projectRepositoryRepository;
    private final AnalysisResultRepository analysisResultRepository;

    public RepositoryIssueListResponse getRepositoryIssues(
            Long repositoryId,
            Long userId,
            RepositoryIssueSeverity severity,
            int page,
            int size
    ) {
        validatePageRequest(page, size);
        validateRepositoryOwner(repositoryId, userId);

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
        validateRepositoryOwner(repositoryId, userId);
        return analysisResultRepository.findVulnerableFiles(repositoryId, userId);
    }

    public RepositoryIssueDetailResponse getRepositoryIssueDetail(
            Long repositoryId,
            Long userId,
            Long analysisResultId
    ) {
        validateRepositoryOwner(repositoryId, userId);
        RepositoryIssueDetailResponse response = analysisResultRepository.findRepositoryIssueDetail(
                repositoryId,
                userId,
                analysisResultId
        );

        if (response == null) {
            throw new GeneralException(GlobalErrorCode.NOT_FOUND);
        }

        return response;
    }

    private void validatePageRequest(int page, int size) {
        if (page < 1 || size < 1) {
            throw new GeneralException(GlobalErrorCode.BAD_REQUEST);
        }
    }

    private void validateRepositoryOwner(Long repositoryId, Long userId) {
        boolean exists = projectRepositoryRepository.existsByRepositoryIdAndUserUserIdAndDeletedFalse(repositoryId, userId);
        if (!exists) {
            throw new GeneralException(GlobalErrorCode.NOT_FOUND);
        }
    }
}
