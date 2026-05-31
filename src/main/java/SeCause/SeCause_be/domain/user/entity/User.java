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

    @Column(name = "github_id", unique = true)
    private Long githubId;

    @Column(name = "github_login_id", unique = true)
    private String githubLoginId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "github_token", nullable = false, length = 512)
    private String githubToken;

    @Column(name = "avatar_url", length = 1024)
    private String avatarUrl;

    @Column(name = "refresh_token_hash", length = 128)
    private String refreshTokenHash;

    private User(Long githubId, String githubLoginId, String email, String name, String githubToken, String avatarUrl) {
        this.githubId = githubId;
        this.githubLoginId = githubLoginId;
        this.email = email;
        this.name = name;
        this.githubToken = githubToken;
        this.avatarUrl = avatarUrl;
    }

    public static User createGithubUser(Long githubId, String githubLoginId, String email, String name, String githubToken, String avatarUrl) {
        return new User(githubId, githubLoginId, email, name, githubToken, avatarUrl);
    }

    public void updateGithubProfile(Long githubId, String githubLoginId, String email, String name, String githubToken, String avatarUrl) {
        this.githubId = githubId;
        this.githubLoginId = githubLoginId;
        this.email = email;
        this.name = name;
        this.githubToken = githubToken;
        this.avatarUrl = avatarUrl;
    }

    public void updateRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public void clearRefreshToken() {
        this.refreshTokenHash = null;
    }
}
