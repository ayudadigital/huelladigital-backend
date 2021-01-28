package com.huellapositiva.domain.service;

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
     * @param accountId Account ID of contact person
     * @return true if it is linked
     */
    public boolean isUserAssociatedWithAnESAL(String accountId) {
        return jpaContactPersonRepository.findByAccountId(accountId)
                .stream()
                .anyMatch(cp -> cp.getJoinedEsal() != null);
    }

}
