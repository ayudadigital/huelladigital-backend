package com.huellapositiva.application.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("api/v1/test-jwt-authorization")
public class ApiControllerShould {

    @RolesAllowed({"VOLUNTEER"})
    @GetMapping
    public String test() {
        return "OK";
    }
}
