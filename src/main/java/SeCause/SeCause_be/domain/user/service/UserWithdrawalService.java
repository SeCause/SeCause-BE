package SeCause.SeCause_be.domain.user.service;

import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import SeCause.SeCause_be.domain.user.repository.UserWithdrawalRepository;
import SeCause.SeCause_be.domain.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWithdrawalService {

    private final UserValidator userValidator;
    private final UserRepository userRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;

    @Transactional
    public void withdraw(Long userId) {
        // 분석 요청 생성도 같은 회원 행을 잠가 삭제 대상이 도중에 늘어나지 않도록 한다.
        User user = userValidator.validateUserForUpdate(userId);
        userRepository.flush();
        userWithdrawalRepository.deleteOwnedData(userId);
        userRepository.delete(user);
    }
}
