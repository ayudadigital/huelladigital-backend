package com.huellapositiva.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties("jwt")
public class JwtProperties {

    private AccessToken accessToken;
    private RefreshToken refreshToken;
    private Signature signature;
    private Encryption encryption;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessToken {
        private long expirationTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshToken {
        private long expirationTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Signature {
        private String secret;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Encryption {
        private String secret;
    }
}
