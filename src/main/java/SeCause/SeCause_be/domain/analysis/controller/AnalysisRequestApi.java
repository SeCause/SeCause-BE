package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountListResponse;
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

@Tag(name = "Analysis Request", description = "분석 요청 API")
public interface AnalysisRequestApi {

    @Operation(
            summary = "연동 가능 GitHub 계정 목록 조회",
            description = "분석 요청 페이지의 Select Github Account 드롭다운에 표시할 내 GitHub 계정과 소속 조직 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "연동 가능 GitHub 계정 목록 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "연동 가능 GitHub 계정 목록 조회가 완료됐습니다.",
                                      "result": {
                                        "accounts": [
                                          {
                                            "name": "chaeyoungwon",
                                            "type": "PERSONAL"
                                          },
                                          {
                                            "name": "SeCause",
                                            "type": "ORGANIZATION"
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
            description = "인증 실패 또는 GitHub 토큰 문제",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "unauthorized",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "COMMON401",
                                              "message": "인증이 필요합니다."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "githubTokenNotFound",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "USER_GITHUB4011",
                                              "message": "GitHub 인증 정보가 없습니다. 다시 로그인해주세요."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "githubTokenInvalid",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "ANALYSIS_GITHUB4012",
                                              "message": "GitHub 인증 정보가 유효하지 않습니다. 다시 로그인해주세요."
                                            }
                                            """
                            )
                    }
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "502",
            description = "GitHub API 호출 실패",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "githubApiError",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ANALYSIS_GITHUB502",
                                      "message": "GitHub API 호출에 실패했습니다."
                                    }
                                    """
                    )
            )
    )
    ApiResponse<LinkableGithubAccountListResponse> getLinkableGithubAccounts(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}
