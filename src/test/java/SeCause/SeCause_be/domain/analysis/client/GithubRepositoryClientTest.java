package SeCause.SeCause_be.domain.analysis.client;

import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GithubRepositoryClientTest {

    @ParameterizedTest
    @CsvSource({"401,GITHUB_TOKEN_INVALID", "403,GITHUB_API_FORBIDDEN", "500,GITHUB_API_REQUEST_FAILED"})
    void preservesSharedErrorMapping(int status, AnalysisErrorCode code) {
        GithubRepositoryClient client = client(status, "");
        assertError(() -> client.getUserAccount("token"), code);
        assertError(() -> client.getUserOrganizations("token"), code);
        assertError(() -> client.getRepository("token", "owner", "repo"), code);
        assertError(() -> client.validateRepositoryBranchExists("token", "owner", "repo", "main"), code);
    }

    @Test
    void preservesEndpointSpecificNotFoundErrors() {
        GithubRepositoryClient client = client(404, "");
        assertError(() -> client.getUserAccount("token"), AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        assertError(() -> client.getUserOrganizations("token"), AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        assertError(() -> client.getRepository("token", "owner", "repo"), AnalysisErrorCode.GITHUB_REPOSITORY_NOT_FOUND);
        assertError(() -> client.validateRepositoryBranchExists("token", "owner", "repo", "main"), AnalysisErrorCode.GITHUB_BRANCH_NOT_FOUND);
    }

    @Test
    void preservesEmptyResponseHandling() {
        GithubRepositoryClient client = client(200, "");
        assertThat(client.getUserAccount("token")).isNull();
        assertThat(client.getUserOrganizations("token")).isEmpty();
        assertError(() -> client.getRepository("token", "owner", "repo"), AnalysisErrorCode.GITHUB_REPOSITORY_NOT_FOUND);
        assertError(() -> client.validateRepositoryBranchExists("token", "owner", "repo", "main"), AnalysisErrorCode.GITHUB_BRANCH_NOT_FOUND);
    }

    @Test
    void preservesPaginationHeadersAndOrder() {
        List<ClientRequest> requests = new ArrayList<>();
        String firstPage = "[" + String.join(",", java.util.Collections.nCopies(100, "{\"login\":\"first\"}")) + "]";
        GithubRepositoryClient client = new GithubRepositoryClient(WebClient.builder().exchangeFunction(request -> {
            requests.add(request);
            return Mono.just(response(200, requests.size() == 1 ? firstPage : "[{\"login\":\"last\"}]"));
        }).build());

        var accounts = client.getUserOrganizations("token");

        assertThat(accounts).hasSize(101);
        assertThat(accounts.getFirst().login()).isEqualTo("first");
        assertThat(accounts.getLast().login()).isEqualTo("last");
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).url().toString()).isEqualTo("https://api.github.com/user/orgs?per_page=100&page=1");
        assertThat(requests.get(1).url().toString()).endsWith("page=2");
        assertThat(requests).allSatisfy(request -> {
            assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer token");
            assertThat(request.headers().getFirst(HttpHeaders.ACCEPT)).isEqualTo("application/json");
        });
    }

    private GithubRepositoryClient client(int status, String body) {
        return new GithubRepositoryClient(WebClient.builder()
                .exchangeFunction(request -> Mono.just(response(status, body))).build());
    }

    private ClientResponse response(int status, String body) {
        var builder = ClientResponse.create(HttpStatus.valueOf(status))
                .header(HttpHeaders.CONTENT_TYPE, "application/json");
        return body.isEmpty() ? builder.build() : builder.body(body).build();
    }

    private void assertError(Runnable action, AnalysisErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOfSatisfying(AnalysisException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(code));
    }
}
