package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisFailureCallbackRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisResultCallbackRequest;
import SeCause.SeCause_be.domain.analysis.service.AnalysisCallbackService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/analyses")
public class AnalysisCallbackController {

    private final AnalysisCallbackService analysisCallbackService;

    @PostMapping("/{analysisId}/result")
    public ApiResponse<Void> saveResult(
            @PathVariable Long analysisId,
            @RequestBody @Valid AnalysisResultCallbackRequest request
    ) {
        analysisCallbackService.saveResult(analysisId, request);
        return ApiResponse.onSuccess("분석 결과를 수신했습니다.");
    }

    @PostMapping("/{analysisId}/failure")
    public ApiResponse<Void> saveFailure(
            @PathVariable Long analysisId,
            @RequestBody @Valid AnalysisFailureCallbackRequest request
    ) {
        analysisCallbackService.saveFailure(analysisId, request);
        return ApiResponse.onSuccess("분석 실패 결과를 수신했습니다.");
    }
}
