package com.huellapositiva.infrastructure.orm.service;

import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.model.FailEmailConfirmation;
import com.huellapositiva.infrastructure.orm.model.Volunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class IssueService {

    @Autowired
    private final JpaFailEmailConfirmationRepository jpaFailEmailConfirmationRepository;

    public Integer registerFailSendEmailConfirmation(EmailConfirmation emailConfirmation, Integer volunteerId){
        /*TODO:buscar voluntario por ID*/
        return saveFailEmail(emailConfirmation/*,volunteerId*/);
    }
    public Integer saveFailEmail(EmailConfirmation failEmailConfirmation) {
        FailEmailConfirmation email = FailEmailConfirmation.builder()
                .emailAddress(failEmailConfirmation.getEmailAddress())
                /*.volunteerId(volunteerId)*/
                .build();
        return jpaFailEmailConfirmationRepository.save(email).getId();
    }

}
