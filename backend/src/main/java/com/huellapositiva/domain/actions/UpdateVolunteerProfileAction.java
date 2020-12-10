package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.valueobjects.ProfileCredentials;
import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.model.valueobjects.ProfileLocation;
import com.huellapositiva.domain.model.valueobjects.ProfileVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

@Service
public class UpdateVolunteerProfileAction {

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    public void execute(ProfileDto profileDto, String email) throws IOException {
        if (someFieldIsEmptyCredentials(profileDto)) {
            throw new IOException("Some field is null");
        }
        ProfileCredentials profileCredentials =  ProfileCredentials.builder().name(profileDto.getName())
                .surname(profileDto.getSurname())
                .birthDate(ProfileCredentials.parseToLocalDate(profileDto.getBirthDate()))
                .email(profileDto.getEmail())
                .phoneNumber(profileDto.getPhoneNumber()).build();

        jpaCredentialRepository.updateProfile(email, profileCredentials.getName(), profileCredentials.getSurname(), profileCredentials.getEmail(),
                profileCredentials.getPhoneNumber(), profileCredentials.getBirthDate());

        //Send email after commit profile changes

/*
        ProfileLocation profileLocation = ProfileLocation.builder().address(profileDto.getAddress())
                .province(profileDto.getProvince())
                .town(profileDto.getTown())
                .zipCode(profileDto.getZipCode()).build();

/*
        ProfileVolunteer profileVolunteer = ProfileVolunteer.builder().twitter(profileDto.getTwitter())
                .instagram(profileDto.getInstagram())
                .linkedin(profileDto.getInstagram())
                .photoUrl(profileDto.getPhoto())
                .curriculumUrl(profileDto.getCurriculumVitae())
                .additionalInformation(profileDto.getAdditionalInformation()).build();
                */

    }

    private boolean someFieldIsEmptyCredentials(ProfileDto profile) {
        if (profile.getName() == null
                || profile.getSurname() == null
                || profile.getBirthDate() == null
                || profile.getEmail() == null
                || profile.getPhoneNumber() == null) {
            return true;
        } else {
            return false;
        }


        /*return profile.getName().isEmpty()
                || profile.getSurname().isEmpty()
                || profile.getBirthDate().isEqual(null)
                || profile.getEmail().isEmpty()
                || profile.getPhoneNumber().equals(null);*/
    }
}
