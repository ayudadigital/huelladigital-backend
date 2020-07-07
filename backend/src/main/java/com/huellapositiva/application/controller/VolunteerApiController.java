package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.FailedToPersistUser;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/volunteers")
@Tag(name = "Volunteer", description = "The volunteer API")
public class VolunteerApiController {

    private final JwtService jwtService;

    private final JpaRoleRepository roleRepository;

    private final RegisterVolunteerAction registerVolunteerAction;

    @Operation(
            summary = "Register a new volunteer",
            description = "Register a new volunteer",
            tags = "user"
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, volunteer register successful"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, credentials are not valid",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, could not register. The user already exist on db",
                            content = @Content()
                    )
            }
    )


    @PostMapping("/register")
    @ResponseBody
    public JwtResponseDto registerVolunteer(@Validated @RequestBody CredentialsVolunteerRequestDto dto) {
        try {
            registerVolunteerAction.execute(dto);
            String username = dto.getEmail();
            List<String> roles = roleRepository.findAllByEmailAddress(username).stream().map(Role::getName).collect(Collectors.toList());
            return jwtService.create(username, roles);
        } catch (PasswordNotAllowed pna) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password doesn't meet minimum length");
        } catch (FailedToPersistUser ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the user");
        }
    }
}
