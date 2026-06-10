package SeCause.SeCause_be.domain.analysis.event;

import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.service.AnalysisDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisRequestedEventListener {

    private final AnalysisDispatchService analysisDispatchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnalysisRequestedEvent event) {
        try {
            analysisDispatchService.markInProgress(event.analysisId());
            analysisDispatchService.dispatch(event);
        } catch (AnalysisException exception) {
            analysisDispatchService.markFailed(event.analysisId(), exception.getMessage());
            log.error("Failed to dispatch analysis request. analysisId={}", event.analysisId(), exception);
        } catch (Exception exception) {
            analysisDispatchService.markFailed(
                    event.analysisId(),
                    AnalysisErrorCode.FASTAPI_REQUEST_FAILED.getMessage()
            );
            log.error("Unexpected error while dispatching analysis request. analysisId={}", event.analysisId(), exception);
        }
    }
}
