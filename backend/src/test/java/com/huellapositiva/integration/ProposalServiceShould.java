package com.huellapositiva.integration;

import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Date;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.ENROLLMENT_CLOSED;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestData.class)
class ProposalServiceShould {

    @Autowired
    private TestData testData;

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @Autowired
    private ProposalService proposalService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void change_status_to_finished() throws Exception {
        // GIVEN
        JpaProposal publishedProposal = testData.registerESALAndProposal(PUBLISHED);
        publishedProposal.setClosingProposalDate(Date.from(Instant.now().minus(1, DAYS)));

        // WHEN
        proposalService.changeStatusToFinished(publishedProposal.getId());

        // THEN
        JpaProposal fetchedProposal = jpaProposalRepository.findByNaturalId(publishedProposal.getId()).get();
        assertThat(fetchedProposal.getStatus().getName()).isEqualTo("finished");
    }

    @Test
    void finish_proposal_using_trigger_if_criteria_is_met() throws Exception {
        //GIVEN
        JpaProposal publishedProposal = testData.registerESALAndProposal(PUBLISHED);
        publishedProposal.setClosingProposalDate(Date.from(Instant.now().minus(1, DAYS)));
        jpaProposalRepository.save(publishedProposal);

        //WHEN
        proposalService.changeExpiredProposalStatusToFinished();

        //THEN
        JpaProposal fetchedProposal = jpaProposalRepository.findByNaturalId(publishedProposal.getId()).get();
        assertThat(fetchedProposal.getStatus().getName()).isEqualTo("finished");
    }

    @Test
    void not_finish_proposal_using_trigger_if_criteria_is_not_met() throws Exception {
        //GIVEN
        JpaProposal enrollmentClosedProposal = testData.registerESALAndProposal(ENROLLMENT_CLOSED);
        enrollmentClosedProposal.setClosingProposalDate(Date.from(Instant.now().plus(1, DAYS)));
        jpaProposalRepository.save(enrollmentClosedProposal);

        //WHEN
        proposalService.changeExpiredProposalStatusToFinished();

        //THEN
        JpaProposal fetchedProposal = jpaProposalRepository.findByNaturalId(enrollmentClosedProposal.getId()).get();
        assertThat(fetchedProposal.getStatus().getName()).isEqualTo("enrollment_closed");
    }
}