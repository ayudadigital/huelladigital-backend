package com.huellapositiva.application.controller;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.exception.EmailException;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/volunteers")
public class VolunteerApiController {
    @Autowired
    private RegisterVolunteerAction registerVolunteerAction;
    @Autowired
    private IssueService issueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerVolunteer(@Validated @RequestBody RegisterVolunteerRequestDto dto) {
        try{
            registerVolunteerAction.execute(dto);
        }catch(PasswordNotAllowed pna){
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST , "Password doesn't meet minimum length",  pna);
        }catch (EmailException ex){
            issueService.registerVolunteerIssue(dto.getEmail(),ex);
            throw new ResponseStatusException( HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email confirmation", ex);
        }
    }
}