package com.huellapositiva.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwaggerShould {

    @LocalServerPort
    private int port;

    @Test
    void swagger_endpoint_should_return_200() {
        given().when()
                .redirects().follow(true)
                .get("http://localhost:" + port + "/swagger-ui")
                .then().assertThat().statusCode(HttpStatus.OK.value());
    }
}
