package com.huellapositiva.integration;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.Organization;
import com.huellapositiva.infrastructure.orm.entities.Proposal;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Calendar;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class ProposalRepositoryShould {

    @Autowired
    private ProposalRepository proposalRepository;

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @Autowired
    private TestData testData;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void persist_a_default_expiration_hour(){
        // WHEN
        testData.createESAL(Organization.builder().name("Huella Positiva").build());
        ProposalRequestDto proposalRequestDto = testData.buildProposalDto(true);
        proposalRequestDto.setOrganizationName("Huella Positiva");
        Integer id = proposalRepository.save(proposalRequestDto);

        Proposal proposal = jpaProposalRepository.findById(id).get();
        Calendar expirationTimestamp = Calendar.getInstance();
        expirationTimestamp.setTime(proposal.getExpirationDate());
        // THEN
        assertAll(
                () -> assertThat(expirationTimestamp.get(HOUR_OF_DAY)).isEqualTo(23),
                () -> assertThat(expirationTimestamp.get(MINUTE)).isEqualTo(55)
        );
    }
}

