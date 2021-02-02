package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.entities.User;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.JpaReviser;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaReviserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CredentialsRepository {

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private JpaReviserRepository jpaReviserRepository;

    public User findReviserByAccountId(String accountId) {
        JpaReviser jpaReviser = jpaReviserRepository.findByAccountIdWithCredentials(accountId).orElseThrow(UserNotFoundException::new);

        return new User(
                new Id(jpaReviser.getCredential().getId()),
                EmailAddress.from(jpaReviser.getCredential().getEmail()),
                new Id(jpaReviser.getId()),
                jpaReviser.getName(),
                jpaReviser.getSurname()
        );
    }
}
