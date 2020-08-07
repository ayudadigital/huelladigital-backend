package com.huellapositiva.integration;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Calendar;
import java.util.UUID;

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
        testData.createESAL(JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalRequestDto = testData.buildProposalDto(true);
        proposalRequestDto.setEsalName("Huella Positiva");
        String id = proposalRepository.save(proposalRequestDto);

        JpaProposal proposal = jpaProposalRepository.findById(Integer.valueOf(id)).get();
        Calendar expirationTimestamp = Calendar.getInstance();
        expirationTimestamp.setTime(proposal.getExpirationDate());
        // THEN
        assertAll(
                () -> assertThat(expirationTimestamp.get(HOUR_OF_DAY)).isEqualTo(23),
                () -> assertThat(expirationTimestamp.get(MINUTE)).isEqualTo(55)
        );
    }
}

