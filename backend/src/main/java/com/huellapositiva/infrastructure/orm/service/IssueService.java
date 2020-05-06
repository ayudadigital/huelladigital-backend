package com.huellapositiva.infrastructure.orm.service;

import com.huellapositiva.domain.valueobjects.EmailConfirmation;
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

    // Recibir el id del usuario
    public Integer registerFailSendEmailConfirmation(EmailConfirmation emailConfirmation){
        return saveFailEmail(emailConfirmation);
    }
    public Integer saveFailEmail(EmailConfirmation failEmailConfirmation){
        FailEmailConfirmation email = FailEmailConfirmation.builder()
                .email(failEmailConfirmation.getEmailAddress())
                .build();
        return jpaFailEmailConfirmationRepository.save(email).getId();
    }

}
