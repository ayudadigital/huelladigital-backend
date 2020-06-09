package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;

@Component
public class JwtUtils {

    @Autowired
    private JwtProperties jwtProperties;

    public DecodedJWT decodeAccessToken(String accessToken) {
        return JWT.require(HMAC512(jwtProperties.getAccessToken().getSecret().getBytes()))
                .build()
                .verify(accessToken.replace(ACCESS_TOKEN_PREFIX, ""));
    }

    public DecodedJWT decodeRefreshToken(String refreshToken) {
        return JWT.require(Algorithm.HMAC512(jwtProperties.getRefreshToken().getSecret().getBytes()))
                .build()
                .verify(refreshToken);
    }

    public String createAccessToken(String username, String authorities) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getAccessToken().getExpirationTime()))
                .withClaim("CLAIM_TOKEN", authorities)
                .sign(HMAC512(jwtProperties.getAccessToken().getSecret().getBytes()));
    }

    public String createRefreshToken(String username, String authorities) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getRefreshToken().getExpirationTime()))
                .withClaim("CLAIM_TOKEN", authorities)
                .sign(HMAC512(jwtProperties.getRefreshToken().getSecret().getBytes()));
    }
}
