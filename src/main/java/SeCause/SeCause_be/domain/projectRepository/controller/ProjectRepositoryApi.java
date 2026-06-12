package SeCause.SeCause_be.domain.projectRepository.controller;

import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryListResponse;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Repository", description = "등록 레포지토리 관리 API")
public interface ProjectRepositoryApi {

    @Operation(
            summary = "레포지토리 목록 조회",
            description = "로그인한 사용자가 분석한 레포지토리 목록을 GitHub 계정과 검색어로 필터링하여 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "레포지토리 목록 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "레포지토리 목록 조회가 완료됐습니다.",
                                      "result": {
                                        "repositories": [
                                          {
                                            "repositoryId": 1,
                                            "owner": "secause",
                                            "name": "SeCause-BE",
                                            "fullName": "secause/SeCause-BE",
                                            "branch": "develop",
                                            "fileCount": 120,
                                            "lineCount": 3500,
                                            "languages": ["Java", "SQL"],
                                            "analysisStatus": "COMPLETED",
                                            "progressPercent": 100,
                                            "analysisRequestedAt": "2026-06-12T20:50:00",
                                            "completedAt": "2026-06-12T20:57:00"
                                          }
                                        ]
                                      }
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "unauthorized",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON401",
                                      "message": "인증이 필요합니다."
                                    }
                                    """
                    )
            )
    )
    ApiResponse<RepositoryListResponse> getRepositories(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @Parameter(description = "개인 또는 조직 GitHub 계정명", example = "secause")
            @RequestParam(required = false) String accountName,

            @Parameter(description = "레포지토리명 또는 계정명 검색어", example = "backend")
            @RequestParam(required = false) String keyword
    );
}
