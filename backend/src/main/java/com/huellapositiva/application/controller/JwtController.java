package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "JWT Service", description = "The JWT API")
@RequestMapping("/api/v1")
public class JwtController {

    @Autowired
    private final JwtService jwtService;

    @Operation(
            summary = "Request a new refresh token",
            description = "Give you a new refresh token",
            tags = "jwt"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "successful",
                            // TODO: poner ejemplo de token refresh
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content()
                    )
            }
    )

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
