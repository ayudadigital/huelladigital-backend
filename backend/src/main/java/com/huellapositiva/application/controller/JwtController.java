package com.huellapositiva.application.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.huellapositiva.infrastructure.security.JwtProperties;
import com.huellapositiva.infrastructure.security.JwtUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class JwtController {

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/refresh")
    public void refreshJwtToken(@RequestHeader("Refresh") String refreshToken, HttpServletResponse res) {
        if (refreshToken == null) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        DecodedJWT decodedRefreshToken;
        try {
            decodedRefreshToken = jwtUtils.decodeRefreshToken(refreshToken);
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

        String newAccessToken = jwtUtils.createAccessToken(decodedRefreshToken.getSubject(), decodedRefreshToken.getClaim("CLAIM_TOKEN").asString());

        res.addHeader(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + newAccessToken);
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
