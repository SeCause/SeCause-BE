package SeCause.SeCause_be.domain.user.dto;

import SeCause.SeCause_be.global.security.UserPrincipal;

public record UserMeResponse(
        Long userId,
        String email,
        String name,
        String avatarUrl
) {

    public static UserMeResponse from(UserPrincipal userPrincipal) {
        return new UserMeResponse(
                userPrincipal.userId(),
                userPrincipal.email(),
                userPrincipal.name(),
                userPrincipal.avatarUrl()
        );
    }
}
