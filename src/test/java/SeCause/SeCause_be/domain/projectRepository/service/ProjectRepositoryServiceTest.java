package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
}
