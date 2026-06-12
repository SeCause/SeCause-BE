package SeCause.SeCause_be.domain.analysis.queue;

public interface AnalysisRequestQueuePublisher {

    void publish(AnalysisRequestQueueMessage message);
}
