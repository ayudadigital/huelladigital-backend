package com.huellapositiva.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.infrastructure.VolunteerCredentialsDto;
import com.huellapositiva.infrastructure.exception.RequestAuthenticationUserException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

import static com.huellapositiva.infrastructure.security.SecurityConstants.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
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
            throw new RequestAuthenticationUserException("Failed to authenticate user.", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

        String authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String username = ((User) auth.getPrincipal()).getUsername();

        String accessToken = jwtUtils.createAccessToken(username, authorities);
        res.addHeader(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + accessToken);
        String refreshToken = jwtUtils.createRefreshToken(username, authorities);
        res.addHeader(REFRESH_HEADER_STRING, refreshToken);
    }
}
