package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.OrganizationRequestDto;
import com.huellapositiva.domain.actions.RegisterOrganizationAction;
import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationEmployeeRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@Tag(name = "Organizations", description = "The organization API")
@RequestMapping("/api/v1/organizations")
public class OrganizationApiController {

    private final JwtService jwtService;

    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    private final RegisterOrganizationAction registerOrganizationAction;

    @Operation(
            summary = "Register a new organization",
            description = "Register a new organization and link it to the logged employee",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, organization registered successful"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, could not register the organization.",
                            content = @Content()
                    )
            }
    )
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
