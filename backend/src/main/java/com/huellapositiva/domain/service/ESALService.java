package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.ExpressRegistrationESAL;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ESALService {

    @Autowired
    private final ESALRepository esalRepository;

    @Autowired
    private final JpaContactPersonRepository jpaContactPersonRepository;

    public String create(ESALRequestDto dto) {
        try {
            return esalRepository.save(new ExpressRegistrationESAL(dto.getName()));
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist the proposal due to a conflict.", ex);
            throw new FailedToPersistProposal("Conflict encountered while storing the proposal in database. Constraints were violated.", ex);
        }
    }

    public boolean isUserAssociatedWithAnESAL(EmailAddress contactPersonEmail) {
        return jpaContactPersonRepository.findByEmail(contactPersonEmail.toString())
                .stream()
                .anyMatch(n -> n.getJoinedEsal() != null);
    }


    public JpaESAL findById(Integer id) {
        return esalRepository.findById(id);
    }
}
