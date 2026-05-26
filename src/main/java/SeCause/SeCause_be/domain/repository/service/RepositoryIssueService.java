package SeCause.SeCause_be.domain.repository.service;

import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueSeverity;
import SeCause.SeCause_be.domain.repository.repository.RepositoryRepository;
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

    private final RepositoryRepository repositoryRepository;
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

    private void validatePageRequest(int page, int size) {
        if (page < 1 || size < 1) {
            throw new GeneralException(GlobalErrorCode.BAD_REQUEST);
        }
    }

    private void validateRepositoryOwner(Long repositoryId, Long userId) {
        boolean exists = repositoryRepository.existsByRepositoryIdAndUserUserIdAndDeletedFalse(repositoryId, userId);
        if (!exists) {
            throw new GeneralException(GlobalErrorCode.NOT_FOUND);
        }
    }
}
