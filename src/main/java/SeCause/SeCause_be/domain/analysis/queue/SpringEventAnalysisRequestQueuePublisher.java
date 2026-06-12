package SeCause.SeCause_be.domain.analysis.queue;

import SeCause.SeCause_be.domain.analysis.event.AnalysisRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringEventAnalysisRequestQueuePublisher implements AnalysisRequestQueuePublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(AnalysisRequestQueueMessage message) {
        eventPublisher.publishEvent(new AnalysisRequestedEvent(message.analysisId()));
    }
}
