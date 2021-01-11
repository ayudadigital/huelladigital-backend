package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.application.exception.EmailAlreadyExistsException;
import com.huellapositiva.application.exception.InvalidFieldException;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UpdateVolunteerProfileAction {
    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    /**
     * This method update the user profile information in database
     *
     * @param profileDto New user profile information to update
     * @param email      Email of user logged
     */
    public void execute(ProfileDto profileDto, String email) {
        boolean isNotEqualsEmail = !email.equals(profileDto.getEmail());
        validations(profileDto, isNotEqualsEmail);

        JpaLocation jpaLocation = updateLocation(profileDto, email);
        JpaProfile jpaProfile = upsertProfile(profileDto, email);
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(email);
        jpaVolunteer.getCredential().setEmail(profileDto.getEmail());

        jpaVolunteer.setProfile(jpaProfile);
        jpaVolunteer.setLocation(jpaLocation);
        jpaVolunteerRepository.save(jpaVolunteer);

        if (isNotEqualsEmail) {
            EmailConfirmation emailConfirmation = EmailConfirmation.from(profileDto.getEmail(), emailConfirmationBaseUrl);
            emailCommunicationService.sendMessageEmailChanged(emailConfirmation);
        }
    }

    /**
     * This method valid the profileDto data
     *
     * @param profileDto New user profile information to update
     * @param isNotEqualsEmail The email used to check in the database email
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
        if (AdditionalInformation.lengthValidation(profileDto.getAdditionalInformation())) {
            throw new InvalidFieldException("The additional information field is invalid");
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
