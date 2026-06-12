package SeCause.SeCause_be.domain.analysis.event;

import SeCause.SeCause_be.domain.analysis.queue.AnalysisRequestQueueConsumer;
import SeCause.SeCause_be.domain.analysis.queue.AnalysisRequestQueueMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AnalysisRequestedEventListener {

    private final AnalysisRequestQueueConsumer analysisRequestQueueConsumer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnalysisRequestedEvent event) {
        analysisRequestQueueConsumer.consume(new AnalysisRequestQueueMessage(event.analysisId()));
    }
}
