package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
            description = "Returns access token and refresh token when you give valid refresh token",
            tags = "jwt",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE,required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Is necessary generate a refresh token value previously, and copy here",
                    content = @Content(examples = @ExampleObject(value = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..qPxdcrmy04En8lrP.JtPX2HK7gApQHR5R_8DjoHZq703Mpl2eiBl4GN-SrEb72lPBeb_CRULKxGAMveQ5WHaHHZJ9TC6-GA37v7bHLPSQrrMZonZCCUhYNl2afPpzYkHwJOKeTRLl3Kx339VJLOhCgtyhxP5Ca_oWW0Um4ke6XYo6pK1uNPncwXmivdvOmQzGEMHslNehJpcdxUkwn7Qw7TU1tUEfDqBUp5c8jOtSaPF6Nui12aKlHrFKn_dKUsDIdhTkBIROipec9wriyF_fMW3pQ34TiYz48aubvmqPAkVWrOLB0BfDapG0LsRsvoAWyr5e9HHa48SYvnb-mKXmhOS5-K8LlOoOLMb6AJuCgQ.IUVXVJP1CcDzwr4mnE_Psw"))
            )
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, created new JwtResponseDto",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDto.class))
                            }
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request, required request body is missing",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized, you need a valid refresh token or XSRF-TOKEN",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden, you need a valid XSRF-TOKEN",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping("/authentication/refresh")
    public JwtResponseDto refreshJwtToken(@Parameter(description = "refresh token value")
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
