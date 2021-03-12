package com.huellapositiva.domain.repository;

import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPersonProfile;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Component
@Transactional
public class ContactPersonRepository {

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    public ContactPerson findByJoinedEsalId(String esalId) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByEsalId(esalId).orElseThrow(UserNotFoundException::new);
        return new ContactPerson(new Id(jpaContactPerson.getCredential().getId()), EmailAddress.from(jpaContactPerson.getCredential().getEmail()), new Id(jpaContactPerson.getId()));
    }

    /**
     * This method return the contact person full information stored in DB.
     *
     * @param accountId Account ID of the contact person
     */
    public ContactPerson findByAccountId(String accountId) {
        JpaContactPerson contactPerson = jpaContactPersonRepository.findByAccountIdWithProfile(accountId)
                .orElseThrow(() -> new UserNotFoundException("Could not find contactPerson with account ID: " + accountId));

        return new ContactPerson(
                new Id(contactPerson.getCredential().getId()),
                EmailAddress.from(contactPerson.getCredential().getEmail()),
                new Id(contactPerson.getId()));
    }

    /**
     * This method store in DB the URL of user profile photo
     *
     * @param contactPerson The contact person information
     */
    public void updatePhoto(ContactPerson contactPerson) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByAccountId(contactPerson.getAccountId().toString())
                .orElseThrow(() -> new NoSuchElementException("No exists contact person with: " + contactPerson.getId()));

        if (jpaContactPerson.getContactPersonProfile() == null) {
            JpaContactPersonProfile jpaContactPersonProfile = JpaContactPersonProfile.builder()
                    .id(Id.newId().toString())
                    .build();
            jpaContactPerson.setContactPersonProfile(jpaContactPersonProfile);
        }

        jpaContactPerson.getContactPersonProfile().setPhotoUrl(contactPerson.getPhoto().toExternalForm());
        jpaContactPersonRepository.save(jpaContactPerson);
    }
}
