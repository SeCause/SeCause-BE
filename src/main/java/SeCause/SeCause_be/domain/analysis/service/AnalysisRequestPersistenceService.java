package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisRequestCreateRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisRequestCreateResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubRepositoryResponse;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.queue.AnalysisRequestQueueMessage;
import SeCause.SeCause_be.domain.analysis.queue.AnalysisRequestQueuePublisher;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.analysis.validator.AnalysisRequestValidator;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisRequestPersistenceService {

    private final ProjectRepositoryRepository projectRepositoryRepository;
    private final AnalysisRepository analysisRepository;
    private final AnalysisRequestQueuePublisher analysisRequestQueuePublisher;
    private final AnalysisRequestValidator analysisRequestValidator;

    @Transactional
    public AnalysisRequestCreateResponse saveAnalysisRequest(
            User user,
            GithubRepositoryResponse githubRepository,
            AnalysisRequestCreateRequest request
    ) {
        analysisRequestValidator.validateNoActiveAnalysis(user.getUserId(), githubRepository.cloneUrl(), request.branch());

        ProjectRepository repository = projectRepositoryRepository.save(ProjectRepository.create(
                user,
                githubRepository.name(),
                githubRepository.description(),
                githubRepository.cloneUrl(),
                request.branch()
        ));
        Analysis analysis = analysisRepository.save(Analysis.create(repository));

        //fastAPI 서버로 분석 요청 전송
        analysisRequestQueuePublisher.publish(new AnalysisRequestQueueMessage(analysis.getAnalysisId()));

        return AnalysisRequestCreateResponse.of(analysis, repository);
    }
}
