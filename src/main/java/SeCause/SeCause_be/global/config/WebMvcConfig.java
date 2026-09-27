package SeCause.SeCause_be.global.config;

import SeCause.SeCause_be.global.security.internal.InternalCallbackInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final InternalCallbackInterceptor internalCallbackInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalCallbackInterceptor)
                .addPathPatterns("/api/internal/**");
    }
}
