package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryDashboardResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryDashboardQueryResult;
import SeCause.SeCause_be.domain.projectRepository.validator.ProjectRepositoryValidator;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectRepositoryServiceTest {

    @Mock
    private ProjectRepositoryRepository projectRepositoryRepository;

    @Mock
    private ProjectRepositoryValidator projectRepositoryValidator;

    @InjectMocks
    private ProjectRepositoryService projectRepositoryService;

    @Test
    void getRepositoriesReturnsRepositorySummaries() {
        Long userId = 1L;
        String accountName = "secause";
        String keyword = "backend";
        RepositorySummaryResponse summary = RepositorySummaryResponse.of(
                10L,
                accountName,
                "SeCause-BE",
                "develop",
                120,
                3500L,
                List.of("Java", "SQL"),
                AnalysisStatus.COMPLETED,
                100,
                LocalDateTime.of(2026, 6, 12, 20, 50),
                LocalDateTime.of(2026, 6, 12, 20, 57)
        );
        given(projectRepositoryRepository.findRepositorySummaries(userId, accountName, keyword))
                .willReturn(List.of(summary));

        RepositoryListResponse response = projectRepositoryService.getRepositories(
                userId,
                accountName,
                keyword
        );

        assertThat(response.repositories()).containsExactly(summary);
        assertThat(response.repositories().getFirst().fullName()).isEqualTo("secause/SeCause-BE");
        verify(projectRepositoryRepository).findRepositorySummaries(userId, accountName, keyword);
    }

    @Test
    void getRepositoryDashboardBuildsSummaryAndSeverityBreakdown() {
        Long repositoryId = 10L;
        Long userId = 1L;
        Map<Severity, Long> countsBySeverity = new EnumMap<>(Severity.class);
        countsBySeverity.put(Severity.CRITICAL, 4L);
        countsBySeverity.put(Severity.HIGH, 16L);
        countsBySeverity.put(Severity.MEDIUM, 40L);
        countsBySeverity.put(Severity.LOW, 64L);
        RepositoryDashboardQueryResult queryResult = new RepositoryDashboardQueryResult(
                repositoryId,
                "secause",
                "SeCause-BE",
                "Security analysis backend",
                "https://github.com/secause/SeCause-BE",
                "develop",
                120,
                3500L,
                List.of("Java", "SQL"),
                AnalysisStatus.COMPLETED,
                100,
                LocalDateTime.of(2026, 6, 12, 20, 50),
                LocalDateTime.of(2026, 6, 12, 20, 57),
                null,
                List.of(
                        new RepositoryDashboardQueryResult.IssueTypeCount(
                                "SQL Injection",
                                Severity.CRITICAL,
                                4
                        )
                ),
                countsBySeverity
        );
        given(projectRepositoryRepository.findRepositoryDashboard(repositoryId, userId))
                .willReturn(Optional.of(queryResult));

        RepositoryDashboardResponse response = projectRepositoryService.getRepositoryDashboard(
                repositoryId,
                userId
        );

        assertThat(response.fullName()).isEqualTo("secause/SeCause-BE");
        assertThat(response.summary().totalIssues()).isEqualTo(124);
        assertThat(response.summary().criticalIssues()).isEqualTo(4);
        assertThat(response.severityBreakdown())
                .extracting(breakdown -> breakdown.severity())
                .containsExactly(
                        Severity.CRITICAL,
                        Severity.HIGH,
                        Severity.MEDIUM,
                        Severity.LOW
                );
        assertThat(response.severityBreakdown().getFirst().percentage()).isEqualTo(3.23);
        verify(projectRepositoryValidator).validateRepositoryOwner(repositoryId, userId);
    }
}
