package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalCancelReasonDto;
import com.huellapositiva.domain.exception.InvalidProposalStatusException;
import com.huellapositiva.domain.model.valueobjects.ProposalStatus;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaProposal;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;
import static java.lang.String.format;

@Service
public class CancelProposalAction {

    @Autowired
    ProposalRepository proposalRepository;

    @Autowired
    JpaProposalRepository jpaProposalRepository;

    /**
     * This method changes the specified proposal status to CANCELLED.
     *
     * @param id  Proposal id
     * @param dto Reason of why a proposal has been cancelled
     *            (method "updateProposalStatus" returns 0 in case it doesn't find any proposal with that id)
     */
    public void executeByReviser(String id, ProposalCancelReasonDto dto) {
        JpaProposal proposal = jpaProposalRepository.findByNaturalId(id).orElseThrow(EntityNotFoundException::new);
        Integer status = proposal.getStatus().getId();
        if (status.equals(FINISHED.getId()) || status.equals(INADEQUATE.getId()) || status.equals(CANCELLED.getId())) {
            throw new InvalidProposalStatusException(format("Invalid proposal transition status from %s to %s", proposal.getStatus(), CANCELLED));
        }
        JpaProposalStatus jpaProposalStatus = JpaProposalStatus.builder()
                .id(ProposalStatus.CANCELLED.getId())
                .name("CANCELLED").build();
        jpaProposalRepository.cancelProposalById(id, jpaProposalStatus, dto.getReason());
    }
}
