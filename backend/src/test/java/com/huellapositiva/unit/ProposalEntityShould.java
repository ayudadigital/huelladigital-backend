package com.huellapositiva.unit;

import com.huellapositiva.domain.exception.InvalidProposalRequestException;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.AgeRange;
import com.huellapositiva.domain.model.valueobjects.ProposalDate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProposalEntityShould {

    @Test
    void return_400_when_starting_date_is_less_than_3_days_from_now() {
        // GIVEN
        ProposalDate startingProposalDate = new ProposalDate(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        ProposalDate closingProposalDate = new ProposalDate(Date.from(Instant.now().plus(2, ChronoUnit.DAYS)));
        ProposalDate startingVolunteeringDate = new ProposalDate(Date.from(Instant.now().plus(4, ChronoUnit.DAYS)));
        Proposal proposal = Proposal.builder()
                .permittedAgeRange(AgeRange.create(18, 80))
                .closingProposalDate(closingProposalDate)
                .startingProposalDate(startingProposalDate)
                .startingVolunteeringDate(startingVolunteeringDate)
                .build();

        // WHEN + THEN
        assertThrows(InvalidProposalRequestException.class, proposal::validate);
    }
}
