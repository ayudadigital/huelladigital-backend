package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;
import static com.huellapositiva.infrastructure.security.SecurityConstants.SIGN_UP_URL;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final List<String> nonAuthenticatedUrls = List.of(SIGN_UP_URL, "/api/v1/email-confirmation/", "/api/v1/refresh");

    private final JwtUtils jwtUtils;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        super(authenticationManager);
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String accessHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (accessHeader == null || !accessHeader.startsWith(ACCESS_TOKEN_PREFIX)) {
            if (nonAuthenticatedUrls.stream().anyMatch(url -> req.getRequestURI().startsWith(url))) {
                chain.doFilter(req, res);
            } else {
                res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        UsernamePasswordAuthenticationToken authentication;
        try {
            authentication = getAuthentication(accessHeader);
        } catch (TokenExpiredException ex) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if(authentication == null){
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String accessToken) {
        DecodedJWT decodedJWT = jwtUtils.decodeAccessToken(accessToken);

        if (decodedJWT.getSubject() != null) {
            Collection<SimpleGrantedAuthority> authorities =
                    Arrays.stream(decodedJWT.getClaim("CLAIM_TOKEN").asString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(decodedJWT.getSubject(), null, authorities);
        }
        // FIXME Fail if cannot extract authentication from token
        return null;
    }
}
