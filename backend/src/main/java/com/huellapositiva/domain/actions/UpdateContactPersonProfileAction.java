package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.UpdateContactPersonProfileRequestDto;
import com.huellapositiva.domain.dto.UpdateProfileResult;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.service.ContactPersonProfileService;
import com.huellapositiva.domain.service.EmailCommunicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateContactPersonProfileAction {

    private final ContactPersonProfileService profileService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public void execute(UpdateContactPersonProfileRequestDto updateContactPersonProfileRequestDto, String accountId) {
        UpdateProfileResult result = profileService.updateContactPersonProfile(updateContactPersonProfileRequestDto, accountId);
        if (result.isNewEmail()) {
            EmailConfirmation emailConfirmation = EmailConfirmation.from(updateContactPersonProfileRequestDto.getEmail(), emailConfirmationBaseUrl);
            emailCommunicationService.sendMessageEmailChanged(emailConfirmation);
        }
    }
}
