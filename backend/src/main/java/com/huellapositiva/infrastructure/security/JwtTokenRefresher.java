package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.huellapositiva.infrastructure.security.SecurityConstants.*;

public class JwtTokenRefresher {

    @Value("${huellapositiva.security.jwt.expiration-time}")
    private static long accessExpirationTime;

    public static String getNewToken(HttpServletRequest req , HttpServletResponse res) {
        String refreshToken = req.getHeader("Refresh");

        if (refreshToken == null) {
            res.setStatus(401);
            return null;
        }

        Date expirationDate = JWT.require(Algorithm.HMAC512(REFRESH_TOKEN_SECRET.getBytes()))
                .build()
                .verify(refreshToken.replace(REFRESH_TOKEN_PREFIX, ""))
                .getExpiresAt();
        boolean hasRefreshTokenExpired = expirationDate.before(new Date());
        if (hasRefreshTokenExpired) {
            res.setStatus(401);
            return null;
        }

        String user = JWT.require(Algorithm.HMAC512(REFRESH_TOKEN_SECRET.getBytes()))
                .build()
                .verify(refreshToken.replace(REFRESH_TOKEN_PREFIX, ""))
                .getSubject();

        String newAccessToken = JWT.create()
                .withSubject(user)
                .withExpiresAt(new Date(System.currentTimeMillis() + accessExpirationTime))
                .sign(HMAC512(ACCESS_TOKEN_SECRET.getBytes()));

        return ACCESS_TOKEN_PREFIX + newAccessToken;
    }
}
