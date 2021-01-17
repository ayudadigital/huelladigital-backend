package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateVolunteerProfileAction {

    private final ProfileService profileService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    /**
     * This method update the user profile information in database
     *
     * @param profileDto New user profile information to update
     * @param email      Email of user logged
     * @param volunteerId Id of the volunteer to update de profile information
     */
    public void execute(ProfileDto profileDto, String email, String volunteerId) {
        boolean isNotEqualsEmail = !email.equals(profileDto.getEmail());
        profileService.updateProfile(profileDto, email, isNotEqualsEmail, volunteerId);
        if (isNotEqualsEmail) {
            EmailConfirmation emailConfirmation = EmailConfirmation.from(profileDto.getEmail(), emailConfirmationBaseUrl);
            emailCommunicationService.sendMessageEmailChanged(emailConfirmation);
        }
    }
}
