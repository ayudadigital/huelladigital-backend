package com.huellapositiva.integration;

import com.huellapositiva.application.dto.RegisterContactPersonDto;
import com.huellapositiva.domain.actions.RegisterESALContactPersonAction;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static com.huellapositiva.util.TestData.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class RegisterESALContactPersonActionShould {

    @Autowired
    private RegisterESALContactPersonAction organizationMemberAction;

    @Autowired
    private TestData testData;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void send_email_to_confirm_a_new_organization_member(){
        //GIVEN
        RegisterContactPersonDto dto = RegisterContactPersonDto.builder().name(VALID_NAME).surname(VALID_SURNAME).phoneNumber(VALID_PHONE).email(DEFAULT_EMAIL).password(DEFAULT_PASSWORD).build();

        //WHEN
        organizationMemberAction.execute(dto);

        //THEN
        verify(emailService, times(1)).sendEmail(any());
    }
}
