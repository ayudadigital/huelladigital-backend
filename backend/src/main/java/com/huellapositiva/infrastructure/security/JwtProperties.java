package com.huellapositiva.infrastructure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("jwt")
public class JwtProperties {

    private AccessToken accessToken;
    private RefreshToken refreshToken;
    private String secret;

    @Data
    public static class AccessToken {
        private long expirationTime;
    }

    @Data
    public static class RefreshToken {
        private long expirationTime;
    }
}
