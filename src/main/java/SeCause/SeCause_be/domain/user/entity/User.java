package SeCause.SeCause_be.domain.user.entity;

import SeCause.SeCause_be.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "github_token", nullable = false, length = 512)
    private String githubToken;

    private User(String email, String name, String githubToken) {
        this.email = email;
        this.name = name;
        this.githubToken = githubToken;
    }

    public static User createGithubUser(String email, String name, String githubToken) {
        return new User(email, name, githubToken);
    }

    public void updateGithubProfile(String name, String githubToken) {
        this.name = name;
        this.githubToken = githubToken;
    }
}
