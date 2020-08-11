package com.huellapositiva.domain.service;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.application.exception.FailedToPersistProposal;
import com.huellapositiva.application.exception.ProposalEnrollmentClosed;
import com.huellapositiva.application.exception.ProposalNotPublished;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@AllArgsConstructor
public class ProposalService {

    @Autowired
    private final ProposalRepository proposalRepository;

    public String registerProposal(ProposalRequestDto proposalRequestDto) {
        try {
             return proposalRepository.save(proposalRequestDto);
        } catch (DataIntegrityViolationException ex) {
            log.error("Unable to persist the proposal due to a conflict.", ex);
            throw new FailedToPersistProposal("Conflict encountered while storing the proposal in database. Constraints were violated.", ex);
        }
    }

    public JpaProposal enrollVolunteer(String proposalId, JpaVolunteer volunteer) {
        JpaProposal proposal = proposalRepository.fetch(proposalId);
        boolean isNotPublished = !proposal.getPublished();
        if (isNotPublished) {
            throw new ProposalNotPublished();
        }
        boolean isEnrollmentClosed = proposal.getExpirationDate().before(new Date());
        if (isEnrollmentClosed) {
            throw new ProposalEnrollmentClosed();
        }
        proposal.getInscribedVolunteers().add(volunteer);
        return proposalRepository.save(proposal);
    }
}
