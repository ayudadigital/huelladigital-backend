package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsOrganizationEmployeeRequestDto;
import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.service.OrganizationEmployeeService;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Proposal;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static com.huellapositiva.util.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
public class ProposalServiceShould {

    @Autowired
    private TestData testData;

    @Autowired
    private OrganizationEmployeeService organizationEmployeeService;

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

//    @Test
//    void persist_an_entity_in_db() {
//        // GIVEN
//        CredentialsOrganizationEmployeeRequestDto organizationDto = CredentialsOrganizationEmployeeRequestDto.builder()
//                .email(DEFAULT_EMAIL)
//                .password(DEFAULT_PASSWORD)
//                .name(DEFAULT_ORGANIZATION)
//                .build();
//        organizationEmployeeService.registerEmployee(PlainPassword.from(organizationDto.getPassword()), EmailConfirmation.from(organizationDto.getEmail(), ""), organizationDto.getName());
//
//
//        // WHEN
//        Integer proposalId = proposalService.registerProposal(proposalDto);
//
//        // THEN
//        Optional<Proposal> proposalOptional = jpaProposalRepository.findByIdWithOrganizationAndInscribedVolunteers(proposalId);
//        assertTrue(proposalOptional.isPresent());
//        Proposal proposal = proposalOptional.get();
//        assertThat(proposal.getTitle(), is("Recogida de alimentos"));
//        assertThat(proposal.getOrganizationEmployee().getName(), is(DEFAULT_ORGANIZATION));
//        assertThat(proposal.getLocation().getProvince(), is("Santa Cruz de Tenerife"));
//    }
}
