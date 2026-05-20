package SeCause.SeCause_be.domain.user.controller;

import SeCause.SeCause_be.domain.user.dto.UserMeResponse;
import SeCause.SeCause_be.global.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(UserMeResponse.from(userPrincipal));
    }
}
