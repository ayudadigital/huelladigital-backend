package com.huellapositiva.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.application.exception.ConflictPersistingUserException;
import com.huellapositiva.application.exception.EmailAlreadyExistsException;
import com.huellapositiva.application.exception.PasswordNotAllowedException;
import com.huellapositiva.application.exception.SomeFieldIsNullException;
import com.huellapositiva.domain.actions.*;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.exception.MatchingEmailException;
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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "Volunteer", description = "The volunteer API")
@RequestMapping("/api/v1/volunteers")
public class VolunteerApiController {

    private final JwtService jwtService;

    private final JpaRoleRepository roleRepository;

    private final RegisterVolunteerAction registerVolunteerAction;

    private final UploadCurriculumVitaeAction uploadCurriculumVitaeAction;

    private final FetchVolunteerProfileAction fetchVolunteerProfileAction;

    private final UpdateVolunteerProfileAction updateVolunteerProfileAction;

    private final UploadPhotoAction uploadPhotoAction;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
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
        } catch (PasswordNotAllowedException pna) {
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
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(path = "/cv-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("VOLUNTEER")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void uploadCurriculumVitae(@RequestPart("cv") MultipartFile cv,
                                      @AuthenticationPrincipal String contactPersonEmail) throws IOException {
        try {
            uploadCurriculumVitaeAction.execute(cv, contactPersonEmail);
        } catch (EmptyFileException ex){
            log.error("There is not any cv attached or is empty.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @Operation(
            summary = "Upload user Photo",
            description = "Upload user Photo to profile",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, uploaded photo successfully"
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
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(path = "/photo-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("VOLUNTEER")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadPhoto(@RequestPart("photo") MultipartFile photo,
                                      @AuthenticationPrincipal String volunteerEmail) throws IOException {
        try {
            uploadPhotoAction.execute(photo, volunteerEmail);
        } catch (EmptyFileException ex){
            log.error("There is not any photo attached or is empty.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @Operation(
            summary = "Return user profile information",
            description = "Return user profile information",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, return full information user profile"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, credentials are not valid",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @GetMapping("/fetchProfileInformation")
    @RolesAllowed("VOLUNTEER")
    @ResponseStatus(HttpStatus.OK)
    public ProfileDto fetchProfileInformation(@AuthenticationPrincipal String volunteerEmail) {
        return fetchVolunteerProfileAction.execute(volunteerEmail);
    }

    @Operation(
            summary = "Update user profile information",
            description = "Update user profile information",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Ok, return full information user profile"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, credentials are not valid",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not found, some field is mandatory",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the new email already match with other email in db",
                            content = @Content()
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(path = "/updateProfileInformation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed("VOLUNTEER")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfileInformation(@RequestPart("dto") MultipartFile dtoProfileInformation,
                                        @AuthenticationPrincipal String contactPersonEmail
                                         ) throws IOException {
        ProfileDto profileDto = objectMapper.readValue(dtoProfileInformation.getBytes(), ProfileDto.class);
        try {
            updateVolunteerProfileAction.execute(profileDto,contactPersonEmail);
        } catch (IOException ex){
            throw new SomeFieldIsNullException(ex.getMessage());
        } catch (MatchingEmailException ex){
            throw new EmailAlreadyExistsException(ex.getMessage());
        }
    }
}
