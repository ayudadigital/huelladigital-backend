package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.huellapositiva.infrastructure.security.SecurityConstants.*;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final UserDetailsService userDetailsService;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String accessHeader = req.getHeader(ACCESS_HEADER_STRING);
        if (accessHeader == null || !accessHeader.startsWith(ACCESS_TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = null;
        try {
            authentication = getAuthentication(accessHeader);
        } catch(TokenExpiredException ex) {
            String newAccessToken = JwtTokenRefresher.getNewToken(req, res);
            if (newAccessToken != null) {
                authentication = getAuthentication(newAccessToken);
                res.addHeader(ACCESS_HEADER_STRING, newAccessToken);
            }
        }
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(req, res);
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String accessToken) {
        String user = JWT.require(Algorithm.HMAC512(ACCESS_TOKEN_SECRET.getBytes()))
                .build()
                .verify(accessToken.replace(ACCESS_TOKEN_PREFIX, ""))
                .getSubject();

        if (user != null) {
            UserDetails details = userDetailsService.loadUserByUsername(user);
            return new UsernamePasswordAuthenticationToken(user, null, details.getAuthorities());
        }
        return null;
    }
}
