package com.huellapositiva.application.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApiController {
    @PostMapping(value = "/refresh")
    @ResponseStatus(HttpStatus.OK)
    public void refreshJwtToken(@RequestBody String refreshToken) {
        
    }
}
