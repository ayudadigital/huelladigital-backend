package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.application.exception.EmailAlreadyExistsException;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;

@Service
@Transactional
@AllArgsConstructor
public class ProfileService {

    @Autowired
    final private JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    final private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    final private JpaRoleRepository jpaRoleRepository;

    /**
     * This method update the user profile information in database
     *
     * @param profileDto New user profile information to update
     * @param email      Email of user logged
     * @param isNotEqualsEmail If the new email it is not the same what the old
     */
    public void updateProfile(ProfileDto profileDto, String email, boolean isNotEqualsEmail) {
        validations(profileDto, isNotEqualsEmail);

        JpaLocation jpaLocation = upsertLocation(profileDto, email);
        JpaProfile jpaProfile = upsertProfile(profileDto, email);
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(email);
        jpaVolunteer.getCredential().setEmail(profileDto.getEmail());

        jpaVolunteer.setProfile(jpaProfile);
        jpaVolunteer.setLocation(jpaLocation);
        jpaVolunteerRepository.save(jpaVolunteer);

        if (isNotEqualsEmail) {
            Role newJpaRole = jpaRoleRepository.findByName(VOLUNTEER_NOT_CONFIRMED.toString())
                    .orElseThrow(() -> new RoleNotFoundException("Role " + VOLUNTEER_NOT_CONFIRMED.toString() + "not found."));
            Set<Role> newUserRoles = new HashSet<>();
            newUserRoles.add(newJpaRole);
            jpaVolunteer.getCredential().setRoles(newUserRoles);
            jpaVolunteerRepository.save(jpaVolunteer);
        }
    }

    /**
     * This method valid the profileDto data
     *
     * @param profileDto New user profile information to update
     * @param isNotEqualsEmail If the new email it is not the same what the old
     */
    private void validations(ProfileDto profileDto, boolean isNotEqualsEmail) {
        if (isNotEqualsEmail && jpaCredentialRepository.findByEmail(profileDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists in the database.");
        }
        if (Location.isNotIsland(profileDto.getIsland())) {
            throw new InvalidFieldException("The island field is invalid");
        }
        if (Location.isNotZipCode(profileDto.getZipCode())) {
            throw new InvalidFieldException("The zip code field is invalid");
        }
        if (PhoneNumber.isNotPhoneNumber(profileDto.getPhoneNumber())) {
            throw new InvalidFieldException("The phone number field is invalid");
        }
        if (AdditionalInformation.isLengthInvalid(profileDto.getAdditionalInformation())) {
            throw new InvalidFieldException("The additional information field is invalid");
        }
    }

    /**
     * This method update information in location table
     *
     * @param profileDto New user credential information to update
     * @param email      Email of user logged
     */
    private JpaLocation upsertLocation(ProfileDto profileDto, String email) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(email);
        String id;
        Integer surrogateKey = null;
        if (jpaVolunteer.getLocation() == null) {
            id = Id.newId().toString();
        } else {
            id = jpaVolunteer.getLocation().getId();
            surrogateKey = jpaVolunteer.getLocation().getSurrogateKey();
        }
        return JpaLocation.builder()
                .surrogateKey(surrogateKey)
                .id(id)
                .province(profileDto.getProvince())
                .town(profileDto.getTown())
                .address(profileDto.getAddress())
                .island(profileDto.getIsland())
                .zipCode(profileDto.getZipCode()).build();
    }

    /**
     * This method update information in profile table
     *
     * @param profileDto New user credential information to update
     * @param email      Email of user logged
     */
    private JpaProfile upsertProfile(ProfileDto profileDto, String email) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(email);
        String id;
        Integer surrogateKey = null;
        if (jpaVolunteer.getProfile() == null) {
            id = Id.newId().toString();
        } else {
            id = jpaVolunteer.getProfile().getId();
            surrogateKey = jpaVolunteer.getProfile().getSurrogateKey();
        }
        return JpaProfile.builder()
                .surrogateKey(surrogateKey)
                .id(id)
                .name(profileDto.getName())
                .surname(profileDto.getSurname())
                .phoneNumber(profileDto.getPhoneNumber())
                .birthDate(profileDto.getBirthDate())
                .twitter(profileDto.getTwitter())
                .instagram(profileDto.getInstagram())
                .linkedin(profileDto.getLinkedin())
                .additionalInformation(profileDto.getAdditionalInformation())
                .build();
    }
}
