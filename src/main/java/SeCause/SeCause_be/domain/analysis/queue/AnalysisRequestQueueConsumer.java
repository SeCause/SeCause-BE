package SeCause.SeCause_be.domain.analysis.queue;

import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.service.AnalysisDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisRequestQueueConsumer {

    private final AnalysisDispatchService analysisDispatchService;

    public void consume(AnalysisRequestQueueMessage message) {
        try {
            analysisDispatchService.markInProgress(message.analysisId());
            analysisDispatchService.dispatch(message.analysisId());
        } catch (AnalysisException exception) {
            analysisDispatchService.markFailed(message.analysisId(), exception.getMessage());
            log.error("Failed to dispatch analysis request. analysisId={}", message.analysisId(), exception);
        } catch (Exception exception) {
            analysisDispatchService.markFailed(
                    message.analysisId(),
                    AnalysisErrorCode.FASTAPI_REQUEST_FAILED.getMessage()
            );
            log.error("Unexpected error while dispatching analysis request. analysisId={}", message.analysisId(), exception);
        }
    }
}
