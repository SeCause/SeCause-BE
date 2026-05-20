package SeCause.SeCause_be.domain.auth.controller;

import SeCause.SeCause_be.domain.auth.dto.GithubLoginRequest;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginResponse;
import SeCause.SeCause_be.domain.auth.service.GithubAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final GithubAuthService githubAuthService;

    @PostMapping("/github/login")
    public ResponseEntity<GithubLoginResponse> loginWithGithub(
            @Valid @RequestBody GithubLoginRequest request
    ) {
        return ResponseEntity.ok(githubAuthService.login(request));
    }
}
