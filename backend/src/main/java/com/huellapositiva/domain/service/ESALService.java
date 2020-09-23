package com.huellapositiva.domain.service;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ESALService {

    @Autowired
    private final ESALRepository esalRepository;

    @Autowired
    private final JpaContactPersonRepository jpaContactPersonRepository;

    /**
     * This method checks if a contactPerson is linked to an ESAL
     *
     * @param contactPersonEmail
     * @return true if it is linked
     */
    public boolean isUserAssociatedWithAnESAL(EmailAddress contactPersonEmail) {
        return jpaContactPersonRepository.findByEmail(contactPersonEmail.toString())
                .stream()
                .anyMatch(n -> n.getJoinedEsal() != null);
    }

}
