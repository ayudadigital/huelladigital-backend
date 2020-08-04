package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.domain.model.valueobjects.ExpressRegistrationESAL;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.infrastructure.orm.entities.Organization;
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
    private final ESALRepository ESALRepository;

    public Integer create(ESALRequestDto dto) {
        try {
            return ESALRepository.save(new ExpressRegistrationESAL(dto.getName()));
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist the proposal due to a conflict.", ex);
            throw new FailedToPersistProposal("Conflict encountered while storing the proposal in database. Constraints were violated.", ex);
        }
    }

    public Organization findById(Integer id) {
        return ESALRepository.findById(id);
    }
}
