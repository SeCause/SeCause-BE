package SeCause.SeCause_be.global.security;

import SeCause.SeCause_be.domain.user.entity.User;

public record UserPrincipal(
        Long userId,
        String email,
        String name,
        String avatarUrl
) {

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl()
        );
    }
}
