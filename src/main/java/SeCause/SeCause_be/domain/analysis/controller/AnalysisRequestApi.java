package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryBranchListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryListResponse;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Operation(
            summary = "선택한 GitHub 계정의 레포지토리 목록 조회",
            description = "Select Github Account에서 선택한 내 계정 또는 조직의 GitHub 레포지토리 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            parameters = {
                    @Parameter(
                            name = "accountName",
                            description = "조회할 GitHub 계정 또는 조직 이름",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "SeCause"
                    ),
                    @Parameter(
                            name = "keyword",
                            description = "레포지토리 이름 또는 owner 기준 검색어",
                            in = ParameterIn.QUERY,
                            example = "backend"
                    )
            }
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
                                      "message": "연동 가능 레포지토리 목록 조회가 완료됐습니다.",
                                      "result": {
                                        "repositories": [
                                          {
                                            "name": "SeCause-BE",
                                            "owner": "SeCause",
                                            "defaultBranch": "develop",
                                            "private": false
                                          }
                                        ]
                                      }
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "validationError",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON4001",
                                      "message": "요청 값 검증에 실패했습니다.",
                                      "error": {
                                        "accountName": "GitHub 계정명은 필수입니다."
                                      }
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "GitHub 계정을 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "accountNotFound",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ANALYSIS_GITHUB4041",
                                      "message": "요청한 GitHub 계정을 찾을 수 없습니다."
                                    }
                                    """
                    )
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
    ApiResponse<LinkableRepositoryListResponse> getLinkableRepositories(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @RequestParam
            @NotBlank(message = "GitHub 계정명은 필수입니다.")
            String accountName,

            @RequestParam(required = false) String keyword
    );

    @Operation(
            summary = "GitHub 레포지토리 브랜치 목록 조회",
            description = "선택한 레포지토리의 브랜치 목록을 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            parameters = {
                    @Parameter(
                            name = "owner",
                            description = "레포지토리 owner 로그인명",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "SeCause"
                    ),
                    @Parameter(
                            name = "repository",
                            description = "레포지토리 이름",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "SeCause-BE"
                    )
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "브랜치 목록 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "브랜치 목록 조회가 완료됐습니다.",
                                      "result": {
                                        "branches": [
                                          {
                                            "name": "develop"
                                          },
                                          {
                                            "name": "main"
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
                    examples = @ExampleObject(
                            name = "githubTokenInvalid",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ANALYSIS_GITHUB4012",
                                      "message": "GitHub 인증 정보가 유효하지 않습니다. 다시 로그인해주세요."
                                    }
                                    """
                    )
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
    ApiResponse<LinkableRepositoryBranchListResponse> getLinkableRepositoryBranches(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @PathVariable String owner,

            @PathVariable String repository
    );
}
