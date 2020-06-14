package com.huellapositiva.unit;

import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtProperties;
import com.huellapositiva.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.huellapositiva.infrastructure.security.JwtProperties.AccessToken;
import static com.huellapositiva.infrastructure.security.JwtProperties.RefreshToken;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceShould {

    private final JwtProperties jwtProperties = new JwtProperties(new AccessToken(5000L), new RefreshToken(3000000L), "secret");

    @Mock
    private JpaRoleRepository roleRepository;

    private JwtService jwtService;

    @BeforeEach
    void beforeEach() {
        jwtService = new JwtService(jwtProperties, roleRepository);
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

    @Test
    void refreshing_a_token_should_reload_roles_from_database() throws InvalidJwtTokenException {
        String username = "user";
        String staleRole = "ROLE1";
        String refreshToken = jwtService.create(username, List.of(staleRole)).getRefreshToken();
        String latestRole = "ROLE2";
        when(roleRepository.findAllByEmailAddress(username)).thenReturn(List.of(Role.builder().name(latestRole).build()));

        String refreshedAccessToken = jwtService.refresh(refreshToken).getAccessToken();

        String refreshedAccessTokenRole = jwtService.getUserDetails(refreshedAccessToken).getSecond().get(0);
        assertThat(refreshedAccessTokenRole, is(latestRole));
    }
}
