package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProposalResponseDto;
import com.huellapositiva.application.dto.RegisterContactPersonDto;
import com.huellapositiva.application.dto.UpdateContactPersonProfileRequestDto;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.actions.RegisterESALContactPersonAction;
import com.huellapositiva.domain.actions.UpdateContactPersonProfileAction;
import com.huellapositiva.domain.actions.UploadPhotoAction;
import com.huellapositiva.domain.exception.EmptyFileException;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "ESAL members", description = "The ESAL's members API")
@RequestMapping("/api/v1/contactperson")
public class ESALContactPersonApiController {

    private final RegisterESALContactPersonAction registerESALContactPersonAction;

    private final JwtService jwtService;

    private final JpaRoleRepository jpaRoleRepository;

    private final UploadPhotoAction uploadPhotoAction;

    private final UpdateContactPersonProfileAction updateContactPersonProfileAction;

    @Operation(
            summary = "Register a new ESAL employee",
            description = "Register a new ESAL employee",
            tags = "user"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Ok, ESAL member has been registered successfully."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, some parameter is not valid."
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, could not register the user due to a constraint violation.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not register the ESAL.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public JwtResponseDto registerContactPerson(@Validated @RequestBody RegisterContactPersonDto dto, HttpServletResponse res) {
        Id contactPersonId = registerESALContactPersonAction.execute(dto);
        String username = dto.getEmail();
        List<String> roles = jpaRoleRepository.findAllByEmailAddress(username).stream().map(Role::getName).collect(Collectors.toList());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(contactPersonId.toString())
                .toUri();
        res.addHeader(HttpHeaders.LOCATION, uri.toString());
        return jwtService.create(username, roles);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProposalResponseDto getMember(@PathVariable Integer id) {
        throw new UnsupportedOperationException();
    }

    @Operation(
            summary = "Update contact person profile information",
            description = "Update contact person profile information",
            tags = "user",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.QUERY, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, update information user profile"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, credentials are not valid or some field is mandatory"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Conflict, the new email already match with other email in db"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping("/profile")
    @RolesAllowed({"CONTACT_PERSON", "CONTACT_PERSON_NOT_CONFIRMED"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfileInformation(@Validated @RequestBody UpdateContactPersonProfileRequestDto updateContactPersonProfileRequestDto,
                                         @Parameter(hidden = true) @AuthenticationPrincipal String accountId) {
        updateContactPersonProfileAction.execute(updateContactPersonProfileRequestDto, accountId);
    }

    @Operation(
            summary = "Upload contact person Photo",
            description = "Upload contact person Photo to profile",
            tags = "user",
            parameters = {
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.QUERY, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "For take this value, open your inspector code on your browser, and take the value of the cookie with the name 'XSRF-TOKEN'. Example: a6f5086d-af6b-464f-988b-7a604e46062b"),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true, example = "a6f5086d-af6b-464f-988b-7a604e46062b", description = "Same value of X-XSRF-TOKEN")
            },
            security = {
                    @SecurityRequirement(name = "accessToken")
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content, uploaded photo successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request, photo is not valid"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error, could not fetch the user data due to a connectivity issue."
                    )
            }
    )
    @PostMapping(path = "/profile/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed({"CONTACT_PERSON","CONTACT_PERSON_NOT_CONFIRMED"})
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadPhoto(@RequestPart("photo") MultipartFile photo,
                            @Parameter(hidden = true) @AuthenticationPrincipal String accountId) throws IOException {
        try {
            uploadPhotoAction.executeAsContactPerson(photo, accountId);
        } catch (InvalidFieldException | EmptyFileException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
