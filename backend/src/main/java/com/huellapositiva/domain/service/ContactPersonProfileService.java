package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.UpdateContactPersonProfileRequestDto;
import com.huellapositiva.application.exception.EmailAlreadyExistsException;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.dto.UpdateProfileResult;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPersonProfile;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.huellapositiva.domain.model.valueobjects.PhoneNumber.isNotPhoneNumber;
import static com.huellapositiva.domain.model.valueobjects.Roles.CONTACT_PERSON_NOT_CONFIRMED;

@Service
@Transactional
@AllArgsConstructor
public class ContactPersonProfileService {

    @Autowired
    private final JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    private final JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    @Autowired
    private final JwtService jwtService;

    /**
     * Update the contact person profile information in database
     *
     * @param profileRequestDto New contact person profile information to update
     * @param accountId Account ID of user logged
     */
    public UpdateProfileResult updateContactPersonProfile(UpdateContactPersonProfileRequestDto profileRequestDto,
                                                          String accountId) {
        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByAccountIdWithProfile(accountId)
                .orElseThrow(() -> new UserNotFoundException("Contact person not found. Account ID: " + accountId));
        boolean isNewEmail = !jpaContactPerson.getCredential().getEmail().equalsIgnoreCase(profileRequestDto.getEmail());
        validations(profileRequestDto, isNewEmail);

        if (isNewEmail) {
            Role newJpaRole = jpaRoleRepository.findByName(CONTACT_PERSON_NOT_CONFIRMED.toString())
                    .orElseThrow(() -> new RoleNotFoundException("Role " + CONTACT_PERSON_NOT_CONFIRMED.toString() + "not found."));
            Set<Role> newUserRoles = new HashSet<>();
            newUserRoles.add(newJpaRole);
            jpaContactPerson.getCredential().setRoles(newUserRoles);
            jwtService.revokeAccessTokens(jpaContactPerson.getCredential().getId());
        }

        updateContactPersonProfile(profileRequestDto, jpaContactPerson);
        jpaContactPerson.getCredential().setEmail(profileRequestDto.getEmail());

        jpaContactPersonRepository.save(jpaContactPerson);

        return new UpdateProfileResult(isNewEmail);
    }

    /**
     * Validate profile data.
     *
     * @param profileRequestDto New contact person profile information to update
     * @param isNewEmail True if the contact person is updating the email
     */
    private void validations(UpdateContactPersonProfileRequestDto profileRequestDto, boolean isNewEmail){
        if (isNewEmail && jpaCredentialRepository.findByEmail(profileRequestDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists in the database.");
        }
        if (isNotPhoneNumber(profileRequestDto.getPhoneNumber())) {
            throw new InvalidFieldException("The phone number field is invalid");
        }
    }

    /**
     *  Update information in profile table
     *
     *  @param profileRequestDto New contact person credential information to update
     *  @param jpaContactPerson  JPA representation of the contact person
     */
    private void updateContactPersonProfile(UpdateContactPersonProfileRequestDto profileRequestDto,
                                            JpaContactPerson jpaContactPerson) {
        JpaContactPersonProfile contactPersonProfile = jpaContactPerson.getContactPersonProfile();
        contactPersonProfile.setName(profileRequestDto.getName());
        contactPersonProfile.setSurname(profileRequestDto.getSurname());
        contactPersonProfile.setPhoneNumber(profileRequestDto.getPhoneNumber());
    }
}
