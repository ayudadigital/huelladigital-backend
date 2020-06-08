package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Component
public class JwtUtils {

    @Autowired
    private JwtProperties jwtProperties;

    public String createAccessToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getAccessToken().getExpirationTime()))
                .sign(HMAC512(jwtProperties.getAccessToken().getSecret().getBytes()));
    }

    public String createRefreshToken(User user) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getRefreshToken().getExpirationTime()))
                .sign(HMAC512(jwtProperties.getRefreshToken().getSecret().getBytes()));
    }
}
