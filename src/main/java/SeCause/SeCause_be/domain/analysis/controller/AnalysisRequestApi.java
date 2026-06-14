package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisRequestCreateRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisRequestCreateResponse;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisRequestStatusResponse;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Analysis Request", description = "분석 요청 API")
public interface AnalysisRequestApi {

    @Operation(
            summary = "분석 요청 생성",
            description = "프론트에서 선택한 GitHub owner, 레포지토리 이름, 브랜치를 검증한 뒤 분석 요청을 생성하고 FastAPI 분석 서버에 비동기로 전달합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "분석 요청 생성 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "분석 요청이 완료됐습니다.",
                                      "result": {
                                        "analysisId": 1,
                                        "repositoryId": 1,
                                        "analysisStatus": "PENDING"
                                      }
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청 값 검증 실패",
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
                                        "owner": "레포지토리 owner는 필수입니다.",
                                        "repositoryName": "레포지토리 이름은 필수입니다.",
                                        "branch": "브랜치는 필수입니다."
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
            responseCode = "404",
            description = "GitHub 레포지토리 또는 브랜치를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "repositoryNotFound",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "ANALYSIS_GITHUB4042",
                                              "message": "요청한 GitHub 레포지토리를 찾을 수 없습니다."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "branchNotFound",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "ANALYSIS_GITHUB4043",
                                              "message": "요청한 GitHub 브랜치를 찾을 수 없습니다."
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
    ApiResponse<AnalysisRequestCreateResponse> createAnalysisRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @RequestBody
            @Valid
            AnalysisRequestCreateRequest request
    );

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
                            name = "ownerName",
                            description = "레포지토리 ownerName 로그인명",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "SeCause"
                    ),
                    @Parameter(
                            name = "repositoryName",
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

            @PathVariable String ownerName,

            @PathVariable String repositoryName
    );

    @Operation(
            summary = "분석 상태 조회",
            description = "분석 요청 생성 시 반환받은 analysisId로 분석 진행 상태, 진행률, 실패 사유를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            parameters = @Parameter(
                    name = "analysisId",
                    description = "분석 요청 ID",
                    required = true,
                    in = ParameterIn.PATH,
                    example = "1"
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "분석 상태 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "분석 상태 조회가 완료됐습니다.",
                                      "result": {
                                        "analysisId": 1,
                                        "analysisStatus": "IN_PROGRESS",
                                        "progressPercent": 45,
                                        "failureReason": null
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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "분석 결과를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "analysisNotFound",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "ANALYSIS_RESULT4041",
                                      "message": "요청한 분석 결과를 찾을 수 없습니다."
                                    }
                                    """
                    )
            )
    )
    ApiResponse<AnalysisRequestStatusResponse> getAnalysisRequestStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @PathVariable Long analysisId
    );
}
