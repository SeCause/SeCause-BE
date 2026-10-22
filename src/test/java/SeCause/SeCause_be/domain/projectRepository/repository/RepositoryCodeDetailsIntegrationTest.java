package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RepositoryCodeDetailsIntegrationTest {
    @Autowired EntityManager entityManager;
    @Autowired ProjectRepositoryRepository repository;

    @Test
    void dashboardAndListReadPersistedMetricsWithoutVulnerableFiles() {
        var user = User.createGithubUser(987654321L, "metrics-test", "metrics@example.com", "Metrics", "token", null);
        entityManager.persist(user);
        var project = ProjectRepository.create(user, "metrics-test", "metrics-test", "test", "https://github.com/metrics-test/repo", "dev");
        project.updateCodeDetails(42, 1000, List.of(" TypeScript ", "JavaScript", "TypeScript"));
        entityManager.persist(project);
        var analysis = Analysis.create(project);
        entityManager.persist(analysis);
        entityManager.flush();
        Long projectId = project.getRepositoryId();
        Long userId = user.getUserId();
        entityManager.clear();

        var dashboard = repository.findRepositoryDashboard(projectId, userId).orElseThrow();
        assertThat(dashboard.fileCount()).isEqualTo(42);
        assertThat(dashboard.lineCount()).isEqualTo(1000);
        assertThat(dashboard.languages()).containsExactly("JavaScript", "TypeScript");
        var summaries = repository.findRepositorySummaries(userId, null, null);
        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().languages()).containsExactly("JavaScript", "TypeScript");
        assertThat(summaries.getFirst().fileCount()).isEqualTo(42);
        assertThat(summaries.getFirst().lineCount()).isEqualTo(1000);
    }
}
