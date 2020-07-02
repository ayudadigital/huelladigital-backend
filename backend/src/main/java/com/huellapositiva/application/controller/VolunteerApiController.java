package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.application.exception.FailedToPersistUser;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
public class VolunteerApiController {

    private final JwtService jwtService;

    private final JpaRoleRepository roleRepository;

    private final RegisterVolunteerAction registerVolunteerAction;

    @ApiOperation(notes = "Register a volunteer", value = "Request Volunteer's Credentials", nickname = "registerVolunteer" )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Volunteer is register in database successfully"),
            @ApiResponse(code = 409, message = "Volunteer already exist in database")
    })
    @PostMapping("/register")
    @ResponseBody
    public JwtResponseDto registerVolunteer(@ApiParam(value = "User info DTO", required = true) @Validated @RequestBody CredentialsVolunteerRequestDto dto) {
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
