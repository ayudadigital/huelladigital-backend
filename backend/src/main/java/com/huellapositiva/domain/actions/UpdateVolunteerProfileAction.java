package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.exception.MatchingEmailException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
import com.huellapositiva.infrastructure.orm.entities.JpaProfile;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

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
     * @param email Email of user logged
     * @throws IOException
     */
    public void execute(ProfileDto profileDto, String email) throws IOException {
        if (someFieldIsEmptyCredentials(profileDto) || someFieldEmptyLocation(profileDto)) {
            throw new IOException("Some field is null");
        }

        boolean isNotEqualsEmail = !email.equals(profileDto.getEmail());
        if (isNotEqualsEmail && jpaCredentialRepository.findByEmail(profileDto.getEmail()).isPresent()) {
            throw new MatchingEmailException("Email already exists in the database.");
        }

        JpaLocation jpaLocation = updateLocation(profileDto, email);
        JpaProfile jpaProfile = upsertProfile(profileDto, email);
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailProfileInformation(email);
        jpaVolunteer.getCredential().setEmail(profileDto.getEmail());

        jpaVolunteer.setProfile(jpaProfile);
        jpaVolunteer.setLocation(jpaLocation);
        jpaVolunteerRepository.save(jpaVolunteer);

        if(isNotEqualsEmail){
            emailCommunicationService.sendMessageEmailChanged(EmailAddress.from(email));
        }
    }

    /**
     * This method update information in credential table
     *
     * @param profileDto New user credential information to update
     * @param email Email of user logged
     */
    private JpaLocation updateLocation(ProfileDto profileDto, String email) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailProfileInformation(email);
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

    private JpaProfile upsertProfile(ProfileDto profileDto, String email) {
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailProfileInformation(email);
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
                .birthDate(parseToLocalDate(profileDto.getBirthDate()))
                .twitter(profileDto.getTwitter())
                .instagram(profileDto.getInstagram())
                .linkedin(profileDto.getLinkedin())
                .additionalInformation(profileDto.getAdditionalInformation())
                .build();
    }

    /**
     * This method checks if the credential information provided is not null
     *
     * @param profile The information to check
     */
    private boolean someFieldIsEmptyCredentials(ProfileDto profile) {
        return profile.getName() == null
                || profile.getSurname() == null
                || profile.getBirthDate() == null
                || profile.getEmail() == null
                || profile.getPhoneNumber() == null;
    }

    /**
     * This method checks if the island and zip code is not null
     *
     * @param profileDto The information to check
     */
    private boolean someFieldEmptyLocation(ProfileDto profileDto) {
        return profileDto.getIsland() == null || profileDto.getZipCode() == null;
    }

    /**
     * This method parse the Date in String to LocalDate
     *
     * @param date This data comes from the profile
     */
    public LocalDate parseToLocalDate(String date){
        String[] parts = date.split("-");
        return LocalDate.of(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
    }
}
