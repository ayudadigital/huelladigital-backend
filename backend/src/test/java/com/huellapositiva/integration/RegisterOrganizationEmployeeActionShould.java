package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsOrganizationEmployeeRequestDto;
import com.huellapositiva.domain.actions.RegisterOrganizationEmployeeAction;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class RegisterOrganizationEmployeeActionShould {

    @Autowired
    private RegisterOrganizationEmployeeAction organizationEmployeeAction;

    @Autowired
    private TestData testData;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void send_email_to_confirm_a_new_organization_employee(){
        //GIVEN
        CredentialsOrganizationEmployeeRequestDto dto = new CredentialsOrganizationEmployeeRequestDto("foo@huellapositiva.com", "plain-password");

        //WHEN
        organizationEmployeeAction.execute(dto);

        //THEN
        verify(emailService, times(1)).sendEmail(any());
    }
}

