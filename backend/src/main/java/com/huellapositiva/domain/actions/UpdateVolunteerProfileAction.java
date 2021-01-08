package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.application.exception.EmailAlreadyExistsException;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateVolunteerProfileAction {
    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private EmailCommunicationService emailCommunicationService;

    /**
     * This method update the user profile information in database
     *
     * @param profileDto New user profile information to update
     * @param email      Email of user logged
     */
    public void execute(ProfileDto profileDto, String email) {
        boolean isNotEqualsEmail = !email.equals(profileDto.getEmail());
        if (isNotEqualsEmail && jpaCredentialRepository.findByEmail(profileDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists in the database.");
        }
        if (Location.isNotIsland(profileDto.getIsland())) {
            throw new InvalidFieldException("The island field is invalid");
        }
        if (Location.isNotZipCode(profileDto.getZipCode())) {
            throw new InvalidFieldException("The zip code field is invalid");
        }

        JpaLocation jpaLocation = updateLocation(profileDto, email);
        JpaProfile jpaProfile = upsertProfile(profileDto, email);
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(email);
        jpaVolunteer.getCredential().setEmail(profileDto.getEmail());

        jpaVolunteer.setProfile(jpaProfile);
        jpaVolunteer.setLocation(jpaLocation);
        jpaVolunteerRepository.save(jpaVolunteer);

        if (isNotEqualsEmail) {
            emailCommunicationService.sendMessageEmailChanged(EmailAddress.from(email));
        }
    }

    /**
     * This method update information in location table
     *
     * @param profileDto New user credential information to update
     * @param email      Email of user logged
     */
    private JpaLocation updateLocation(ProfileDto profileDto, String email) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(email);
        String id;
        if (jpaVolunteer.getLocation() == null) {
            id = Id.newId().toString();
        } else {
            id = jpaVolunteer.getLocation().getId();
        }
        return JpaLocation.builder()
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
        if (jpaVolunteer.getProfile() == null) {
            id = Id.newId().toString();
        } else {
            id = jpaVolunteer.getProfile().getId();
        }
        return JpaProfile.builder()
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
