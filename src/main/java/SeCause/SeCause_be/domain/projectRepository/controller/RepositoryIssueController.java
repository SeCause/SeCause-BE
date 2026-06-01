package SeCause.SeCause_be.domain.projectRepository.controller;

import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueDetailResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueSeverity;
import SeCause.SeCause_be.domain.projectRepository.dto.VulnerableFileListResponse;
import SeCause.SeCause_be.domain.projectRepository.service.RepositoryIssueService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
public class RepositoryIssueController implements RepositoryIssueApi {

    private final RepositoryIssueService repositoryIssueService;

    @GetMapping("/{repositoryId}/analysis/issues")
    @Override
    public ApiResponse<RepositoryIssueListResponse> getRepositoryIssues(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long repositoryId,
            @RequestParam(defaultValue = "ALL") RepositoryIssueSeverity severity,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.") int size
    ) {
        RepositoryIssueListResponse response = repositoryIssueService.getRepositoryIssues(
                repositoryId,
                userPrincipal.userId(),
                severity,
                page,
                size
        );

        return ApiResponse.onSuccess("레포지토리 이슈 목록 조회가 완료됐습니다.", response);
    }

    @GetMapping("/{repositoryId}/analysis/files")
    @Override
    public ApiResponse<VulnerableFileListResponse> getVulnerableFiles(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long repositoryId
    ) {
        VulnerableFileListResponse response = repositoryIssueService.getVulnerableFiles(
                repositoryId,
                userPrincipal.userId()
        );

        return ApiResponse.onSuccess("취약 파일 목록 조회가 완료됐습니다.", response);
    }

    @GetMapping("/{repositoryId}/analysis/issues/{analysisResultId}")
    @Override
    public ApiResponse<RepositoryIssueDetailResponse> getRepositoryIssueDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long repositoryId,
            @PathVariable Long analysisResultId
    ) {
        RepositoryIssueDetailResponse response = repositoryIssueService.getRepositoryIssueDetail(
                repositoryId,
                userPrincipal.userId(),
                analysisResultId
        );

        return ApiResponse.onSuccess("이슈 상세 조회가 완료됐습니다.", response);
    }
}
