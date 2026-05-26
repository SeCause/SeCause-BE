package SeCause.SeCause_be.healthCheck;

import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/health-check")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.onSuccess("헬스 체크가 완료됐습니다.", "Server is running!");
    }
}
