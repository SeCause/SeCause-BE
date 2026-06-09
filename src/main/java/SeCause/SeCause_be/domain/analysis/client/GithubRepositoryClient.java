package SeCause.SeCause_be.domain.analysis.client;

import SeCause.SeCause_be.domain.analysis.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.dto.GithubAccountResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubUserAccountResponse;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class GithubRepositoryClient {

    private static final int PER_PAGE_LIMIT = 100;

    private final WebClient webClient;

    public GithubUserAccountResponse getUserAccount(String githubToken) {
        try {
            return webClient.get()
                    .uri(builder -> builder
                            .scheme("https")
                            .host("api.github.com")
                            .path("/user")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(GithubUserAccountResponse.class)
                    .block();
        } catch (WebClientResponseException.Unauthorized exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_TOKEN_INVALID);
        } catch (WebClientResponseException.Forbidden exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_FORBIDDEN);
        } catch (WebClientResponseException exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        } catch (WebClientException exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        }
    }

    public List<GithubAccountResponse> getUserOrganizations(String githubToken) {
        return getList(
                githubToken,
                builder -> builder
                        .scheme("https")
                        .host("api.github.com")
                        .path("/user/orgs")
                        .queryParam("per_page", PER_PAGE_LIMIT)
                        .build(),
                new ParameterizedTypeReference<List<GithubAccountResponse>>() {
                }
        );
    }

    private <T> List<T> getList(
            String githubToken,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<List<T>> responseType
    ) {
        try {
            List<T> response = webClient.get()
                    .uri(uriFunction)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();

            return response == null ? List.of() : response;
        } catch (WebClientResponseException.Unauthorized exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_TOKEN_INVALID);
        } catch (WebClientResponseException.Forbidden exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_FORBIDDEN);
        } catch (WebClientResponseException exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        } catch (WebClientException exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        }
    }
}
