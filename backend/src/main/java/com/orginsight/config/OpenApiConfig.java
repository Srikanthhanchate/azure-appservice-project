package com.orginsight.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orgInsightOpenApi() {
        final String bearerSchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("OrgInsight AI API")
                        .description("REST API for the OrgInsight AI enterprise platform "
                                + "(Employees, Projects, Knowledge Hub, Reports, Settings, Admin, AI Insights)")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .components(new Components().addSecuritySchemes(bearerSchemeName,
                        new SecurityScheme()
                                .name(bearerSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
