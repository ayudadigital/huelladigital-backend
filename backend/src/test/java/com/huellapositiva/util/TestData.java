package com.huellapositiva.util;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.UUID;

@AllArgsConstructor
@TestComponent
@Transactional
public class TestData {

    public static final String DEFAULT_FROM = "noreply@huellapositiva.com";

    public static final String DEFAULT_EMAIL = "foo@huellapositiva.com";

    public static final String DEFAULT_ESAL_CONTACT_PERSON_EMAIL = "organizationEmployee@huellapositiva.com";

    public static final String DEFAULT_PASSWORD = "plainPassword";

    public static final String DEFAULT_ESAL = "Huella Digital";

    public static final String DEFAULT_PROPOSAL_EXPIRATION_HOUR = "23:55:00";

    public static final String UUID_REGEX = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";

    @Autowired
    private final JpaVolunteerRepository volunteerRepository;

    @Autowired
    private final JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    private final JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private final JpaFailEmailConfirmationRepository failEmailConfirmationRepository;

    @Autowired
    private final JpaRoleRepository roleRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JpaLocationRepository jpaLocationRepository;


    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    @Autowired
    private final JpaESALRepository jpaESALRepository;


    public void resetData() {
        volunteerRepository.deleteAll();
        jpaContactPersonRepository.deleteAll();
        jpaProposalRepository.deleteAll();
        jpaLocationRepository.deleteAll();
        jpaESALRepository.deleteAll();
        jpaCredentialRepository.deleteAll();
        jpaEmailConfirmationRepository.deleteAll();
        failEmailConfirmationRepository.deleteAll();
    }

    private EmailConfirmation createEmailConfirmation(UUID token) {
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .email(DEFAULT_EMAIL)
                .hash(token.toString())
                .build();
        return jpaEmailConfirmationRepository.save(emailConfirmation);
    }

    public Credential createCredential(String email, UUID token) {
        return createCredential(email, DEFAULT_PASSWORD, token);
    }

    public Credential createCredential(String email, Roles role) {
        return createCredential(email, UUID.randomUUID(), DEFAULT_PASSWORD, role);
    }

    public Credential createCredential(String email, String plainPassword, UUID token) {
        return createCredential(email, token, plainPassword, Roles.VOLUNTEER_NOT_CONFIRMED);
    }

    public Credential createCredential(String email, UUID token, String plainPassword, Roles userRole){
        EmailConfirmation emailConfirmation = createEmailConfirmation(token);
        Role role = roleRepository.findByName(userRole.toString()).orElse(null);
        Credential credential = Credential.builder()
                .email(email)
                .hashedPassword(passwordEncoder.encode(plainPassword))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .roles(Collections.singleton(role))
                .build();

        return jpaCredentialRepository.save(credential);
    }

    public JpaVolunteer createVolunteer(String email, String password) {
        return createVolunteer(email, password, Roles.VOLUNTEER);
    }

    public JpaVolunteer createVolunteer(String email, String password, Roles role) {
        Credential credential = createCredential(email, UUID.randomUUID(), password, role);

        JpaVolunteer volunteer = JpaVolunteer.builder()
                .credential(credential)
                .id(UUID.randomUUID().toString())
                .build();

        return volunteerRepository.save(volunteer);
    }

    public JpaContactPerson createESALMember(String email, String password) {
        return createESALMember(email, password, Roles.CONTACT_PERSON);
    }

    public JpaContactPerson createESALMember(String email, String password, Roles role) {
        Credential credential = createCredential(email, UUID.randomUUID(), password, role);
        JpaContactPerson contactPerson = JpaContactPerson.builder()
                .credential(credential)
                .id(UUID.randomUUID().toString())
                .build();
        return jpaContactPersonRepository.save(contactPerson);
    }

    public String createAndLinkESAL(JpaContactPerson contactPerson, JpaESAL esal) {
        String id = createESAL(esal);
        //member.setJoinedEsal(jpaESALRepository.findByUUID(id).get());
        //jpaContactPersonRepository.save(member);
        jpaContactPersonRepository.updateJoinedESAL(contactPerson.getId(), esal);
        return id;
    }

    public String createESAL(JpaESAL esal) {
        return jpaESALRepository.save(esal).getId();
    }

    public JpaProposal createProposal(JpaProposal proposal) {
         return jpaProposalRepository.save(proposal);
    }

    public ProposalRequestDto buildPublishedProposalDto() {
        return buildProposalDto(true);
    }

    public ProposalRequestDto buildUnpublishedProposalDto() {
        return buildProposalDto(false);
    }

    public ProposalRequestDto buildProposalDto(boolean isPublished) {
        return ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .expirationDate("24-08-2020")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .published(isPublished)
                .build();
    }

    public JpaProposal registerESALAndPublishedProposal() throws ParseException {
        return registerESALAndProposal(true);
    }

    public JpaProposal registerESALAndNotPublishedProposal() throws ParseException {
        return registerESALAndProposal(false);
    }

    private JpaProposal registerESALAndProposal(boolean isPublished) throws ParseException {
        JpaContactPerson contactPerson = createESALMember(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JpaESAL esal = JpaESAL.builder().id(UUID.randomUUID().toString()).name(DEFAULT_ESAL).build();
        createAndLinkESAL(contactPerson, esal);
        JpaProposal proposal = JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Recogida de ropita")
                .location(Location.builder().province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4").build())
                .esal(esal)
                .expirationDate( new SimpleDateFormat("dd-MM-yyyy").parse("24-08-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .published(isPublished)
                .build();
        return createProposal(proposal);
    }
}
