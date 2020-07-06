package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
            description = "Returns access token and refresh token when you give valid refresh token",
            tags = "jwt",
            security = {
                    @SecurityRequirement(name = "XSRF-TOKEN"),
                    @SecurityRequirement(name = "X-XSRF-TOKEN")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "successful",
                            content = {
                                    @Content(mediaType = "application/json",
                                            schema = @Schema(
                                                    implementation = JwtResponseDto.class
                                            )
                                    )
                            }
                    ),

                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden",
                            content = @Content()
                    )
            }
    )
    @PostMapping("/refresh")
    public JwtResponseDto refreshJwtToken(@Parameter(description = "refresh token value", example = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..qPxdcrmy04En8lrP.JtPX2HK7gApQHR5R_8DjoHZq703Mpl2eiBl4GN-SrEb72lPBeb_CRULKxGAMveQ5WHaHHZJ9TC6-GA37v7bHLPSQrrMZonZCCUhYNl2afPpzYkHwJOKeTRLl3Kx339VJLOhCgtyhxP5Ca_oWW0Um4ke6XYo6pK1uNPncwXmivdvOmQzGEMHslNehJpcdxUkwn7Qw7TU1tUEfDqBUp5c8jOtSaPF6Nui12aKlHrFKn_dKUsDIdhTkBIROipec9wriyF_fMW3pQ34TiYz48aubvmqPAkVWrOLB0BfDapG0LsRsvoAWyr5e9HHa48SYvnb-mKXmhOS5-K8LlOoOLMb6AJuCgQ.IUVXVJP1CcDzwr4mnE_Psw")
                                          @RequestBody String refreshToken, HttpServletResponse res) {
        try {
            return jwtService.refresh(refreshToken);
        } catch (InvalidJwtTokenException e) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
    }
}
