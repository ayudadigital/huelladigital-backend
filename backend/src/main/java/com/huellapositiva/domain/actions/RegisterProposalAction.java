package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ESALRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {

    private final ESALRepository esalRepository;

    private final ESALContactPersonRepository esalContactPersonRepository;

    @Autowired
    private final ProposalRepository proposalRepository;

    public String execute(ProposalRequestDto dto, String contactPersonEmail) throws ParseException {
        ESAL joinedESAL = esalContactPersonRepository.getJoinedESAL(contactPersonEmail);
        Proposal proposal = Proposal.parseDto(dto, joinedESAL);
        proposal.validate();
        return proposalRepository.save(proposal);
    }

    public String execute(ProposalRequestDto dto) throws ParseException {
        ESAL joinedESAL = esalRepository.findByName(dto.getEsalName());
        Proposal proposal = Proposal.parseDto(dto, joinedESAL);
        proposal.validate();
        return proposalRepository.save(proposal);
    }
}
