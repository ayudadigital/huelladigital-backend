package com.huellapositiva.unit;

import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.actions.UpdateVolunteerProfileAction;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.ProfileDtoDataEntry;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static com.huellapositiva.util.TestData.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@Import(TestData.class)
public class UpdateVolunteerProfileActionShould {

    @Autowired
    private UpdateVolunteerProfileAction updateVolunteerProfileAction;

    @Autowired
    private TestData testData;

    @Mock
    private EmailCommunicationService emailCommunicationService;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @BeforeEach
    void beforeEach(){
        //updateVolunteerProfileAction = new UpdateVolunteerProfileAction(emailCommunicationService);
    }

    @Test
    void send_change_email(){
        /*updateVolunteerProfileAction = new UpdateVolunteerProfileAction(emailCommunicationService);
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        System.out.println("hola");

        ProfileDto profileDto = ProfileDto.builder()
                .name("nombre")
                .surname("apellido")
                .email(DEFAULT_EMAIL_2)
                .phoneNumber("+34 123456789")
                .birthDate(LocalDate.of(2020, 12, 10))
                .zipCode("35000")
                .island("Fuerteventura")
                .build();
        updateVolunteerProfileAction.execute(profileDto, DEFAULT_EMAIL);
        verify(emailCommunicationService).sendMessageEmailChanged(any());*/

    }

}
