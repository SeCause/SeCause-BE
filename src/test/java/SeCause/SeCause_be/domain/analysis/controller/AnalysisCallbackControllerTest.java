package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisFailureCallbackRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisResultCallbackRequest;
import SeCause.SeCause_be.domain.analysis.service.AnalysisCallbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AnalysisCallbackControllerTest {

    private static final String SUCCESS_JSON = """
            {
              "analysisId": 1, "repositoryId": 1, "status": "COMPLETED",
              "findings": [{
                "tool": "SEMGREP", "type": "SQL_INJECTION", "cweId": "CWE-89", "severity": "HIGH",
                "filePath": "src/db/user.py", "lineStart": 25, "lineEnd": 27,
                "message": "scanner message", "evidence": "취약 코드 조각",
                "summary": "요약", "rootCause": "원인", "impact": "영향", "recommendation": "수정 방향",
                "fixExamples": [{"language":"python","vulnerableCode":"...","fixedCode":"...","explanation":"..."}],
                "references": ["https://example.com/reference"],
                "referenceDocuments": [{"title":"문서 제목","url":"https://example.com/document","sourceType":"CWE"}]
              }],
              "summary": {"totalCount": 12}
            }
            """;

    private static final String FAILURE_JSON = """
            {
              "analysisId":1, "repositoryId":1, "status":"FAILED",
              "errorCode":"CLONE_FAILED", "errorMessage":"clone failed", "failedStage":"collect"
            }
            """;

    private AnalysisCallbackService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(AnalysisCallbackService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalysisCallbackController(service)).build();
    }

    @Test
    void receivesSuccessfulCallbackJson() throws Exception {
        mockMvc.perform(post("/api/internal/analyses/1/result")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SUCCESS_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(service).saveResult(eq(1L), org.mockito.ArgumentMatchers.any(AnalysisResultCallbackRequest.class));
    }

    @Test
    void receivesFailureCallbackJson() throws Exception {
        mockMvc.perform(post("/api/internal/analyses/1/failure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FAILURE_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));

        verify(service).saveFailure(eq(1L), org.mockito.ArgumentMatchers.any(AnalysisFailureCallbackRequest.class));
    }
}
