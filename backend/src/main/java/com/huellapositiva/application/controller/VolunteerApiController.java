package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/volunteers")
public class VolunteerApiController {
    @Autowired
    private RegisterVolunteerAction registerVolunteerAction;
    @Autowired
    private IssueService issueService;

    @PostMapping
    @ResponseStatus
    public HttpStatus registerVolunteer(@Validated @RequestBody RegisterVolunteerRequestDto dto) {
        try{
            registerVolunteerAction.execute(dto);
        }catch (Exception ex){
            issueService.registerVolunteerIssue(dto.getEmail(),ex);
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.CREATED;
    }
}