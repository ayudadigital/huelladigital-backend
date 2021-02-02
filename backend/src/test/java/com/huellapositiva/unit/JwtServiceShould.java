package com.huellapositiva.unit;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtProperties;
import com.huellapositiva.infrastructure.security.JwtProperties.Encryption;
import com.huellapositiva.infrastructure.security.JwtProperties.Signature;
import com.huellapositiva.infrastructure.security.JwtService;
import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER;
import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;
import static com.huellapositiva.infrastructure.security.JwtProperties.AccessToken;
import static com.huellapositiva.infrastructure.security.JwtProperties.RefreshToken;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtServiceShould {

    private final JwtProperties jwtProperties = new JwtProperties(
            new AccessToken(5000L),
            new RefreshToken(3000000L),
            new Signature("ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789"),
            new Encryption("ABCDEF0123456789ABCDEF0123456789"));

    @Mock
    private JpaRoleRepository roleRepository;

    private JwtService jwtService;

    @BeforeEach
    void beforeEach() throws JOSEException {
        jwtService = new JwtService(jwtProperties, roleRepository);
        jwtService.init();
    }

    @Test
    void revoked_token_should_throw_exception_when_decoded() {
        String username = "user";
        String revokedAccessToken = jwtService.create(username, List.of(VOLUNTEER_NOT_CONFIRMED.toString())).getAccessToken();

        await().atMost(2, SECONDS).pollDelay(100, MILLISECONDS).untilAsserted(() ->
                assertThrows(InvalidJwtTokenException.class, () -> {
                    jwtService.create(username, List.of(VOLUNTEER.toString()));
                    jwtService.getUserDetails(revokedAccessToken);
                })
        );
    }

    @Test
    void refreshing_a_token_should_reload_roles_from_database() throws InvalidJwtTokenException {
        String accountId = UUID.randomUUID().toString();
        String staleRole = VOLUNTEER_NOT_CONFIRMED.toString();
        String refreshToken = jwtService.create(accountId, List.of(staleRole)).getRefreshToken();
        String latestRole = VOLUNTEER.toString();
        when(roleRepository.findAllByAccountId(accountId)).thenReturn(List.of(Role.builder().name(latestRole).build()));

        String refreshedAccessToken = jwtService.refresh(refreshToken).getAccessToken();

        String refreshedAccessTokenRole = jwtService.getUserDetails(refreshedAccessToken).getSecond().get(0);
        assertThat(refreshedAccessTokenRole, is(latestRole));
    }

    @Test
    void creating_tokens_should_return_roles_in_access_token_and_no_roles_in_refresh_tokens() throws InvalidJwtTokenException {
        JwtResponseDto jwtResponseDto = jwtService.create(DEFAULT_EMAIL, List.of(VOLUNTEER_NOT_CONFIRMED.toString()));

        String accessToken = jwtResponseDto.getAccessToken();
        List<String> accessTokenRoles = jwtService.getUserDetails(accessToken).getSecond();
        assertThat(accessTokenRoles, hasSize(1));
        String refreshToken = jwtResponseDto.getRefreshToken();
        List<String> refreshTokenRoles = jwtService.getUserDetails(refreshToken).getSecond();
        assertThat(refreshTokenRoles, hasSize(0));
    }
}
