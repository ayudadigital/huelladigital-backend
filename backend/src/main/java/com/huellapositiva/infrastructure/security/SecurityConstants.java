package com.huellapositiva.infrastructure.security;

public class SecurityConstants {
    public static final String SECRET = "SecretKeyToGenJWTs";
    public static final long EXPIRATION_TIME = 3_600_000; //1 hour
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_UP_URL = "/api/v1/volunteers/register";
    public static final String LOGIN_URL = "/api/v1/volunteers/login";
}
