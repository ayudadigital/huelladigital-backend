package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.UpdateVolunteerProfileRequestDto;
import com.huellapositiva.domain.dto.UpdateProfileResult;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.VolunteerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateVolunteerProfileAction {

    private final VolunteerProfileService volunteerProfileService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    /**
     * This method update the user profile information in database
     *
     * @param updateVolunteerProfileRequestDto New user profile information to update
     * @param accountId Account ID of logged user
     */
    public void execute(UpdateVolunteerProfileRequestDto updateVolunteerProfileRequestDto, String accountId) {
        UpdateProfileResult result = volunteerProfileService.updateVolunteerProfileProfile(updateVolunteerProfileRequestDto, accountId);
        if (result.isNewEmail()) {
            EmailConfirmation emailConfirmation = EmailConfirmation.from(updateVolunteerProfileRequestDto.getEmail(), emailConfirmationBaseUrl);
            emailCommunicationService.sendMessageEmailChanged(emailConfirmation);
        }
    }
}
