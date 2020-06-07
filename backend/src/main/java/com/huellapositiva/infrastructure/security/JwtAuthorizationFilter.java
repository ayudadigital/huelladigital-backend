package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.http.HttpHeaders;
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
import java.util.List;

import static com.huellapositiva.infrastructure.security.SecurityConstants.*;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final UserDetailsService userDetailsService;

    private final JwtTokenRefresher jwtTokenRefresher;

    private List<String> nonAuthenticatedUrls = List.of(SIGN_UP_URL, "/api/v1/email-confirmation/");

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtTokenRefresher jwtTokenRefresher) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
        this.jwtTokenRefresher = jwtTokenRefresher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String accessHeader = req.getHeader(ACCESS_HEADER_STRING);
        if (accessHeader == null || !accessHeader.startsWith(ACCESS_TOKEN_PREFIX)) {
            if (nonAuthenticatedUrls.stream().anyMatch(url -> req.getRequestURI().startsWith(url))) {
                chain.doFilter(req, res);
            } else {
                res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        UsernamePasswordAuthenticationToken authentication = null;
        try {
            authentication = getAuthentication(accessHeader);
        } catch (TokenExpiredException ex) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
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
        // FIXME Fail if cannot extract authentication from token
        return null;
    }
}
