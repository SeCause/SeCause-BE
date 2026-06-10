package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryBranchListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryListResponse;
import SeCause.SeCause_be.domain.analysis.service.AnalysisRequestService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/analysis/request")
public class AnalysisRequestController implements AnalysisRequestApi {

    private final AnalysisRequestService analysisRequestService;

    @GetMapping("/accounts")
    @Override
    public ApiResponse<LinkableGithubAccountListResponse> getLinkableGithubAccounts(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        LinkableGithubAccountListResponse response = analysisRequestService.getLinkableGithubAccounts(
                userPrincipal.userId()
        );

        return ApiResponse.onSuccess("연동 가능 GitHub 계정 목록 조회가 완료됐습니다.", response);
    }

    @GetMapping("/repositories")
    @Override
    public ApiResponse<LinkableRepositoryListResponse> getLinkableRepositories(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @NotBlank(message = "GitHub 계정명은 필수입니다.") String accountName,
            @RequestParam(required = false) String keyword
    ) {
        LinkableRepositoryListResponse response = analysisRequestService.getLinkableRepositories(
                userPrincipal.userId(),
                accountName,
                keyword
        );

        return ApiResponse.onSuccess("연동 가능 레포지토리 목록 조회가 완료됐습니다.", response);
    }

    @GetMapping("/repositories/{owner}/{repository}/branches")
    @Override
    public ApiResponse<LinkableRepositoryBranchListResponse> getLinkableRepositoryBranches(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String owner,
            @PathVariable String repository
    ) {
        LinkableRepositoryBranchListResponse response = analysisRequestService.getLinkableRepositoryBranches(
                userPrincipal.userId(),
                owner,
                repository
        );

        return ApiResponse.onSuccess("브랜치 목록 조회가 완료됐습니다.", response);
    }
}
