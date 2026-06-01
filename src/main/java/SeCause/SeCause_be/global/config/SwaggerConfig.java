package SeCause.SeCause_be.global.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${springdoc.swagger-ui.prod-server}")
    private String prodServerUrl;

    @Bean
    public OpenAPI secauseAPI(){
        Info info = new Info()
                .title("SeCause API")
                .description("SeCause API 명세서")
                .version("1.0");

        String jwtSchemeName = "Bearer Authentication";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        Server prodServer = new Server().url(prodServerUrl).description("운영 서버 (HTTPS)");
        Server localServer = new Server().url("http://localhost:8080").description("로컬 테스트용 (HTTP)");

        return new OpenAPI()
                .addServersItem(prodServer)
                .addServersItem(localServer)
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
