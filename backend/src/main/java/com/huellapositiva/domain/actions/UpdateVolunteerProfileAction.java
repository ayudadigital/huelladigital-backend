package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProfileDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;

@Service
public class UpdateVolunteerProfileAction {

    public void execute(ProfileDto profileDto) throws IOException {
        if (someFieldIsEmptyCredentials(profileDto)) {
            throw new IOException("Some field is null");
        }
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
