package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class JwtController {

    @Autowired
    private final JwtService jwtService;

    @PostMapping("/refresh")
    public JwtResponseDto refreshJwtToken(@RequestBody String refreshToken, HttpServletResponse res) {
        try {
            return jwtService.refresh(refreshToken);
        } catch (InvalidJwtTokenException e) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
    }
}
