package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.exception.MatchingEmailException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.JpaLocation;
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

    public void execute(ProfileDto profileDto, String email) throws IOException {
        if (someFieldIsEmptyCredentials(profileDto) || someFieldEmptyLocation(profileDto)) {
            throw new IOException("Some field is null");
        }

        boolean isNotEqualsEmail = !email.equals(profileDto.getEmail());
        if (isNotEqualsEmail && jpaCredentialRepository.findByEmail(profileDto.getEmail()).isPresent()) {
            throw new MatchingEmailException("Email already exists in the database.");
        }

        JpaLocation jpaLocation = updateLocation(profileDto, email);
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailProfileInformation(email);
        updateProfileInformation(profileDto, jpaVolunteer);
        updateCredentials(profileDto, jpaVolunteer);

        jpaVolunteer.setLocation(jpaLocation);
        jpaVolunteerRepository.save(jpaVolunteer);

        if(isNotEqualsEmail){
            emailCommunicationService.sendMessageEmailChanged(EmailAddress.from(email));
        }
    }

    private void updateProfileInformation(ProfileDto profileDto, JpaVolunteer jpaVolunteer) {
        jpaVolunteer.setTwitter(profileDto.getTwitter());
        jpaVolunteer.setInstagram(profileDto.getInstagram());
        jpaVolunteer.setLinkedin(profileDto.getLinkedin());
        jpaVolunteer.setAdditionalInformation(profileDto.getAdditionalInformation());
    }

    private void updateCredentials(ProfileDto profileDto, JpaVolunteer jpaVolunteer) {
        jpaVolunteer.getCredential().setName(profileDto.getName());
        jpaVolunteer.getCredential().setSurname(profileDto.getSurname());
        jpaVolunteer.getCredential().setEmail(profileDto.getEmail());
        jpaVolunteer.getCredential().setBirthDate(parseToLocalDate(profileDto.getBirthDate()));
        jpaVolunteer.getCredential().setPhoneNumber(profileDto.getPhoneNumber());
    }

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

    private boolean someFieldIsEmptyCredentials(ProfileDto profile) {
        return profile.getName() == null
                || profile.getSurname() == null
                || profile.getBirthDate() == null
                || profile.getEmail() == null
                || profile.getPhoneNumber() == null;
    }

    private boolean someFieldEmptyLocation(ProfileDto profileDto) {
        return profileDto.getIsland() == null || profileDto.getZipCode() == null;
    }

    public LocalDate parseToLocalDate(String date){
        String[] parts = date.split("-");
        return LocalDate.of(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2]));
    }
}
