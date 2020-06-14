package com.huellapositiva.unit;

import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.security.JwtProperties;
import com.huellapositiva.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.huellapositiva.infrastructure.security.JwtProperties.AccessToken;
import static com.huellapositiva.infrastructure.security.JwtProperties.RefreshToken;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtServiceShould {

    private final JwtProperties jwtProperties = new JwtProperties(new AccessToken(5000L), new RefreshToken(3000000L), "secret");

    private JwtService jwtService;

    @BeforeEach
    void beforeEach() {
        jwtService = new JwtService(jwtProperties);
    }

    @Test
    void revoked_token_should_throw_exception_when_decoded() {
        String username = "user";
        String revokedAccessToken = jwtService.create(username, List.of("ROLE1")).getAccessToken();

        await().atMost(2, SECONDS).pollDelay(100, MILLISECONDS).untilAsserted(() ->
                assertThrows(InvalidJwtTokenException.class, () -> {
                    jwtService.create(username, List.of("ROLE2"));
                    jwtService.getUserDetails(revokedAccessToken);
                })
        );
    }
}
