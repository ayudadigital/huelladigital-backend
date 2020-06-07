package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class JwtController {

    @Autowired
    private final RegisterVolunteerAction registerVolunteerAction;

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refreshJwtToken(@RequestHeader("Refresh") String refreshToken) {

    }
}
