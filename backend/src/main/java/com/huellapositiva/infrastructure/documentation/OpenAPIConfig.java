package com.huellapositiva.infrastructure.documentation;

import com.huellapositiva.application.dto.AuthenticationRequestDto;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "Bearer",
        name = "accessToken"
)
@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "Bearer",
        name = "refreshToken"
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
                .addTagsItem(new Tag().name("Login")
                        .description("Login hide by Spring Security"))
                .paths(addLoginPath()
                )
                .servers(servers);
    }

    private Paths addLoginPath() {
        return new Paths()
                .addPathItem("/api/v1/authentication/login", new PathItem()
                        .post(new Operation().addTagsItem("Login")
                                .requestBody(new RequestBody()
                                        .required(true)
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new ComposedSchema()
                                                                .addProperties("email", new Schema<AuthenticationRequestDto>()
                                                                        .type("string").description("Email of user")
                                                                        .example(new Example().value("john.doe@huellapositiva.com").getValue()))
                                                                .addProperties("password", new Schema<AuthenticationRequestDto>()
                                                                        .type("string").description("Password of user")
                                                                        .example(new Example().value("myPassword").getValue()))
                                                                .addRequiredItem("email")
                                                                .addRequiredItem("password")
                                                                .name("AuthenticationRequestDto")
                                                                .title("AuthenticationRequestDto")

                                                        )
                                                )
                                        )
                                ).responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse()
                                                .description("You have successfully logged in."))
                                        .addApiResponse("400", new ApiResponse()
                                                .description("The email or password field is incorrect or does not match."))
                                        .addApiResponse("401", new ApiResponse()
                                                .description("The login has failed."))
                                        .addApiResponse("403", new ApiResponse()
                                                .description("The email or password field is incorrect or does not match."))
                                        .addApiResponse("500", new ApiResponse()
                                                .description("Internal server error, could not fetch the user data due to a connectivity issue.")))
                        )
                );
    }
}