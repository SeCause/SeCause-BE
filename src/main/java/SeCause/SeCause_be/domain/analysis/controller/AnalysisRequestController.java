package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountListResponse;
import SeCause.SeCause_be.domain.analysis.service.AnalysisRequestService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
