package com.huellapositiva.unit;

import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.VolunteerRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaProfileRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VolunteerRepositoryShould {

    @Mock
    private JpaVolunteerRepository jpaVolunteerRepository;
    @Mock
    private JpaEmailConfirmationRepository jpaEmailConfirmationRepository;
    @Mock
    private JpaRoleRepository jpaRoleRepository;
    @Mock
    private JpaProfileRepository jpaProfileRepository;

    @Test
    void saving_volunteer_should_throw_role_not_found_if_role_doesnt_exist() {
        // GIVEN
        EmailConfirmation emailConfirmation = EmailConfirmation.from(DEFAULT_EMAIL, "");
        PasswordHash passwordHash = new PasswordHash("123456");
        Volunteer volunteer = new Volunteer(Id.newId(), EmailAddress.from(emailConfirmation.getEmailAddress()), passwordHash, Id.newId());
        when(jpaRoleRepository.findByName(Roles.VOLUNTEER_NOT_CONFIRMED.toString())).thenReturn(Optional.empty());

        // WHEN + THEN
        VolunteerRepository volunteerRepository = new VolunteerRepository(jpaVolunteerRepository, jpaEmailConfirmationRepository, jpaRoleRepository, jpaProfileRepository);
        assertThrows(RoleNotFoundException.class, () -> volunteerRepository.save(volunteer, emailConfirmation));
    }
}
