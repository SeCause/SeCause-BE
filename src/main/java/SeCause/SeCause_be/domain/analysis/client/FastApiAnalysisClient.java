package SeCause.SeCause_be.domain.analysis.client;

import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.dto.FastApiAnalysisRequest;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

@Component
@RequiredArgsConstructor
public class FastApiAnalysisClient {

    private final WebClient webClient;

    @Value("${fast-api.analysis-request-url}")
    private String analysisRequestUrl;

    //fastAPI로 분석 요청 전송
    public void requestAnalysis(FastApiAnalysisRequest request) {
        try {
            webClient.post()
                    .uri(analysisRequestUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientException exception) {
            throw new AnalysisException(AnalysisErrorCode.FASTAPI_REQUEST_FAILED);
        }
    }
}
