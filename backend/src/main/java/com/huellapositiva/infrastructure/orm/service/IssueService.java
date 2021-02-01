package com.huellapositiva.infrastructure.orm.service;

import com.huellapositiva.infrastructure.orm.entities.JpaFailEmailConfirmation;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@AllArgsConstructor
public class IssueService {

    @Autowired
    private final JpaFailEmailConfirmationRepository jpaFailEmailConfirmationRepository;

    public void registerEmailConfirmationIssue(String email, Exception ex) {
        JpaFailEmailConfirmation failEmailConfirmation = JpaFailEmailConfirmation.builder()
                .emailAddress(email)
                .exceptionTrace(Arrays.toString(ex.getStackTrace()))
                .build();

        jpaFailEmailConfirmationRepository.save(failEmailConfirmation);
    }
}
