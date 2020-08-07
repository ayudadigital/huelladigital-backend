package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.ProposalRequestDto;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.repository.ESALContactPersonRepository;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.domain.service.ProposalService;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@RequiredArgsConstructor
@Service
public class RegisterProposalAction {

    private final JpaContactPersonRepository jpaContactPersonRepository;

    private final JpaESALRepository jpaESALRepository;

    private final ProposalService proposalService;

    private final ESALContactPersonRepository esalContactPersonRepository;

    @Autowired
    private final ProposalRepository proposalRepository;

    public String execute(ProposalRequestDto dto, String contactPersonEmail) {
        ESAL joinedESAL = esalContactPersonRepository.getJoinedESAL(contactPersonEmail);
        Proposal proposal = Proposal.builder()
                .title(dto.getTitle())
                .esal(joinedESAL)
                .expirationDate(dto.getExpirationDate())
                .maximumAge(dto.getMaximumAge())
                .minimumAge(dto.getMinimumAge())
                .province(dto.getProvince())
                .address(dto.getAddress())
                .requiredDays(dto.getRequiredDays())
                .town(dto.getTown())
                .published(dto.isPublished())
                .build();
        return proposalRepository.save(proposal);
    }

    public String execute(ProposalRequestDto dto) {
        JpaESAL esal = jpaESALRepository.findByName(dto.getEsalName())
                .orElseThrow( () -> new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        dto.setEsalName(esal.getName());
        return proposalService.registerProposal(dto);
    }
}
