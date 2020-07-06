package com.huellapositiva.infrastructure.documentation;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        name = "basicScheme"
)
public class OpenAPIConfig {

    @Value("${info.build.version}")
    private String buildVersion;

    private final List<Server> servers = List.of(
            new Server().url("http://localhost:8080/").description("Generated server url"),
            new Server().url("https://dev.huelladigital.ayudadigital.org/").description("Environment of pre-production"),
            new Server().url("https://plataforma.huelladigital.ayudadigital.org/").description("Production environment")
            );

    @Bean
    public OpenAPI customConfiguration() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Huella Positiva")
                        .description("Plataforma de voluntariado en Canarias")
                        .version(buildVersion))
                .servers(servers);

    }
}