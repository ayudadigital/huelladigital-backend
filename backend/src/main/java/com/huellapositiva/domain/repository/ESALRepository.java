package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.ESALAlreadyExists;
import com.huellapositiva.domain.exception.UserAlreadyHasESALException;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.ExpressRegistrationESAL;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@AllArgsConstructor
public class ESALRepository {

    @Autowired
    private final JpaESALRepository jpaESALRepository;

    @Autowired
    private final JpaContactPersonRepository jpaContactPersonRepository;

    public String save(ExpressRegistrationESAL expressOrganization) {
        JpaESAL organization = JpaESAL.builder()
                .name(expressOrganization.getName())
                .build();
        return jpaESALRepository.save(organization).getId();
    }

    public JpaESAL findById(Integer id) {
        return jpaESALRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find the organization by the provided ID"));
    }

    public String save(ESAL model) {
        JpaContactPerson contactPerson = jpaContactPersonRepository.findByEmail(model.getContactPersonEmail().toString()).get();
        JpaESAL esal = JpaESAL.builder()
                .name(model.getName())
                .build();
        try {
            String id = jpaESALRepository.save(esal).getId();
            jpaContactPersonRepository.updateJoinedESAL(contactPerson.getId(), esal);
            return id;
        } catch (DataIntegrityViolationException ex) {
            throw new ESALAlreadyExists();
        }
    }

    public void delete(int id) {
        jpaContactPersonRepository.unlinkMembersOfESAL(id);
        jpaESALRepository.deleteById(id);
    }

    public Id newId() {
        return Id.newId();
    }
}
