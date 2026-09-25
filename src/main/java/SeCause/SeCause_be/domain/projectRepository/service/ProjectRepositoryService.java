package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryAnalysisResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryCodeDetailsResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryDashboardResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryDashboardSummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueTypeCountResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySeverityBreakdownResponse;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.exception.ProjectRepositoryException;
import SeCause.SeCause_be.domain.projectRepository.exception.code.ProjectRepositoryErrorCode;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryDashboardQueryResult;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectRepositoryService {

    private final ProjectRepositoryRepository projectRepositoryRepository;

    /**
     * 로그인 사용자가 분석한 레포지토리 목록을 조회합니다.
     */
    public RepositoryListResponse getRepositories(Long userId, String accountName, String keyword) {
        return RepositoryListResponse.from(
                projectRepositoryRepository.findRepositorySummaries(userId, accountName, keyword)
        );
    }

    /**
     * 로그인 사용자가 소유한 레포지토리의 분석 대시보드를 조회합니다.
     */
    public RepositoryDashboardResponse getRepositoryDashboard(Long repositoryId, Long userId) {
        RepositoryDashboardQueryResult result = projectRepositoryRepository
                .findRepositoryDashboard(repositoryId, userId)
                .orElseThrow(() -> new ProjectRepositoryException(
                        ProjectRepositoryErrorCode.PROJECT_REPOSITORY_NOT_FOUND
                ));

        Map<Severity, Long> countsBySeverity = result.issueCountsBySeverity();
        long totalIssues = countsBySeverity.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        return new RepositoryDashboardResponse(
                result.repositoryId(),
                result.owner(),
                result.name(),
                createFullName(result.owner(), result.name()),
                result.description(),
                result.githubUrl(),
                createCodeDetails(result),
                createAnalysis(result),
                createSummary(totalIssues),
                createIssuesByType(result),
                createSeverityBreakdown(countsBySeverity, totalIssues)
        );
    }

    /**
     * 로그인 사용자가 소유한 레포지토리를 삭제 처리합니다.
     */
    @Transactional
    public void deleteRepository(Long repositoryId, Long userId) {
        ProjectRepository repository = projectRepositoryRepository
                .findByRepositoryIdAndUserUserIdAndDeletedFalse(repositoryId, userId)
                .orElseThrow(() -> new ProjectRepositoryException(
                        ProjectRepositoryErrorCode.PROJECT_REPOSITORY_NOT_FOUND
                ));

        repository.delete();
    }

    private RepositoryCodeDetailsResponse createCodeDetails(
            RepositoryDashboardQueryResult result
    ) {
        return new RepositoryCodeDetailsResponse(
                result.branch(),
                result.fileCount(),
                result.lineCount(),
                result.languages()
        );
    }

    private RepositoryAnalysisResponse createAnalysis(RepositoryDashboardQueryResult result) {
        return new RepositoryAnalysisResponse(
                result.analysisStatus(),
                result.progressPercent(),
                result.analysisRequestedAt(),
                result.completedAt(),
                result.failureReason()
        );
    }

    private RepositoryDashboardSummaryResponse createSummary(long totalIssues) {
        return new RepositoryDashboardSummaryResponse(totalIssues);
    }

    private List<RepositoryIssueTypeCountResponse> createIssuesByType(
            RepositoryDashboardQueryResult result
    ) {
        return result.issuesByType().stream()
                .map(issue -> new RepositoryIssueTypeCountResponse(
                        issue.type(),
                        issue.severity(),
                        issue.count()
                ))
                .toList();
    }

    private List<RepositorySeverityBreakdownResponse> createSeverityBreakdown(
            Map<Severity, Long> countsBySeverity,
            long totalIssues
    ) {
        return Arrays.stream(Severity.values())
                .map(severity -> {
                    long count = issueCount(countsBySeverity, severity);
                    return new RepositorySeverityBreakdownResponse(
                            severity,
                            count,
                            calculatePercentage(count, totalIssues)
                    );
                })
                .toList();
    }

    private long issueCount(Map<Severity, Long> countsBySeverity, Severity severity) {
        return countsBySeverity.getOrDefault(severity, 0L);
    }

    private double calculatePercentage(long count, long totalIssues) {
        if (totalIssues == 0) {
            return 0.0;
        }

        return Math.round(count * 10_000.0 / totalIssues) / 100.0;
    }

    private String createFullName(String owner, String name) {
        if (owner == null || owner.isBlank()) {
            return name == null ? "" : name;
        }
        if (name == null || name.isBlank()) {
            return owner;
        }
        return owner + "/" + name;
    }
}
