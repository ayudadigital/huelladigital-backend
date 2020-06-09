package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;
import static com.huellapositiva.infrastructure.security.SecurityConstants.REFRESH_TOKEN_PREFIX;

@Component
public class JwtTokenRefresher {

    @Autowired
    private JwtUtils jwtUtils;

    public String getNewToken(HttpServletRequest req , HttpServletResponse res) {
        String refreshToken = req.getHeader("Refresh");

        if (refreshToken == null) {
            res.setStatus(401);
            return null;
        }

        DecodedJWT decodedJWT = jwtUtils.decodeRefreshToken(refreshToken.replace(REFRESH_TOKEN_PREFIX, ""));
        Date expirationDate = decodedJWT.getExpiresAt();
        boolean hasRefreshTokenExpired = expirationDate.before(new Date());
        if (hasRefreshTokenExpired) {
            res.setStatus(401);
            return null;
        }

        String newAccessToken = jwtUtils.createAccessToken(decodedJWT.getSubject(), decodedJWT.getClaim("CLAIM_TOKEN").asString());

        return ACCESS_TOKEN_PREFIX + newAccessToken;
    }
}
