package SeCause.SeCause_be.domain.auth.service;

import SeCause.SeCause_be.domain.auth.dto.TokenReissueResult;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import SeCause.SeCause_be.global.apiPayload.code.GlobalErrorCode;
import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;
import SeCause.SeCause_be.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Transactional
    public TokenReissueResult reissue(String refreshToken) {
        User user = getUserByRefreshToken(refreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);
        user.updateRefreshToken(newRefreshToken);

        return new TokenReissueResult(newAccessToken, newRefreshToken);
    }

    private User getUserByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new GeneralException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        try {
            if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
                throw new GeneralException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
            }

            Long userId = jwtTokenProvider.getRefreshTokenUserId(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(GlobalErrorCode.INVALID_REFRESH_TOKEN));

            if (!refreshToken.equals(user.getRefreshToken())) {
                throw new GeneralException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
            }

            return user;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new GeneralException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
