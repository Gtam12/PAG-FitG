package com.fitg.config.openapi;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration

public class SwaggerConfiguration {

    private static final String BEARER_SCHEME = "bearer-jwt";


    @Bean
    public OpenAPI swaggerApi() {
        return new OpenAPI().info(new Info().title("FitG").version("1.0").description("Calorie logging and activity tracking api")).
                components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme().
                        type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

}
