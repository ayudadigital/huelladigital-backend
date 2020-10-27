package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ContactPersonRepository {

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    public ContactPerson findByJoinedEsalId(String esalId) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByEsalId(esalId).orElseThrow(UserNotFoundException::new);
        return new ContactPerson(EmailAddress.from(jpaContactPerson.getCredential().getEmail()),new Id(jpaContactPerson.getId()));
    }
}
