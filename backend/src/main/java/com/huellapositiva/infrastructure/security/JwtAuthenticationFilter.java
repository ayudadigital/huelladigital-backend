package com.huellapositiva.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.infrastructure.VolunteerCredentialsDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final JwtService jwtService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        setFilterProcessesUrl("/api/v1/volunteers/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) {
        try {
            VolunteerCredentialsDto userCredentials = objectMapper.readValue(req.getInputStream(), VolunteerCredentialsDto.class);
            UserDetails user = userDetailsService.loadUserByUsername(userCredentials.getEmail());
            Authentication authentication = new UsernamePasswordAuthenticationToken(userCredentials.getEmail(), userCredentials.getPassword(), user.getAuthorities());
            return authenticationManager.authenticate(authentication);
        } catch (IOException e) {
            throw new BadCredentialsException("Failed to authenticate user.", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth)
            throws IOException, ServletException {
        String username = ((User) auth.getPrincipal()).getUsername();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());
        JwtResponseDto jwtResponseDto = jwtService.create(username, roles);
        String responseBody = objectMapper.writeValueAsString(jwtResponseDto);
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(responseBody);
        res.getWriter().flush();
        res.getWriter().close();
    }
}
