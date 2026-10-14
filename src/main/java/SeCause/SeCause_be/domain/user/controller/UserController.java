package SeCause.SeCause_be.domain.user.controller;

import SeCause.SeCause_be.domain.user.dto.UserMeResponse;
import SeCause.SeCause_be.domain.user.dto.UserUpdateRequest;
import SeCause.SeCause_be.domain.user.dto.UserUpdateResponse;
import SeCause.SeCause_be.domain.user.service.UserService;
import SeCause.SeCause_be.domain.user.service.UserWithdrawalService;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import SeCause.SeCause_be.global.security.jwt.JwtCookieProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UserService userService;
    private final UserWithdrawalService userWithdrawalService;
    private final JwtCookieProvider jwtCookieProvider;

    @DeleteMapping("/me")
    @Override
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        userWithdrawalService.withdraw(userPrincipal.userId());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider.deleteAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, jwtCookieProvider.deleteRefreshTokenCookie().toString())
                .body(ApiResponse.onSuccess("회원탈퇴가 완료됐습니다."));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserMeResponse> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserMeResponse response = userService.getMe(userPrincipal.userId());
        return ApiResponse.onSuccess("내 정보 조회가 완료됐습니다.", response);
    }

    @PatchMapping("/me")
    @Override
    public ApiResponse<UserUpdateResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateMe(userPrincipal.userId(), request);

        return ApiResponse.onSuccess("내 정보 수정이 완료됐습니다.", response);
    }
}
