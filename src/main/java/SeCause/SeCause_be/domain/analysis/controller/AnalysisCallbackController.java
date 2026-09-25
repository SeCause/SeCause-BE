package SeCause.SeCause_be.domain.analysis.controller;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFailureRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackSuccessRequest;
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
    public ApiResponse<Void> handleSuccess(
            @PathVariable Long analysisId,
            @RequestBody @Valid AnalysisCallbackSuccessRequest request
    ) {
        analysisCallbackService.handleSuccess(analysisId, request);
        return ApiResponse.onSuccess("분석 성공 콜백 처리가 완료됐습니다.");
    }

    @PostMapping("/{analysisId}/failure")
    public ApiResponse<Void> handleFailure(
            @PathVariable Long analysisId,
            @RequestBody @Valid AnalysisCallbackFailureRequest request
    ) {
        analysisCallbackService.handleFailure(analysisId, request);
        return ApiResponse.onSuccess("분석 실패 콜백 처리가 완료됐습니다.");
    }
}
