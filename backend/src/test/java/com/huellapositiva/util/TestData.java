package com.huellapositiva.util;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.exception.InvalidStatusIdException;
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
import java.time.LocalDate;
import java.util.*;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;
import static com.huellapositiva.domain.model.valueobjects.Roles.REVISER;

@AllArgsConstructor
@TestComponent
@Transactional
public class TestData {

    public static final String DEFAULT_FROM = "noreply@huellapositiva.com";

    public static final String DEFAULT_SUBJECT = "Asunto del email";

    public static final String DEFAULT_EMAIL = "foo@huellapositiva.com";

    public static final String DEFAULT_EMAIL_2 = "foo_2@huellapositiva.com";

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

    @Autowired
    private final JpaProposalStatusRepository jpaProposalStatusRepository;

    @Autowired
    private final JpaProfileRepository jpaProfileRepository;

    @Autowired
    private final JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private final JpaReviserRepository jpaReviserRepository;

    public void resetData() {
        jpaReviserRepository.deleteAll();
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

    public JpaCredential createCredential(String email, UUID token) {
        return createCredential(email, DEFAULT_PASSWORD, token);
    }

    public JpaCredential createCredential(String email, Roles role) {
        return createCredential(email, UUID.randomUUID(), DEFAULT_PASSWORD, role);
    }

    public JpaCredential createCredential(String email, String plainPassword, UUID token) {
        return createCredential(email, token, plainPassword, Roles.VOLUNTEER_NOT_CONFIRMED);
    }

    public JpaCredential createCredential(String email, UUID token, String plainPassword, Roles userRole){
        EmailConfirmation emailConfirmation = createEmailConfirmation(token);
        Role role = roleRepository.findByName(userRole.toString()).orElse(null);
        JpaCredential jpaCredential = JpaCredential.builder()
                .email(email)
                .hashedPassword(passwordEncoder.encode(plainPassword))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .roles(Collections.singleton(role))
                .build();

        if (REVISER.toString().equals(userRole.toString())) {
            System.out.println("HOlaaaa");
            JpaReviser jpaReviser = JpaReviser.builder()
                    .id(Id.newId().toString())
                    .credential(jpaCredential)
                    .name("name")
                    .surname("surname")
                    .build();
            jpaReviserRepository.save(jpaReviser);
            return jpaCredentialRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        }

        return jpaCredentialRepository.save(jpaCredential);
    }

    public JpaVolunteer createVolunteerWithProfile(String email, String password) {
        createVolunteer(email, password, Roles.VOLUNTEER);
        return createVolunteerProfile(email);
    }

    public JpaVolunteer createVolunteer(String email, String password) {
        return createVolunteer(email, password, Roles.VOLUNTEER);
    }

    public JpaVolunteer createVolunteer(String email, String password, Roles role) {
        JpaCredential jpaCredential = createCredential(email, UUID.randomUUID(), password, role);

        JpaVolunteer volunteer = JpaVolunteer.builder()
                .credential(jpaCredential)
                .id(UUID.randomUUID().toString())
                .build();

        return volunteerRepository.save(volunteer);
    }

    public JpaContactPerson createESALJpaContactPerson(String email, String password) {
        return createESALJpaContactPerson(email, password, Roles.CONTACT_PERSON);
    }

    public JpaContactPerson createESALJpaContactPerson(String email, String password, Roles role) {
        JpaCredential jpaCredential = createCredential(email, UUID.randomUUID(), password, role);
        JpaContactPerson contactPerson = JpaContactPerson.builder()
                .credential(jpaCredential)
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


    public ProposalRequestDto buildProposalDto(){
        return ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .zipCode("12345")
                .island("Tenerife")
                .startingProposalDate("15-06-2021")
                .closingProposalDate("24-06-2021")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("30-06-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
    }

    public Proposal buildPublishedProposalWithEsal(ESAL esal) {
        return buildProposal(esal, PUBLISHED);
    }

    public Proposal buildUnpublishedProposalWithEsal(ESAL esal) {
        return buildProposal(esal, UNPUBLISHED);
    }

    @SneakyThrows
    public Proposal buildProposal(ESAL esal, ProposalStatus status) {
        Proposal proposal = Proposal.builder()
                .id(Id.newId())
                .title("Recogida de ropita")
                .esal(esal)
                .location(new Location("SC Tenerife", "La Laguna", "Avenida Trinidad", "12345", "Tenerife"))
                .startingProposalDate(ProposalDate.createStartingProposalDate("20-01-2021"))
                .closingProposalDate(ProposalDate.createClosingProposalDate("24-01-2021"))
                .startingVolunteeringDate(ProposalDate.createClosingProposalDate("25-01-2021"))
                .requiredDays("Weekends")
                .permittedAgeRange(AgeRange.create(18, 26))
                .status(status)
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

    public JpaProposal registerESALAndPublishedProposal() {
        return registerESALAndProposal(PUBLISHED);
    }

    public JpaProposal registerESALAndNotPublishedProposal() {
        return registerESALAndProposal(UNPUBLISHED);
    }

    public JpaProposal registerESALAndFinishedProposal() {
        return registerESALAndProposal(FINISHED);
    }

    public JpaProposal registerESALAndProposalWithInscribedVolunteers() {
        return registerESALAndProposalWithInscribedVolunteers(PUBLISHED);
    }

    @SneakyThrows
    private JpaProposal registerESALAndProposalWithInscribedVolunteers(ProposalStatus proposalStatus) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        JpaVolunteer jpaVolunteer = createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD, Roles.VOLUNTEER);
        JpaVolunteer jpaVolunteer2 = createVolunteer(DEFAULT_EMAIL_2, DEFAULT_PASSWORD, Roles.VOLUNTEER);

        Set<JpaVolunteer> jpaVolunteers = new HashSet<>();
        jpaVolunteers.add(jpaVolunteer);
        jpaVolunteers.add(jpaVolunteer2);

        JpaContactPerson contactPerson = createESALJpaContactPerson(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JpaESAL esal = JpaESAL.builder().id(UUID.randomUUID().toString()).name(DEFAULT_ESAL).build();
        createAndLinkESAL(contactPerson, esal);
        JpaProposal jpaProposal = JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Recogida de ropita")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4")
                        .island("Tenerife")
                        .zipCode("12345").build())
                .esal(esal)
                .startingProposalDate(simpleDateFormat.parse("20-08-2020"))
                .closingProposalDate( simpleDateFormat.parse("24-08-2020"))
                .startingVolunteeringDate(simpleDateFormat.parse("25-08-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .status(getJpaStatus(proposalStatus))
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .inscribedVolunteers(jpaVolunteers)
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

    @SneakyThrows
    private JpaProposal registerESALAndProposal(ProposalStatus proposalStatus) {
        JpaContactPerson contactPerson = createESALJpaContactPerson(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JpaESAL esal = JpaESAL.builder().id(UUID.randomUUID().toString()).name(DEFAULT_ESAL).build();
        createAndLinkESAL(contactPerson, esal);
        JpaProposal jpaProposal = JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Recogida de ropita")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4")
                        .zipCode("12345")
                        .island("Tenerife").build())
                .esal(esal)
                .startingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("20-08-2020"))
                .closingProposalDate( new SimpleDateFormat("dd-MM-yyyy").parse("24-08-2020"))
                .startingVolunteeringDate(new SimpleDateFormat("dd-MM-yyyy").parse("25-08-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .status(getJpaStatus(proposalStatus))
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
        JpaContactPerson contactPerson = createESALJpaContactPerson(DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JpaESAL esal = JpaESAL.builder().id(UUID.randomUUID().toString()).name(DEFAULT_ESAL).build();
        createAndLinkESAL(contactPerson, esal);

        Proposal proposal = Proposal.builder().id(Id.newId())
                .title("Recogida de ropita")
                .esal(new ESAL(esal.getName(), new Id(esal.getId())))
                .location(new Location("SC Tenerife", "La Laguna", "Avenida Trinidad", "12345", "Tenerife"))
                .startingProposalDate(ProposalDate.createStartingProposalDate("20-01-2021"))
                .closingProposalDate(ProposalDate.createClosingProposalDate("24-01-2021"))
                .startingVolunteeringDate(ProposalDate.createStartingVolunteeringDate("25-01-2021"))
                .requiredDays("Weekends")
                .permittedAgeRange(AgeRange.create(18, 26))
                .status(PUBLISHED)
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

    public JpaProposalStatus getJpaStatus(ProposalStatus proposalStatus) {
        return jpaProposalStatusRepository.findById(proposalStatus.getId())
            .orElseThrow(InvalidStatusIdException::new);
    }

    /*If was necessary, you would to do the method public... or create other method*/
    private JpaVolunteer createVolunteerProfile(String email) {
        String id = Id.newId().toString();
        JpaProfile jpaProfile = JpaProfile.builder()
                .id(id)
                .name("nombre")
                .surname("apellidos")
                .phoneNumber("12412412125")
                .birthDate(LocalDate.of(1993, 12, 12))
                .photoUrl("Una direccion ahi")
                .curriculumVitaeUrl("Una direccion ahi")
                .twitter("Aqui un enlace a twitter")
                .linkedin("Aqui un enlace a linkedin")
                .instagram("Aqui un enlace a instagram")
                .additionalInformation("Pequenna descripcion")
                .build();
        jpaProfileRepository.save(jpaProfile);

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(email);
        jpaVolunteerRepository.updateProfile(jpaVolunteer.getId(), jpaProfile);

        JpaLocation jpaLocation = JpaLocation.builder()
                .id(Id.newId().toString())
                .province("Las Palmas")
                .zipCode("35100")
                .town("Maspalomas")
                .address("Calle Italia N1")
                .island("Gran Canaria")
                .build();
        jpaLocationRepository.save(jpaLocation);
        jpaVolunteerRepository.updateLocation(jpaVolunteer.getId(), jpaLocation);

        jpaVolunteer.setProfile(jpaProfile);
        jpaVolunteer.setLocation(jpaLocation);

        return jpaVolunteer;
    }
}
