package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsOrganizationMemberRequestDto;
import com.huellapositiva.domain.actions.RegisterOrganizationMemberAction;
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
class RegisterOrganizationMemberActionShould {

    @Autowired
    private RegisterOrganizationMemberAction organizationMemberAction;

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
        CredentialsOrganizationMemberRequestDto dto = new CredentialsOrganizationMemberRequestDto("foo@huellapositiva.com", "plain-password");

        //WHEN
        organizationMemberAction.execute(dto);

        //THEN
        verify(emailService, times(1)).sendEmail(any());
    }
}

