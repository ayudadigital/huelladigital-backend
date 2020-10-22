package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.ESALAlreadyExistsException;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@Transactional
@AllArgsConstructor
public class ESALRepository {

    @Autowired
    private final JpaESALRepository jpaESALRepository;

    @Autowired
    private final JpaContactPersonRepository jpaContactPersonRepository;

    public JpaESAL findById(Integer id) {
        return jpaESALRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find the ESAL by the provided ID"));
    }

    public ESAL findByName(String esalName) {
        JpaESAL esal = jpaESALRepository.findByName(esalName)
                .orElseThrow(() -> new RuntimeException("Could not find the ESAL by the provided name"));
        return new ESAL(
                esal.getName(),
                new Id(esal.getId()),
                null);
    }

    public String save(ESAL model) {
        JpaContactPerson contactPerson = jpaContactPersonRepository.findByEmail(model.getContactPersonEmail().toString())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        JpaESAL esal = JpaESAL.builder()
                .id(model.getId().toString())
                .name(model.getName())
                .build();
        try {
            String id = jpaESALRepository.save(esal).getId();
            jpaContactPersonRepository.updateJoinedESAL(contactPerson.getId(), esal);
            return id;
        } catch (DataIntegrityViolationException ex) {
            throw new ESALAlreadyExistsException();
        }
    }

    public String saveAsReviser(ESAL model) {
        JpaESAL esal = JpaESAL.builder()
                .id(model.getId().toString())
                .name(model.getName())
                .build();
        try {
            return jpaESALRepository.save(esal).getId();
        } catch (DataIntegrityViolationException ex) {
            throw new ESALAlreadyExistsException();
        }
    }

    public void delete(String id) {
        jpaContactPersonRepository.unlinkMembersOfESAL(id);
        jpaESALRepository.deleteByNaturalId(id);
    }

    public Id newId() {
        return Id.newId();
    }
}
