package SeCause.SeCause_be.domain.repository.controller;

import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueSeverity;
import SeCause.SeCause_be.domain.repository.dto.VulnerableFileListResponse;
import SeCause.SeCause_be.domain.repository.service.RepositoryIssueService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
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
}
