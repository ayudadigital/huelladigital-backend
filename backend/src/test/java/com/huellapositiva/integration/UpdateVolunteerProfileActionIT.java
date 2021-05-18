package com.huellapositiva.integration;

import com.huellapositiva.application.dto.UpdateVolunteerProfileRequestDto;
import com.huellapositiva.domain.actions.UpdateVolunteerProfileAction;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER;
import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;
import static com.huellapositiva.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class UpdateVolunteerProfileActionIT {

    @Autowired
    private TestData testData;

    @Autowired
    private UpdateVolunteerProfileAction updateVolunteerProfileAction;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void should_change_role_when_updating_email_address() {
        testData.createVolunteer(DEFAULT_ACCOUNT_ID, DEFAULT_EMAIL, DEFAULT_PASSWORD, VOLUNTEER);
        UpdateVolunteerProfileRequestDto updateVolunteerProfileRequestDto = UpdateVolunteerProfileRequestDto.builder()
                .name(VALID_NAME)
                .surname(VALID_SURNAME)
                .email(DEFAULT_EMAIL_2)
                .phoneNumber(VALID_PHONE)
                .birthDate(VALID_BIRTHDAY)
                .zipCode(VALID_ZIPCODE)
                .island(VALID_ISLAND)
                .build();

        updateVolunteerProfileAction.execute(updateVolunteerProfileRequestDto, DEFAULT_ACCOUNT_ID);

        assertThat(jpaVolunteerRepository.findByAccountIdWithCredentials(DEFAULT_EMAIL)).isEmpty();
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(DEFAULT_EMAIL_2);
        assertThat(jpaVolunteer.getCredential().getRoles()).hasToString("[" + VOLUNTEER_NOT_CONFIRMED + "]");
    }
}
