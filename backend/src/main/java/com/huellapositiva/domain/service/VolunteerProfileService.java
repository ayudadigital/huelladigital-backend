package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.UpdateVolunteerProfileRequestDto;
import com.huellapositiva.application.exception.EmailAlreadyExistsException;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.dto.UpdateProfileResult;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static com.huellapositiva.domain.model.valueobjects.AdditionalInformation.isLengthInvalid;
import static com.huellapositiva.domain.model.valueobjects.Location.isNotIsland;
import static com.huellapositiva.domain.model.valueobjects.Location.isNotZipCode;
import static com.huellapositiva.domain.model.valueobjects.PhoneNumber.isNotPhoneNumber;
import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;

@Service
@Transactional
@AllArgsConstructor
public class VolunteerProfileService {

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private final JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    @Autowired
    private final JwtService jwtService;

    /**
     * This method update the user profile information in database
     *
     * @param profileRequestDto New user profile information to update
     * @param accountId         Account ID of user logged
     */
    public UpdateProfileResult updateVolunteerProfileProfile(UpdateVolunteerProfileRequestDto profileRequestDto, String accountId) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByAccountIdWithCredentialAndLocationAndProfile(accountId)
                .orElseThrow(() -> new UserNotFoundException("Volunteer not found. Account ID: " + accountId));
        boolean isNewEmail = !jpaVolunteer.getCredential().getEmail().equalsIgnoreCase(profileRequestDto.getEmail());
        validations(profileRequestDto, isNewEmail);

        upsertLocation(profileRequestDto, jpaVolunteer);
        upsertVolunteerProfile(profileRequestDto, jpaVolunteer);
        jpaVolunteer.getCredential().setEmail(profileRequestDto.getEmail());

        if (isNewEmail) {
            Role newJpaRole = jpaRoleRepository.findByName(VOLUNTEER_NOT_CONFIRMED.toString())
                    .orElseThrow(() -> new RoleNotFoundException("Role " + VOLUNTEER_NOT_CONFIRMED.toString() + "not found."));
            Set<Role> newUserRoles = new HashSet<>();
            newUserRoles.add(newJpaRole);
            jpaVolunteer.getCredential().setRoles(newUserRoles);
            jwtService.revokeAccessTokens(jpaVolunteer.getCredential().getId());
        }

        jpaVolunteerRepository.save(jpaVolunteer);

        return new UpdateProfileResult(isNewEmail);
    }

    /**
     * Validate profile data.
     *
     * @param profileRequestDto New user profile information to update
     * @param newEmail          True if the user is updating the email
     */
    private void validations(UpdateVolunteerProfileRequestDto profileRequestDto, boolean newEmail) {
        if (newEmail && jpaCredentialRepository.findByEmail(profileRequestDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists in the database.");
        }
        if (isNotIsland(profileRequestDto.getIsland())) {
            throw new InvalidFieldException("The island field is invalid");
        }
        if (isNotZipCode(profileRequestDto.getZipCode())) {
            throw new InvalidFieldException("The zip code field is invalid");
        }
        if (isNotPhoneNumber(profileRequestDto.getPhoneNumber())) {
            throw new InvalidFieldException("The phone number field is invalid");
        }
        if (isLengthInvalid(profileRequestDto.getAdditionalInformation())) {
            throw new InvalidFieldException("The additional information field is invalid");
        }
    }

    /**
     * This method update information in location table
     *
     * @param profileRequestDto New user credential information to update
     * @param jpaVolunteer      JPA representation of the volunteer
     */
    private void upsertLocation(UpdateVolunteerProfileRequestDto profileRequestDto, JpaVolunteer jpaVolunteer) {
        JpaLocation location = jpaVolunteer.getLocation();
        if (location == null) {
            location = JpaLocation.builder()
                    .id(Id.newId().toString())
                    .build();
            jpaVolunteer.setLocation(location);
        }
        location.setProvince(profileRequestDto.getProvince());
        location.setTown(profileRequestDto.getTown());
        location.setAddress(profileRequestDto.getAddress());
        location.setIsland(profileRequestDto.getIsland());
        location.setZipCode(profileRequestDto.getZipCode());
    }

    /**
     * This method update information in profile table
     *
     * @param profileRequestDto New user credential information to update
     * @param jpaVolunteer      JPA representation of the volunteer
     */
    private void upsertVolunteerProfile(UpdateVolunteerProfileRequestDto profileRequestDto, JpaVolunteer jpaVolunteer) {
        JpaProfile profile = jpaVolunteer.getProfile();
        if (profile == null) {
            profile = JpaProfile.builder()
                    .id(Id.newId().toString())
                    .build();
            jpaVolunteer.setProfile(profile);
        }
        profile.setName(profileRequestDto.getName());
        profile.setSurname(profileRequestDto.getSurname());
        profile.setPhoneNumber(profileRequestDto.getPhoneNumber());
        profile.setBirthDate(profileRequestDto.getBirthDate());
        profile.setTwitter(profileRequestDto.getTwitter());
        profile.setInstagram(profileRequestDto.getInstagram());
        profile.setLinkedin(profileRequestDto.getLinkedin());
        profile.setAdditionalInformation(profileRequestDto.getAdditionalInformation());
    }
}
