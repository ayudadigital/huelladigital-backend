package com.huellapositiva.unit;

import com.huellapositiva.application.dto.UpdateProfileRequestDto;
import com.huellapositiva.domain.actions.UpdateVolunteerProfileAction;
import com.huellapositiva.domain.dto.UpdateProfileResult;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static com.huellapositiva.util.TestData.DEFAULT_ACCOUNT_ID;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL_2;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVolunteerProfileActionShould {

    private UpdateVolunteerProfileAction updateVolunteerProfileAction;

    @Mock
    private EmailCommunicationService emailCommunicationService;

    @Mock
    private ProfileService profileService;

    @BeforeEach
    void beforeEach(){
        updateVolunteerProfileAction = new UpdateVolunteerProfileAction(profileService, emailCommunicationService);
    }

    @Test
    void send_change_email() {
        UpdateProfileRequestDto updateProfileRequestDto = UpdateProfileRequestDto.builder()
                .name("nombre")
                .surname("apellido")
                .email(DEFAULT_EMAIL_2)
                .phoneNumber("+34 123456789")
                .birthDate(LocalDate.of(2020, 12, 10))
                .zipCode("35000")
                .island("Fuerteventura")
                .build();
        when(profileService.updateProfile(updateProfileRequestDto,DEFAULT_ACCOUNT_ID)).thenReturn(new UpdateProfileResult(true));

        updateVolunteerProfileAction.execute(updateProfileRequestDto, DEFAULT_ACCOUNT_ID);

        verify(emailCommunicationService).sendMessageEmailChanged(any());
    }
}
