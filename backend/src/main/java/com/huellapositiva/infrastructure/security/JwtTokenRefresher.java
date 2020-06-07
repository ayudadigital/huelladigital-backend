package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;
import static com.huellapositiva.infrastructure.security.SecurityConstants.REFRESH_TOKEN_PREFIX;

@Component
public class JwtTokenRefresher {

    @Autowired
    private JwtProperties jwtProperties;

    public String getNewToken(HttpServletRequest req , HttpServletResponse res) {
        String refreshToken = req.getHeader("Refresh");

        if (refreshToken == null) {
            res.setStatus(401);
            return null;
        }

        Date expirationDate = JWT.require(Algorithm.HMAC512(jwtProperties.getRefreshToken().getSecret().getBytes()))
                .build()
                .verify(refreshToken.replace(REFRESH_TOKEN_PREFIX, ""))
                .getExpiresAt();
        boolean hasRefreshTokenExpired = expirationDate.before(new Date());
        if (hasRefreshTokenExpired) {
            res.setStatus(401);
            return null;
        }

        String user = JWT.require(Algorithm.HMAC512(jwtProperties.getRefreshToken().getSecret().getBytes()))
                .build()
                .verify(refreshToken.replace(REFRESH_TOKEN_PREFIX, ""))
                .getSubject();

        String newAccessToken = JWT.create()
                .withSubject(user)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getAccessToken().getExpirationTime()))
                .sign(HMAC512(jwtProperties.getAccessToken().getSecret().getBytes()));

        return ACCESS_TOKEN_PREFIX + newAccessToken;
    }
}
