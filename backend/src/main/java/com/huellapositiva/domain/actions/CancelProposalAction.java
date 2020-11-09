package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.model.valueobjects.ProposalStatus;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.JpaProposalStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CancelProposalAction {

    @Autowired
    ProposalRepository proposalRepository;

    public void executeByReviser(String id){
        JpaProposalStatus jpaProposalStatus = JpaProposalStatus.builder()
                .id(ProposalStatus.CANCELLED.getId())
                .name("CANCELLED").build();
        proposalRepository.updateProposalStatus(id, jpaProposalStatus);
    }
}
