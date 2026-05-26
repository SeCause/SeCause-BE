package SeCause.SeCause_be.domain.auth.controller;

import SeCause.SeCause_be.domain.auth.dto.GithubLoginRequest;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginResponse;
import SeCause.SeCause_be.domain.auth.dto.GithubLoginResult;
import SeCause.SeCause_be.domain.auth.service.GithubAuthService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.jwt.JwtCookieProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final GithubAuthService githubAuthService;
    private final JwtCookieProvider jwtCookieProvider;

    @PostMapping("/github/login")
    public ResponseEntity<ApiResponse<GithubLoginResponse>> loginWithGithub(
            @Valid @RequestBody GithubLoginRequest request
    ) {
        GithubLoginResult result = githubAuthService.login(request);
        ResponseCookie accessTokenCookie = jwtCookieProvider.createAccessTokenCookie(result.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .body(ApiResponse.onSuccess("깃허브 로그인이 완료됐습니다.", result.response()));
    }
}
