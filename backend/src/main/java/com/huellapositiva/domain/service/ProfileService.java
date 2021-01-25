package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.UpdateProfileRequestDto;
import com.huellapositiva.application.exception.EmailAlreadyExistsException;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.valueobjects.AdditionalInformation;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.domain.model.valueobjects.PhoneNumber;
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
    private final JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private final JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    /**
     * This method update the user profile information in database
     *
     * @param updateProfileRequestDto New user profile information to update
     * @param email      Email of user logged
     * @param isNotEqualsEmail If the new email it is not the same what the old
     */
    public void updateProfile(UpdateProfileRequestDto updateProfileRequestDto, String email, boolean isNotEqualsEmail) {
        validations(updateProfileRequestDto, isNotEqualsEmail);

        JpaLocation jpaLocation = upsertLocation(updateProfileRequestDto, email);
        JpaProfile jpaProfile = upsertProfile(updateProfileRequestDto, email);
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(email);
        jpaVolunteer.getCredential().setEmail(updateProfileRequestDto.getEmail());

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
     * @param updateProfileRequestDto New user profile information to update
     * @param isNotEqualsEmail If the new email it is not the same what the old
     */
    private void validations(UpdateProfileRequestDto updateProfileRequestDto, boolean isNotEqualsEmail) {
        if (isNotEqualsEmail && jpaCredentialRepository.findByEmail(updateProfileRequestDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists in the database.");
        }
        if (Location.isNotIsland(updateProfileRequestDto.getIsland())) {
            throw new InvalidFieldException("The island field is invalid");
        }
        if (Location.isNotZipCode(updateProfileRequestDto.getZipCode())) {
            throw new InvalidFieldException("The zip code field is invalid");
        }
        if (PhoneNumber.isNotPhoneNumber(updateProfileRequestDto.getPhoneNumber())) {
            throw new InvalidFieldException("The phone number field is invalid");
        }
        if (AdditionalInformation.isLengthInvalid(updateProfileRequestDto.getAdditionalInformation())) {
            throw new InvalidFieldException("The additional information field is invalid");
        }
    }

    /**
     * This method update information in location table
     *
     * @param updateProfileRequestDto New user credential information to update
     * @param email      Email of user logged
     */
    private JpaLocation upsertLocation(UpdateProfileRequestDto updateProfileRequestDto, String email) {
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
                .province(updateProfileRequestDto.getProvince())
                .town(updateProfileRequestDto.getTown())
                .address(updateProfileRequestDto.getAddress())
                .island(updateProfileRequestDto.getIsland())
                .zipCode(updateProfileRequestDto.getZipCode()).build();
    }

    /**
     * This method update information in profile table
     *
     * @param updateProfileRequestDto New user credential information to update
     * @param email      Email of user logged
     */
    private JpaProfile upsertProfile(UpdateProfileRequestDto updateProfileRequestDto, String email) {
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
                .name(updateProfileRequestDto.getName())
                .surname(updateProfileRequestDto.getSurname())
                .phoneNumber(updateProfileRequestDto.getPhoneNumber())
                .birthDate(updateProfileRequestDto.getBirthDate())
                .twitter(updateProfileRequestDto.getTwitter())
                .instagram(updateProfileRequestDto.getInstagram())
                .linkedin(updateProfileRequestDto.getLinkedin())
                .additionalInformation(updateProfileRequestDto.getAdditionalInformation())
                .build();
    }
}
