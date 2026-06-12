package SeCause.SeCause_be.domain.projectRepository.controller;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryAnalysisResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryCodeDetailsResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryDashboardResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryDashboardSummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.service.ProjectRepositoryService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
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
class ProjectRepositoryControllerTest {

    @Mock
    private ProjectRepositoryService projectRepositoryService;

    @InjectMocks
    private ProjectRepositoryController projectRepositoryController;

    @Test
    void getRepositoryDashboardReturnsSuccessResponse() {
        Long repositoryId = 10L;
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "tester",
                "tester@example.com",
                "Tester",
                "https://example.com/avatar.png"
        );
        RepositoryDashboardResponse serviceResponse = new RepositoryDashboardResponse(
                repositoryId,
                "secause",
                "SeCause-BE",
                "secause/SeCause-BE",
                "Security analysis backend",
                "https://github.com/secause/SeCause-BE",
                new RepositoryCodeDetailsResponse("develop", 120, 3500L, List.of("Java")),
                new RepositoryAnalysisResponse(
                        AnalysisStatus.COMPLETED,
                        100,
                        LocalDateTime.of(2026, 6, 12, 20, 50),
                        LocalDateTime.of(2026, 6, 12, 20, 57),
                        null
                ),
                new RepositoryDashboardSummaryResponse(0, 0, 0, 0, 0),
                List.of(),
                List.of()
        );
        given(projectRepositoryService.getRepositoryDashboard(repositoryId, userPrincipal.userId()))
                .willReturn(serviceResponse);

        ApiResponse<RepositoryDashboardResponse> response =
                projectRepositoryController.getRepositoryDashboard(userPrincipal, repositoryId);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.message()).isEqualTo("레포지토리 대시보드 조회가 완료됐습니다.");
        assertThat(response.result()).isSameAs(serviceResponse);
        verify(projectRepositoryService).getRepositoryDashboard(repositoryId, userPrincipal.userId());
    }

    @Test
    void deleteRepositoryReturnsSuccessResponse() {
        Long repositoryId = 10L;
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "tester",
                "tester@example.com",
                "Tester",
                "https://example.com/avatar.png"
        );

        ApiResponse<Void> response = projectRepositoryController.deleteRepository(
                userPrincipal,
                repositoryId
        );

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.message()).isEqualTo("레포지토리 삭제가 완료됐습니다.");
        assertThat(response.result()).isNull();
        verify(projectRepositoryService).deleteRepository(repositoryId, userPrincipal.userId());
    }
}
