package com.huellapositiva.application.controller;

import com.huellapositiva.domain.Roles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("api/v1/test")
public class TestApiController {

    @RolesAllowed({"VOLUNTEER"})
    @GetMapping
    public String test() {
        return "OK";
    }
}
