package com.huellapositiva.infrastructure.security;

import com.huellapositiva.application.exception.InvalidJwtTokenException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private static final String ACCESS_TOKEN_PREFIX = "Bearer ";

    private final JwtService jwtService;

    private final AntPathRequestMatcher[] authAllowList;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtService jwtService, AntPathRequestMatcher[] authAllowList) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.authAllowList = authAllowList;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(ACCESS_TOKEN_PREFIX)) {
            if (Arrays.stream(authAllowList).anyMatch(matcher -> matcher.matches(req))) {
                chain.doFilter(req, res);
            } else {
                res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        UsernamePasswordAuthenticationToken authentication;
        try {
            authentication = getAuthentication(authHeader);
        } catch (InvalidJwtTokenException e) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String authHeader) throws InvalidJwtTokenException {
        String jwtToken = authHeader.replace(ACCESS_TOKEN_PREFIX, "");
        Pair<String, List<String>> userDetails = jwtService.getUserDetails(jwtToken);
        Collection<SimpleGrantedAuthority> authorities = userDetails.getSecond().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(userDetails.getFirst(), null, authorities);
    }
}
