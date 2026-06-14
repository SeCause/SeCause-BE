package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryDashboardResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySeverityCountResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.exception.ProjectRepositoryException;
import SeCause.SeCause_be.domain.projectRepository.exception.code.ProjectRepositoryErrorCode;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryDashboardQueryResult;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectRepositoryServiceTest {

    @Mock
    private ProjectRepositoryRepository projectRepositoryRepository;

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
                new RepositorySeverityCountResponse(4, 16, 40, 64),
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
    void getRepositoriesBuildsFullNameWithoutNullText() {
        Long userId = 1L;
        RepositorySummaryResponse summary = RepositorySummaryResponse.of(
                10L,
                null,
                "SeCause-BE",
                "develop",
                120,
                3500L,
                List.of("Java"),
                new RepositorySeverityCountResponse(0, 0, 0, 0),
                AnalysisStatus.COMPLETED,
                100,
                LocalDateTime.of(2026, 6, 12, 20, 50),
                LocalDateTime.of(2026, 6, 12, 20, 57)
        );
        given(projectRepositoryRepository.findRepositorySummaries(userId, null, null))
                .willReturn(List.of(summary));

        RepositoryListResponse response = projectRepositoryService.getRepositories(
                userId,
                null,
                null
        );

        assertThat(response.repositories().getFirst().fullName()).isEqualTo("SeCause-BE");
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
        assertThat(response.severityBreakdown())
                .extracting(breakdown -> breakdown.severity())
                .containsExactly(
                        Severity.CRITICAL,
                        Severity.HIGH,
                        Severity.MEDIUM,
                        Severity.LOW
                );
        assertThat(response.severityBreakdown().getFirst().percentage()).isEqualTo(3.23);
        verify(projectRepositoryRepository).findRepositoryDashboard(repositoryId, userId);
    }

    @Test
    void getRepositoryDashboardBuildsFullNameWithoutNullText() {
        Long repositoryId = 10L;
        Long userId = 1L;
        RepositoryDashboardQueryResult queryResult = new RepositoryDashboardQueryResult(
                repositoryId,
                "secause",
                null,
                null,
                "https://github.com/secause/SeCause-BE",
                "develop",
                0,
                0L,
                List.of(),
                AnalysisStatus.COMPLETED,
                100,
                LocalDateTime.of(2026, 6, 12, 20, 50),
                LocalDateTime.of(2026, 6, 12, 20, 57),
                null,
                List.of(),
                Map.of()
        );
        given(projectRepositoryRepository.findRepositoryDashboard(repositoryId, userId))
                .willReturn(Optional.of(queryResult));

        RepositoryDashboardResponse response = projectRepositoryService.getRepositoryDashboard(
                repositoryId,
                userId
        );

        assertThat(response.fullName()).isEqualTo("secause");
    }

    @Test
    void deleteRepositoryMarksRepositoryAsDeleted() {
        Long repositoryId = 10L;
        Long userId = 1L;
        ProjectRepository repository = ProjectRepository.create(
                null,
                "secause",
                "SeCause-BE",
                "Security analysis backend",
                "https://github.com/secause/SeCause-BE.git",
                "develop"
        );
        given(projectRepositoryRepository.findByRepositoryIdAndUserUserIdAndDeletedFalse(
                repositoryId,
                userId
        )).willReturn(Optional.of(repository));

        projectRepositoryService.deleteRepository(repositoryId, userId);

        assertThat(repository.isDeleted()).isTrue();
        verify(projectRepositoryRepository).findByRepositoryIdAndUserUserIdAndDeletedFalse(
                repositoryId,
                userId
        );
    }

    @Test
    void deleteRepositoryThrowsExceptionWhenRepositoryIsNotAccessible() {
        Long repositoryId = 10L;
        Long userId = 1L;
        given(projectRepositoryRepository.findByRepositoryIdAndUserUserIdAndDeletedFalse(
                repositoryId,
                userId
        )).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectRepositoryService.deleteRepository(repositoryId, userId))
                .isInstanceOf(ProjectRepositoryException.class)
                .satisfies(exception -> assertThat(
                        ((ProjectRepositoryException) exception).getErrorCode()
                ).isEqualTo(ProjectRepositoryErrorCode.PROJECT_REPOSITORY_NOT_FOUND));
    }
}
