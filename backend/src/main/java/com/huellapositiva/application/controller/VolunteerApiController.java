package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.ConflictPersistingUserException;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.actions.UploadCurriculumVitaeAction;
import com.huellapositiva.domain.model.entities.Volunteer;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Tag(name = "Volunteer", description = "The volunteer API")
@RequestMapping("/api/v1/volunteers")
public class VolunteerApiController {

    private final JwtService jwtService;

    private final JpaRoleRepository roleRepository;

    private final RegisterVolunteerAction registerVolunteerAction;

    private final UploadCurriculumVitaeAction uploadCurriculumVitaeAction;

    @Operation(
            summary = "Register a new volunteer",
            description = "Register a new volunteer",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
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
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public JwtResponseDto registerVolunteer(@Validated @RequestBody AuthenticationRequestDto dto, HttpServletResponse res) {
        try {
            Volunteer volunteer = registerVolunteerAction.execute(dto);
            String username = volunteer.getEmailAddress().toString();
            List<String> roles = roleRepository.findAllByEmailAddress(username).stream().map(Role::getName).collect(Collectors.toList());
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}").buildAndExpand(volunteer.getId().toString())
                    .toUri();
            res.addHeader(HttpHeaders.LOCATION, uri.toString());
            return jwtService.create(username, roles);
        } catch (PasswordNotAllowed pna) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password doesn't meet minimum length");
        } catch (ConflictPersistingUserException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not register the user");
        }
    }

    @Operation(
            summary = "Upload Curriculum Vitae",
            description = "Upload Curriculum Vitae as a volunteer",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, uploaded curriculum successfully"
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
    @PostMapping(path = "/cv-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("VOLUNTEER")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void uploadCurriculumVitae(@RequestPart("cv") MultipartFile cv,
                                      @AuthenticationPrincipal String contactPersonEmail) throws IOException {
        uploadCurriculumVitaeAction.execute(cv, contactPersonEmail);
    }
}
