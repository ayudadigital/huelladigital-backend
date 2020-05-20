package com.huellapositiva.infrastructure.orm.service;

import com.huellapositiva.infrastructure.orm.model.FailEmailConfirmation;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class IssueService {

    @Autowired
    private final JpaFailEmailConfirmationRepository jpaFailEmailConfirmationRepository;

    public Integer registerVolunteerIssue(String email, Exception ex) {
        FailEmailConfirmation failEmailConfirmation = FailEmailConfirmation.builder()
                .emailAddress(email)
                .exceptionTrace(ex.getStackTrace().toString())
                .build();

        return jpaFailEmailConfirmationRepository.save(failEmailConfirmation).getId();
    }
}
