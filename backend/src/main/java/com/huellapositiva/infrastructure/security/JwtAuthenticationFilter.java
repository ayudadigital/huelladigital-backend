package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.infrastructure.VolunteerCredentialsDto;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.huellapositiva.infrastructure.security.SecurityConstants.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private JwtProperties jwtProperties;


    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtProperties = jwtProperties;
        setFilterProcessesUrl(LOGIN_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            VolunteerCredentialsDto reqUserCredentials = new ObjectMapper()
                    .readValue(req.getInputStream(), VolunteerCredentialsDto.class);

            UserDetails user = userDetailsService.loadUserByUsername(reqUserCredentials.getEmail());

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            reqUserCredentials.getEmail(),
                            reqUserCredentials.getPassword(),
                            user.getAuthorities())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to authenticate user.", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        String token = JWT.create()
                .withSubject(((User) auth.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getAccessToken().getExpirationTime()))
                .sign(HMAC512(jwtProperties.getAccessToken().getSecret().getBytes()));
        res.addHeader(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + token);

        String refreshToken = JWT.create()
                .withSubject(((User) auth.getPrincipal()).getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getRefreshToken().getExpirationTime()))
                .sign(HMAC512(jwtProperties.getRefreshToken().getSecret().getBytes()));
        res.addHeader(REFRESH_HEADER_STRING, refreshToken);
    }
}
