package SeCause.SeCause_be.domain.analysis.client;

import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.dto.GithubAccountResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubBranchResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubRepositoryResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class GithubRepositoryClient {

    private static final int PER_PAGE_LIMIT = 100;

    private final WebClient webClient;

    public GithubUserAccountResponse getUserAccount(String githubToken) {
        return getResponse(
                githubToken,
                builder -> builder.path("/user").build(),
                ParameterizedTypeReference.forType(GithubUserAccountResponse.class),
                AnalysisErrorCode.GITHUB_API_REQUEST_FAILED
        );
    }

    public List<GithubAccountResponse> getUserOrganizations(String githubToken) {
        return getPagedList(
                githubToken,
                (builder, page) -> builder
                        .path("/user/orgs")
                        .queryParam("per_page", PER_PAGE_LIMIT)
                        .queryParam("page", page)
                        .build(),
                new ParameterizedTypeReference<List<GithubAccountResponse>>() {
                }
        );
    }

    public List<GithubRepositoryResponse> getUserOwnedRepositories(String githubToken) {
        return getPagedList(
                githubToken,
                (builder, page) -> builder
                        .path("/user/repos")
                        .queryParam("affiliation", "owner")
                        .queryParam("per_page", PER_PAGE_LIMIT)
                        .queryParam("page", page)
                        .build(),
                new ParameterizedTypeReference<List<GithubRepositoryResponse>>() {
                }
        );
    }

    public List<GithubRepositoryResponse> getOrganizationRepositories(String githubToken, String organization) {
        return getPagedList(
                githubToken,
                (builder, page) -> builder
                        .path("/orgs/{organization}/repos")
                        .queryParam("type", "all")
                        .queryParam("per_page", PER_PAGE_LIMIT)
                        .queryParam("page", page)
                        .build(organization),
                new ParameterizedTypeReference<List<GithubRepositoryResponse>>() {
                }
        );
    }

    public GithubRepositoryResponse getRepository(String githubToken, String owner, String repository) {
        return getObject(
                githubToken,
                builder -> builder
                        .path("/repos/{owner}/{repository}")
                        .build(owner, repository),
                GithubRepositoryResponse.class,
                AnalysisErrorCode.GITHUB_REPOSITORY_NOT_FOUND
        );
    }

    public List<GithubBranchResponse> getRepositoryBranches(String githubToken, String owner, String repository) {
        return getPagedList(
                githubToken,
                (builder, page) -> builder
                        .path("/repos/{owner}/{repository}/branches")
                        .queryParam("per_page", PER_PAGE_LIMIT)
                        .queryParam("page", page)
                        .build(owner, repository),
                new ParameterizedTypeReference<List<GithubBranchResponse>>() {
                }
        );
    }

    public void validateRepositoryBranchExists(
            String githubToken,
            String owner,
            String repository,
            String branch
    ) {
        getObject(
                githubToken,
                builder -> builder
                        .path("/repos/{owner}/{repository}/branches/{branch}")
                        .build(owner, repository, branch),
                GithubBranchResponse.class,
                AnalysisErrorCode.GITHUB_BRANCH_NOT_FOUND
        );
    }

    private <T> T getObject(
            String githubToken,
            Function<UriBuilder, URI> uriFunction,
            Class<T> responseType,
            AnalysisErrorCode notFoundCode
    ) {
        T response = getResponse(githubToken, uriFunction, ParameterizedTypeReference.forType(responseType), notFoundCode);
        if (response == null) {
            throw new AnalysisException(notFoundCode);
        }
        return response;
    }

    private <T> List<T> getPagedList(
            String githubToken,
            BiFunction<UriBuilder, Integer, URI> uriFunction,
            ParameterizedTypeReference<List<T>> responseType
    ) {
        List<T> results = new ArrayList<>();
        int page = 1;

        while (true) {
            int currentPage = page;
            List<T> response = getList(
                    githubToken,
                    builder -> uriFunction.apply(builder, currentPage),
                    responseType
            );
            results.addAll(response);
            if (response.size() < PER_PAGE_LIMIT) {
                return results;
            }
            page++;
        }
    }

    private <T> List<T> getList(
            String githubToken,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<List<T>> responseType
    ) {
        List<T> response = getResponse(githubToken, uriFunction, responseType, AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        return response == null ? List.of() : response;
    }

    private <T> T getResponse(
            String githubToken,
            Function<UriBuilder, URI> uriFunction,
            ParameterizedTypeReference<T> responseType,
            AnalysisErrorCode notFoundCode
    ) {
        try {
            return webClient.get()
                    .uri(builder -> uriFunction.apply(builder.scheme("https").host("api.github.com")))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException.Unauthorized exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_TOKEN_INVALID);
        } catch (WebClientResponseException.Forbidden exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_FORBIDDEN);
        } catch (WebClientResponseException.NotFound exception) {
            throw new AnalysisException(notFoundCode);
        } catch (WebClientException exception) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_API_REQUEST_FAILED);
        }
    }
}
