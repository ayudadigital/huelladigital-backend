package com.huellapositiva.infrastructure.orm.service;

import com.huellapositiva.domain.ExpressRegistrationVolunteer;
import com.huellapositiva.domain.FailEmailConfirmation;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        com.huellapositiva.infrastructure.orm.model.FailEmailConfirmation email = com.huellapositiva.infrastructure.orm.model.FailEmailConfirmation.builder()
                .email(failEmailConfirmation.getEmailAddress())
                .build();
        return jpaFailEmailConfirmationRepository.save(email).getId();
    }

}
