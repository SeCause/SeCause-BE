package SeCause.SeCause_be.domain.projectRepository.controller;

import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueDetailResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueSeverity;
import SeCause.SeCause_be.domain.projectRepository.dto.VulnerableFileListResponse;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Repository Issue", description = "레포지토리 분석 이슈 API")
public interface RepositoryIssueApi {

    @Operation(
            summary = "레포지토리 이슈 목록 조회",
            description = "레포지토리 분석 결과에서 발견된 코드/인프라 보안 이슈 목록을 취약도 기준으로 필터링하여 페이지네이션 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            parameters = {
                    @Parameter(
                            name = "repositoryId",
                            description = "레포지토리 ID",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "1"
                    ),
                    @Parameter(
                            name = "severity",
                            description = "필터링할 이슈 취약도 수준",
                            in = ParameterIn.QUERY,
                            schema = @Schema(implementation = RepositoryIssueSeverity.class),
                            example = "ALL"
                    ),
                    @Parameter(
                            name = "page",
                            description = "페이지 번호. 1부터 시작합니다.",
                            in = ParameterIn.QUERY,
                            example = "1"
                    ),
                    @Parameter(
                            name = "size",
                            description = "한 페이지에 보여질 이슈 수",
                            in = ParameterIn.QUERY,
                            example = "20"
                    )
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "레포지토리 이슈 목록 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "레포지토리 이슈 목록 조회가 완료됐습니다.",
                                      "result": {
                                        "content": [
                                          {
                                            "analysisResultId": 1,
                                            "vulnerabilityType": "SQL_INJECTION",
                                            "severity": "CRITICAL",
                                            "filePath": "src/utils/database.ts",
                                            "lineStart": 25,
                                            "lineEnd": 30,
                                            "summary": "사용자 입력값이 검증 없이 SQL 쿼리에 직접 사용됩니다."
                                          }
                                        ],
                                        "page": 1,
                                        "size": 20,
                                        "totalElements": 22,
                                        "totalPages": 2,
                                        "hasNext": true
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
                            name = "badRequest",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "COMMON4001",
                                      "message": "요청 값 검증에 실패했습니다.",
                                      "error": {
                                        "page": "페이지 번호는 1 이상이어야 합니다."
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
            description = "레포지토리를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "notFound",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "PROJECT_REPOSITORY404",
                                      "message": "요청한 레포지토리를 찾을 수 없습니다."
                                    }
                                    """
                    )
            )
    )
    ApiResponse<RepositoryIssueListResponse> getRepositoryIssues(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @PathVariable Long repositoryId,

            @RequestParam(defaultValue = "ALL") RepositoryIssueSeverity severity,

            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
            int size
    );

    @Operation(
            summary = "취약 파일 목록 조회",
            description = "레포지토리 분석 결과에서 취약점이 발견된 파일 목록과 파일별 이슈 수, CRITICAL 이슈 수를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            parameters = {
                    @Parameter(
                            name = "repositoryId",
                            description = "레포지토리 ID",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "1"
                    )
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "취약 파일 목록 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "취약 파일 목록 조회가 완료됐습니다.",
                                      "result": {
                                        "files": [
                                          {
                                            "repositoryFileId": 5,
                                            "filePath": "src/utils/database.ts",
                                            "fileType": "SOURCE",
                                            "language": "TypeScript",
                                            "issueCount": 8,
                                            "criticalCount": 2
                                          },
                                          {
                                            "repositoryFileId": 6,
                                            "filePath": "Dockerfile",
                                            "fileType": "INFRA",
                                            "language": null,
                                            "issueCount": 3,
                                            "criticalCount": 0
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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "레포지토리를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "notFound",
                            value = """
                                    {
                                      "isSuccess": false,
                                      "code": "PROJECT_REPOSITORY404",
                                      "message": "요청한 레포지토리를 찾을 수 없습니다."
                                    }
                                    """
                    )
            )
    )
    ApiResponse<VulnerableFileListResponse> getVulnerableFiles(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @PathVariable Long repositoryId
    );

    @Operation(
            summary = "이슈 상세 조회",
            description = "레포지토리 분석 이슈의 상세 정보와 관련 보안 레퍼런스를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            parameters = {
                    @Parameter(
                            name = "repositoryId",
                            description = "레포지토리 ID",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "1"
                    ),
                    @Parameter(
                            name = "analysisResultId",
                            description = "분석 결과 ID",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "1"
                    )
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "이슈 상세 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = @ExampleObject(
                            name = "success",
                            value = """
                                    {
                                      "isSuccess": true,
                                      "code": "COMMON2000",
                                      "message": "이슈 상세 조회가 완료됐습니다.",
                                      "result": {
                                        "analysisResultId": 1,
                                        "vulnerabilityType": "SQL_INJECTION",
                                        "severity": "CRITICAL",
                                        "filePath": "src/utils/database.ts",
                                        "lineStart": 25,
                                        "lineEnd": 30,
                                        "codeSnippet": "const q = 'SELECT * FROM users WHERE id=' + userId;",
                                        "description": "사용자 입력값이 검증 없이 SQL 쿼리에 직접 포함됩니다.",
                                        "summary": "비매개변수화 쿼리로 인한 SQL Injection 취약점",
                                        "attackScenario": "userId=1 OR 1=1 -- 입력 시 모든 사용자 데이터에 접근 가능합니다.",
                                        "fixCode": "const q = 'SELECT * FROM users WHERE id=$1';\\ndb.query(q, [userId]);",
                                        "fixSummary": "Prepared Statement로 파라미터 바인딩을 적용합니다.",
                                        "references": [
                                          {
                                            "securityReferenceId": 1,
                                            "referenceType": "OWASP",
                                            "title": "OWASP Top 10 A03:2021 Injection",
                                            "referenceUrl": "https://owasp.org/Top10/A03_2021-Injection/"
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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "레포지토리 또는 이슈를 찾을 수 없음",
            content = @Content(
                    schema = @Schema(implementation = ApiResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "repositoryNotFound",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "PROJECT_REPOSITORY404",
                                              "message": "요청한 레포지토리를 찾을 수 없습니다."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "analysisResultNotFound",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "ANALYSIS_RESULT404",
                                              "message": "요청한 분석 결과를 찾을 수 없습니다."
                                            }
                                            """
                            )
                    }
            )
    )
    ApiResponse<RepositoryIssueDetailResponse> getRepositoryIssueDetail(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @PathVariable Long repositoryId,

            @PathVariable Long analysisResultId
    );
}
