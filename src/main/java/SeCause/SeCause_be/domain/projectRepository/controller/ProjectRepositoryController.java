package SeCause.SeCause_be.domain.projectRepository.controller;

import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryListResponse;
import SeCause.SeCause_be.domain.projectRepository.service.ProjectRepositoryService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repositories")
public class ProjectRepositoryController implements ProjectRepositoryApi {

    private final ProjectRepositoryService projectRepositoryService;

    @GetMapping
    @Override
    public ApiResponse<RepositoryListResponse> getRepositories(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String keyword
    ) {
        RepositoryListResponse response = projectRepositoryService.getRepositories(
                userPrincipal.userId(),
                accountName,
                keyword
        );

        return ApiResponse.onSuccess("레포지토리 목록 조회가 완료됐습니다.", response);
    }
}
