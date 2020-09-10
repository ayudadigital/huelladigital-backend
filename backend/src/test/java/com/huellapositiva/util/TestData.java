package com.huellapositiva.util;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.*;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.AwsS3Properties;
import com.huellapositiva.infrastructure.orm.entities.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    @Autowired
    private final JpaProposalSkillsRepository jpaProposalSkillsRepository;

    @Autowired
    private final JpaProposalRequirementsRepository jpaProposalRequirementsRepository;

    @Autowired
    private final ProposalRepository proposalRepository;

    @Autowired
    private final AwsS3Properties awsS3Properties;


    public void resetData() {
        jpaProposalSkillsRepository.deleteAll();
        jpaProposalRequirementsRepository.deleteAll();
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
        String id = createJpaESAL(esal).getId();
        jpaContactPersonRepository.updateJoinedESAL(contactPerson.getId(), esal);
        return id;
    }

    public JpaESAL createJpaESAL(JpaESAL esal) {
        return jpaESALRepository.save(esal);
    }

    public ESAL createESAL(String id, String name) {
        JpaESAL savedEsal = jpaESALRepository.save(JpaESAL.builder().id(id).name(name).build());
        return new ESAL(savedEsal.getName(), new Id(savedEsal.getId()));
    }

    public JpaProposal createProposal(JpaProposal jpaProposal) {
         return jpaProposalRepository.save(jpaProposal);
    }

    public ProposalRequestDto buildPublishedProposalDto() {
        return buildProposalDto(true);
    }

    public ProposalRequestDto buildUnpublishedProposalDto() {
        return buildProposalDto(false);
    }

    public ProposalRequestDto buildProposalDto(boolean isPublished){
        return ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .startingProposalDate("15-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .published(isPublished)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("30-01-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
    }

    public Proposal buildPublishedProposalWithEsal(ESAL esal) {
        return buildProposal(esal, true);
    }

    public Proposal buildUnpublishedProposalWithEsal(ESAL esal) {
        return buildProposal(esal, false);
    }

    @SneakyThrows
    public Proposal buildProposal(ESAL esal, boolean isPublished) {
        Proposal proposal = Proposal.builder()
                .id(Id.newId())
                .title("Recogida de ropita")
                .esal(esal)
                .location(new Location("SC Tenerife", "La Laguna", "Avenida Trinidad"))
                .startingProposalDate(ProposalDate.createStartingProposalDate("20-01-2021"))
                .closingProposalDate(ProposalDate.createClosingProposalDate("24-01-2021"))
                .startingVolunteeringDate(ProposalDate.createClosingProposalDate("25-01-2021"))
                .requiredDays("Weekends")
                .permittedAgeRange(AgeRange.create(18, 26))
                .published(isPublished)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .category(ProposalCategory.ON_SITE)
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .image(createMockImageUrl())
                .build();

        Arrays.asList(new Skill("Habilidad", "Descripción"), new Skill("Negociación", "Saber regatear"))
                .forEach(proposal::addSkill);
        Arrays.asList(new Requirement("Forma física para cargar con la ropa"), new Requirement("Disponibilidad horaria"), new Requirement("Carnet de conducir"))
                .forEach(proposal::addRequirement);

        return proposal;
    }

    public JpaProposal registerESALAndPublishedProposal() throws ParseException {
        return registerESALAndProposal(true);
    }

    public JpaProposal registerESALAndNotPublishedProposal() throws ParseException {
        return registerESALAndProposal(false);
    }

    @SneakyThrows
    private JpaProposal registerESALAndProposal(boolean isPublished) {
        JpaContactPerson contactPerson = createESALMember(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JpaESAL esal = JpaESAL.builder().id(UUID.randomUUID().toString()).name(DEFAULT_ESAL).build();
        createAndLinkESAL(contactPerson, esal);
        JpaProposal jpaProposal = JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Recogida de ropita")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4").build())
                .esal(esal)
                .startingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("20-08-2020"))
                .closingProposalDate( new SimpleDateFormat("dd-MM-yyyy").parse("24-08-2020"))
                .startingVolunteeringDate(new SimpleDateFormat("dd-MM-yyyy").parse("25-08-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .published(isPublished)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .category(ProposalCategory.ON_SITE.toString())
                .imageUrl(createMockImageUrl().toString())
                .build();
        jpaProposal = createProposal(jpaProposal);
        jpaProposalSkillsRepository.save(JpaProposalSkills.builder()
                .name("Asertividad")
                .description("Aprenderás habilidades para una mejor comunicación")
                .proposal(jpaProposal)
                .build());
        jpaProposalRequirementsRepository.save(JpaProposalRequirements.builder()
                .name("Disponer de vehículo")
                .proposal(jpaProposal)
                .build());
        return jpaProposal;
    }

    public String registerESALandPublishedProposalObject() throws ParseException {
        JpaContactPerson contactPerson = createESALMember(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JpaESAL esal = JpaESAL.builder().id(UUID.randomUUID().toString()).name(DEFAULT_ESAL).build();
        createAndLinkESAL(contactPerson, esal);

        Proposal proposal = Proposal.builder().id(Id.newId())
                .title("Recogida de ropita")
                .esal(new ESAL(esal.getName(), new Id(esal.getId())))
                .location(new Location("SC Tenerife", "La Laguna", "Avenida Trinidad"))
                .startingProposalDate(ProposalDate.createStartingProposalDate("20-01-2021"))
                .closingProposalDate(ProposalDate.createClosingProposalDate("24-01-2021"))
                .startingVolunteeringDate(ProposalDate.createStartingVolunteeringDate("25-01-2021"))
                .requiredDays("Weekends")
                .permittedAgeRange(AgeRange.create(18, 26))
                .published(true)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .category(ProposalCategory.ON_SITE)
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .image(createMockImageUrl())
                .build();
        Arrays.asList(new Skill("Habilidad", "Descripción"), new Skill("Negociación", "Saber regatear"))
                .forEach(proposal::addSkill);
        Arrays.asList(new Requirement("Forma física para cargar con la ropa"), new Requirement("Disponibilidad horaria"), new Requirement("Carnet de conducir"))
                .forEach(proposal::addRequirement);

        return proposalRepository.save(proposal);
    }

    @SneakyThrows
    public URL createMockImageUrl() {
        return new URL(awsS3Properties.getEndpoint() + '/' + awsS3Properties.getBucketName() + "/test-data/" + UUID.randomUUID() + ".png");
    }

    public MultipartFile createMockMultipartFile() {
        return new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes());
    }
}
