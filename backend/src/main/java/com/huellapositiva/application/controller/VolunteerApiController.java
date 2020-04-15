package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.service.VolunteerService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/volunteers")
public class VolunteerApiController {

    @Autowired
    private final VolunteerService volunteerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerPatient(@RequestBody RegisterVolunteerRequestDto dto) {
        volunteerService.registerVolunteer(dto);
    }
}