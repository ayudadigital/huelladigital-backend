package com.huellapositiva.infrastructure.documentation;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme( description = "Cookie inside your browser. Example: a6f5086d-af6b-464f-988b-7a604e46062b",name = "XSRF-TOKEN", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE)
@SecurityScheme( description = "Same value XSRF-TOKEN",name = "X-XSRF-TOKEN", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
public class OpenAPIConfig {

    @Value("${info.build.version}")
    private String buildVersion;

    @Bean
    public OpenAPI customConfiguration() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Huella Positiva")
                .description("Plataforma de voluntariado en Canarias")
                        .version(buildVersion));

    }
}