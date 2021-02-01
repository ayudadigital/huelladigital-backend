package com.huellapositiva.domain.service;

import com.huellapositiva.application.exception.ProposalEnrollmentClosedException;
import com.huellapositiva.application.exception.ProposalNotPublishedException;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.repository.ProposalRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.PUBLISHED;

@Slf4j
@Service
@AllArgsConstructor
public class ProposalService {

    @Autowired
    private final ProposalRepository proposalRepository;

    /**
     * This method fetches the proposal requested to enroll in and if enrollment is available it enrolls the volunteer
     *
     * @param proposalId proposal's id
     * @param volunteer volunteer information
     */
    public void enrollVolunteer(String proposalId, Volunteer volunteer) {
        Proposal proposal = proposalRepository.fetch(proposalId);
        if (proposal.getStatus() != PUBLISHED) {
            throw new ProposalNotPublishedException("Proposal not found. Proposal ID: " + proposalId);
        }
        boolean isEnrollmentClosed = proposal.getClosingProposalDate().isBeforeNow();
        if (isEnrollmentClosed) {
            throw new ProposalEnrollmentClosedException();
        }
        proposal.inscribeVolunteer(volunteer);
        proposalRepository.save(proposal);
    }
}
