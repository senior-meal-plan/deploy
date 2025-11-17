package io.github.tlsdla1235.seniormealplan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        // Security Scheme (HTTP Bearer JWT)
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .info(new Info()
                        .title("Senior Meal Plan API")
                        .version("v1")
                        .description("JWT 인증이 필요한 API 문서"))
                // 전역 보안 요구조건(선택: 전역 적용)
                .schemaRequirement("BearerAuth", bearer)
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
