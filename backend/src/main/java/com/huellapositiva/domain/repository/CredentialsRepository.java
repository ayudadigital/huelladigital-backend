package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.UserNotFound;
import com.huellapositiva.domain.model.entities.User;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CredentialsRepository {

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    public User findByEmail(String email) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(email).orElseThrow(UserNotFound::new);
        return new User(
                EmailAddress.from(jpaCredential.getEmail()),
                new Id(jpaCredential.getId()),
                jpaCredential.getName(),
                jpaCredential.getSurname()
        );
    }
}
