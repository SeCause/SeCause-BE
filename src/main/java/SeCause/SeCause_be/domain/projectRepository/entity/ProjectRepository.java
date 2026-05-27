package SeCause.SeCause_be.domain.projectRepository.entity;

import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "repositories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRepository extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repository_id")
    private Long repositoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "github_link", nullable = false, length = 500)
    private String githubLink;

    @Column(name = "branch", nullable = false, length = 100)
    private String branch = "main";

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "total_files", nullable = false)
    private int totalFiles = 0;

    private ProjectRepository(User user, String title, String description, String githubLink, String branch) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.githubLink = githubLink;
        this.branch = branch;
    }

    public static ProjectRepository create(
            User user,
            String title,
            String description,
            String githubLink,
            String branch
    ) {
        return new ProjectRepository(user, title, description, githubLink, branch);
    }

    public void updateInfo(String title, String description, String githubLink, String branch) {
        this.title = title;
        this.description = description;
        this.githubLink = githubLink;
        this.branch = branch;
    }

    public void updateTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public void delete() {
        this.deleted = true;
    }
}
