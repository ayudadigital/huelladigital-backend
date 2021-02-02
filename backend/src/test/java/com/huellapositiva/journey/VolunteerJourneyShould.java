package com.huellapositiva.journey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.util.TestData;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerJourneyShould {

    private static final String BASE_URL = "http://localhost:%s";

    @LocalServerPort
    private int port;

    @Autowired
    private TestData testData;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void should_login_after_registering_volunteer() {
        registerVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        loginVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
    }

    private JwtResponseDto registerVolunteer(String email, String password) {
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
                .email(email)
                .password(password)
                .build();

        return given()
                .baseUri(format(BASE_URL, port))
                .body(dto)
                .contentType("application/json")
                .accept("application/json")
              .when()
                .post("/api/v1/volunteers")
              .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("roles[0]", Matchers.equalTo(Roles.VOLUNTEER_NOT_CONFIRMED.toString()))
                .extract().body().as(JwtResponseDto.class);
    }

    private JwtResponseDto loginVolunteer(String email, String password) {
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
                .email(email)
                .password(password)
                .build();

        return given()
                .baseUri(format(BASE_URL, port))
                .body(dto)
                .contentType("application/json")
                .accept("application/json")
              .when()
                .post("/api/v1/authentication/login")
              .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("roles[0]", Matchers.equalTo(Roles.VOLUNTEER_NOT_CONFIRMED.toString()))
                .extract().body().as(JwtResponseDto.class);
    }

    // TODO Journey: Update profile information including email and retrieve with valid token
}
