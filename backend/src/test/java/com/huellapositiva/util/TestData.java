package com.huellapositiva.util;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.orm.model.*;
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

    public static final String DEFAULT_ORGANIZATION_EMPLOYEE_EMAIL = "organizationEmployee@huellapositiva.com";

    public static final String DEFAULT_PASSWORD = "plainPassword";

    public static final String DEFAULT_ORGANIZATION = "Huella Digital";

    @Autowired
    private final JpaVolunteerRepository volunteerRepository;

    @Autowired
    private final JpaOrganizationEmployeeRepository organizationEmployeeRepository;

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
    private final JpaOrganizationEmployeeRepository jpaOrganizationEmployeeRepository;

    @Autowired
    private final JpaProposalRepository jpaProposalRepository;

    @Autowired
    private final JpaOrganizationRepository organizationRepository;


    public void resetData() {
        volunteerRepository.deleteAll();
        jpaOrganizationEmployeeRepository.deleteAll();
        jpaProposalRepository.deleteAll();
        jpaLocationRepository.deleteAll();
        organizationRepository.deleteAll();
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

    public Volunteer createVolunteer(String email, String password) {
        return createVolunteer(email, password, Roles.VOLUNTEER);
    }

    public Volunteer createVolunteer(String email, String password, Roles role) {
        Credential credential = createCredential(email, UUID.randomUUID(), password, role);
        Volunteer volunteer = Volunteer.builder().credential(credential).build();
        return volunteerRepository.save(volunteer);
    }

    public OrganizationEmployee createOrganizationEmployee(String email, String password) {
        return createOrganizationEmployee(email, password, Roles.ORGANIZATION_EMPLOYEE);
    }

    public OrganizationEmployee createOrganizationEmployee(String email, String password, Roles role) {
        Credential credential = createCredential(email, UUID.randomUUID(), password, role);
        OrganizationEmployee employee = OrganizationEmployee.builder().credential(credential).build();
        return organizationEmployeeRepository.save(employee);
    }

    public Integer createAndLinkOrganization(OrganizationEmployee employee, Organization organization) {
        organizationRepository.save(organization);
        return organizationEmployeeRepository.updateJoinedOrganization(employee.getId(), organization);
    }

    public Proposal createProposal(Proposal proposal) {
         return jpaProposalRepository.save(proposal);
    }

    public ProposalRequestDto buildPublishedProposalDto() {
        return buildProposalDto(true);
    }
    public ProposalRequestDto buildUnpublishedProposalDto() {
        return buildProposalDto(false);
    }

    private ProposalRequestDto buildProposalDto(boolean isPublished) {
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

    public Proposal registerOrganizationAndProposal() throws ParseException {
        OrganizationEmployee employee = createOrganizationEmployee(DEFAULT_ORGANIZATION_EMPLOYEE_EMAIL, DEFAULT_PASSWORD);
        Organization organization = Organization.builder().name(DEFAULT_ORGANIZATION).build();
        createAndLinkOrganization(employee, organization);
        Proposal proposal = Proposal.builder()
                .title("Recogida de ropita")
                .location(Location.builder().province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4").build())
                .organization(organization)
                .expirationDate( new SimpleDateFormat("dd-MM-yyyy").parse("24-08-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .published(true)
                .build();
        return createProposal(proposal);
    }
}
