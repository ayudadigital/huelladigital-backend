package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.UpdateProfileRequestDto;
import com.huellapositiva.domain.dto.UpdateProfileResult;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
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
     * @param updateProfileRequestDto New user profile information to update
     * @param accountId Account ID of logged user
     */
    public void execute(UpdateProfileRequestDto updateProfileRequestDto, String accountId) {
        UpdateProfileResult result = profileService.updateProfile(updateProfileRequestDto, accountId);
        if (result.isNewEmail()) {
            EmailConfirmation emailConfirmation = EmailConfirmation.from(updateProfileRequestDto.getEmail(), emailConfirmationBaseUrl);
            emailCommunicationService.sendMessageEmailChanged(emailConfirmation);
        }
    }
}
