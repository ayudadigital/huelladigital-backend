package com.huellapositiva.infrastructure.documentation;

import com.huellapositiva.application.dto.AuthenticationRequestDto;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .paths(new Paths()
                        .addPathItem("/api/v1/authentication/login", new PathItem()
                                .post(new Operation().addTagsItem("Login")
                                        .requestBody(
                                            new RequestBody()
                                                    .description("Mi variable")
                                                    .required(true)
                                                    .content(new Content()
                                                            .addMediaType("application/json", new MediaType()
                                                                    .example(new Example().value(createJSONAuthRequestDto()).externalValue(createJSONAuthRequestDto()))
                                                                    .examples(Map.of("Mierda", new Example().value(createJSONAuthRequestDto())))
                                                                    .schema(

                                                                            new ComposedSchema()
                                                                                    .addAllOfItem(new Schema()
                                                                                                    .addProperties("email", new Schema<AuthenticationRequestDto>().type("string").description("Email of user").example(new Example().externalValue("john.doe@huellapositiva.com")))
                                                                                                    .addProperties("password", new Schema<AuthenticationRequestDto>().type("string").description("Password of user").example(new Example().externalValue("myPassword")))
                                                                                            .addRequiredItem("email")
                                                                                            .addRequiredItem("password")
                                                                                            .name("AuthenticationRequestDto")
                                                                                            .title("AuthenticationRequestDto")

                                                                                            /*.name("miSchema")
                                                                                            .type("AuthenticationRequestDto")
                                                                                            .description("culo")*/
                                                                                    )


                                                                                    /*.name("miSchema")
                                                                                    .type("AuthenticationRequestDto")
                                                                                    .description("blablabuee")*/


                                                                            /*new Schema()
                                                                            .name("TuPutaMadre")
                                                                            .type("AuthenticationRequestDto")
                                                                            .description("blablabla")
                                                                            .addProperties("Autentication", new Schema<AuthenticationRequestDto>())*/
                                                                    )
                                                            )
                                                    ).extensions(Map.of("Hola", new Object()))
                                        )
                                        .addParametersItem(new Parameter()
                                                .name("Messirve")
                                                .schema(new Schema().type("Integer"))
                                        )
                                ).addParametersItem(new Parameter().name("MiVariableBonita").schema(new Schema().type("Integer")))
                                .summary("Mi propio método")
                        )
                )
                .servers(servers);

    }

    public String createJSONAuthRequestDto() {
        /*Map authRequestDto = new HashMap();
        authRequestDto.put("email", "john.doe@huellapositiva.com");
        authRequestDto.put("password", "mypassword");
        String jsonText = JSONValue.toJSONString(authRequestDto);*/
        ////////////////////////////////
        JSONObject authRequestDto = new JSONObject();
        authRequestDto.put("email", "john.doe@huellapositiva.com");
        authRequestDto.put("password", "mypassword");
        return authRequestDto.toJSONString();
    }

    /*@Bean
    public OpenAPI customConfiguration2() {
        return new OpenAPI()
                .components(new Components())
                .paths(new Paths()
                        .addPathItem("Mi path", new PathItem().$ref("/sadgsdg"))
                ).servers(servers);

    }*/
}