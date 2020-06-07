package com.huellapositiva.application.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.huellapositiva.infrastructure.security.JwtProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class JwtController {

    @Autowired
    private JwtProperties jwtProperties;

    @GetMapping("/refresh")
    public void refreshJwtToken(@RequestHeader("Refresh") String refreshToken, HttpServletResponse res) {
        if (refreshToken == null) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        DecodedJWT jwt;
        try {
            jwt = JWT.require(Algorithm.HMAC512(jwtProperties.getRefreshToken().getSecret().getBytes()))
                    .build()
                    .verify(refreshToken);
        } catch (TokenExpiredException ex) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception ex) {
            log.error("Failed to verify refresh token: " + refreshToken, ex);
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String newToken = JWT.create()
                .withSubject(jwt.getSubject())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getAccessToken().getExpirationTime()))
                .sign(HMAC512(jwtProperties.getAccessToken().getSecret().getBytes()));

        res.addHeader(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + newToken);
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
