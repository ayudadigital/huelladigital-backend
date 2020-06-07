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

    @Data
    public static class AccessToken {
        private String secret;
        private long expirationTime;
    }

    @Data
    public static class RefreshToken {
        private String secret;
        private long expirationTime;
    }
}
