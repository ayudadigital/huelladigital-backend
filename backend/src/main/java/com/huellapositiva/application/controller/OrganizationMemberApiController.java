package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.CredentialsOrganizationMemberRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.exception.FailedToPersistUser;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.actions.RegisterOrganizationMemberAction;
import com.huellapositiva.infrastructure.orm.entities.Role;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Tag(name = "Organization Employee", description = "The organization employee API")
@RequestMapping("/api/v1/organizationmember")
public class OrganizationMemberApiController {

    private final RegisterOrganizationMemberAction registerOrganizationMemberAction;

    private final JwtService jwtService;

    private final JpaRoleRepository jpaRoleRepository;

    @Operation(
            summary = "Register a new organization employee",
            description = "Register a new organization employee",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Ok, organization employee has been registered successfully."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the organization.",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, could not register the user due to a constraint violation.",
                            content = @Content()
                    )
            }
    )
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public JwtResponseDto registerOrganizationMember(@RequestBody CredentialsOrganizationMemberRequestDto dto, HttpServletResponse res) {
        try {
            Integer id = registerOrganizationMemberAction.execute(dto);
            String username = dto.getEmail();
            List<String> roles = jpaRoleRepository.findAllByEmailAddress(username).stream().map(Role::getName).collect(Collectors.toList());
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(id)
                    .toUri();
            res.addHeader(HttpHeaders.LOCATION, uri.toString());
            return jwtService.create(username, roles);
        } catch (PasswordNotAllowed pna) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password doesn't meet minimum length");
        } catch (FailedToPersistUser ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the user");
        }
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto getMember(@PathVariable Integer id) {
        throw new UnsupportedOperationException();
    }
}
