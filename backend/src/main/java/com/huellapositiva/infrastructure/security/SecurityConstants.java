package com.huellapositiva.infrastructure.security;

public class SecurityConstants {
    public static final String ACCESS_TOKEN_SECRET = "SecretKeyToGenJWTs";
    public static final String REFRESH_TOKEN_SECRET = "SecretKeyToGenRefreshJWT";
    public static final long ACCESS_EXPIRATION_TIME = 300_000; //5 minutes
    public static final long REFRESH_EXPIRATION_TIME = 3_600_000; //1 hour
    public static final String ACCESS_TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_PREFIX = "Basic ";
    public static final String ACCESS_HEADER_STRING = "Authorization";
    public static final String REFRESH_HEADER_STRING = "Refresh";
    public static final String SIGN_UP_URL = "/api/v1/volunteers/register";
    public static final String LOGIN_URL = "/api/v1/volunteers/login";
}
