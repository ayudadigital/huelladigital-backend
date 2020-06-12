package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Slf4j
@AllArgsConstructor
@Component
public class JwtService {

    private static final String ROLE_CLAIM = "roles";

    @Autowired
    private final JwtProperties jwtProperties;

    public JwtResponseDto refresh(String refreshToken) throws InvalidJwtTokenException {
        Pair<String, List<String>> userDetails = getUserDetails(refreshToken);
        return create(userDetails.getFirst(), userDetails.getSecond());
    }

    public JwtResponseDto create(String username, List<String> roles) {
        String newAccessToken = createToken(username, roles, jwtProperties.getAccessToken().getExpirationTime());
        String newRefreshToken = createToken(username, roles, jwtProperties.getRefreshToken().getExpirationTime());
        return new JwtResponseDto(newAccessToken, newRefreshToken);
    }

    public Pair<String, List<String>> getUserDetails(String token) throws InvalidJwtTokenException {
        DecodedJWT decodedRefreshToken = decodeToken(token);
        String username = decodedRefreshToken.getSubject();
        List<String> roles = decodedRefreshToken.getClaim(ROLE_CLAIM).asList(String.class);
        return Pair.of(username, roles);
    }

    private DecodedJWT decodeToken(String token) throws InvalidJwtTokenException {
        try {
            return JWT.require(Algorithm.HMAC512(jwtProperties.getSecret().getBytes()))
                    .build()
                    .verify(token);
        } catch (Exception e) {
            log.error("Invalid token: {}", token, e);
            throw new InvalidJwtTokenException("Unable to decode token: " + token, e);
        }
    }

    private String createToken(String username, List<String> roles, long duration) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + duration))
                .withArrayClaim(ROLE_CLAIM, roles.toArray(new String[0]))
                .sign(HMAC512(jwtProperties.getSecret().getBytes()));
    }
}
