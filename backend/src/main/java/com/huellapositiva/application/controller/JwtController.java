package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
            description = "Give you a new refresh token",
            tags = "jwt"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "successful",
                            content = @Content(
                                    schema = @Schema(
                                            example = "{\n\"refreshToken\": \"eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiZGlyIn0..UZg_dFpW0JJp0nul.GyaID9YuFwRcUkH7gagM9242657Px7474WH3MWJ3lrQho_RspGNDGlaOGYiZzaU0dHHufqC_zL7q7I0zvNTbVbjTrxCtrY5UjqH42Z7VLg_BsLy2JXiQDVd2VZ-zUabiifoigW3l_towpywAhpK0thvkrXUK4DlKGLuDJmKe7PNiOVkRAoBSU31GumMWU2mJxA97bav0hvYtKdWh9sF7WFv8dOrXX6jPGREj3C1Z3nVb5EGl2ub_mwANYNo97jvcSfSYEuLgPMZiAQHfzGAtsu2tOlctYPz8JJLao5nO4GTVzQ.E1MR54BpO6CxHXzjU5ED-g\"\n}"
                                    )
                            )
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
