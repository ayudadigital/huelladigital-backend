package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.application.exception.FailedToPersistUser;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.actions.RegisterOrganizationAction;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.service.OrganizationService;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationApiController {

    private final JwtService jwtService;

    private final OrganizationService organizationService;

    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    private final RegisterOrganizationAction registerOrganizationAction;

    @PostMapping("/register")
    @ResponseBody
    public void registerOrganization(@RequestBody OrganizationRequestDto dto, HttpServletRequest req) {
        try {
            String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
            String employeeEmail = jwtService.getUserDetails(authHeader.replace("Bearer ", "")).getFirst();
            OrganizationEmployee organizationEmployee = jpaOrganizationEmployeeRepository.findByEmail(employeeEmail)
                    .orElseThrow( () -> new RuntimeException("Could not retrieve the organization employee by his email."));
            registerOrganizationAction.execute(dto, organizationEmployee);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the user");
        }
    }
}
