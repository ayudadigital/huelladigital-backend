package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.valueobjects.ProposalStatus;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class CancelProposalAction {

    @Autowired
    ProposalRepository proposalRepository;

    /**
     * This method changes the specified proposal status to CANCELLED.
     *
     * @param id Proposal id
     *
     * (method "updateProposalStatus" returns 0 in case it doesn't find any proposal with that id)
     */
    public void executeByReviser(String id) {
        JpaProposalStatus jpaProposalStatus = JpaProposalStatus.builder()
                .id(ProposalStatus.CANCELLED.getId())
                .name("CANCELLED").build();
        int proposalNotFound = 0;
        if (proposalRepository.updateProposalStatus(id, jpaProposalStatus) == proposalNotFound)
            throw new EntityNotFoundException ();
    }
}
