package com.huellapositiva.application.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.huellapositiva.infrastructure.security.SecurityConstants.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    @Value("${huellapositiva.security.jwt.expiration-time}")
    private long accessExpirationTime;

    @GetMapping(value = "/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> refreshJwtToken(@RequestHeader("Refresh") String refreshToken, @RequestHeader("Authorization") String accessToken) {
        Date expirationDate = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                .build()
                .verify(refreshToken.replace(REFRESH_TOKEN_PREFIX, ""))
                .getExpiresAt();

        boolean hasExpired = expirationDate.before(new Date());
        if (hasExpired) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                .build()
                .verify(accessToken.replace(ACCESS_TOKEN_PREFIX, ""))
                .getSubject();

        String newAccessToken = JWT.create()
                .withSubject(user)
                .withExpiresAt(new Date(System.currentTimeMillis()+accessExpirationTime))
                .sign(HMAC512(SECRET.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.add(ACCESS_HEADER_STRING, ACCESS_TOKEN_PREFIX + newAccessToken);


        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
}
